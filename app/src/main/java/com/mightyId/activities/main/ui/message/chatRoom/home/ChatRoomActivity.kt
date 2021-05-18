package com.mightyId.activities.main.ui.message.chatRoom.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.iterator
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.mightyId.R
import com.vanniktech.emoji.EmojiPopup
import com.mightyId.activities.base.BaseActivity
import com.mightyId.activities.call.invitation.InvitationViewModel
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.utils.*
import com.mightyId.activities.login.signup.FragmentWebView
import com.mightyId.activities.main.ui.message.chatRoom.captureImage.CameraActivity
import com.mightyId.activities.main.ui.message.chatRoom.fullscreenImageDialog.ImageDialog
import com.mightyId.databinding.ActivityChatRoomBinding
import com.mightyId.databinding.DialogPinExpandBinding
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomAdapter.Companion.TYPE_NOTIFY
import com.mightyId.activities.main.ui.message.chatRoom.messageOption.DialogForwardMessage
import com.mightyId.activities.main.ui.message.chatRoom.messageOption.DialogMessageOption
import com.mightyId.activities.main.ui.message.chatRoom.options.MenuOptionActivity
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters.MediaAdapter
import com.mightyId.activities.main.ui.message.chatRoom.todo.FragmentCreateTask
import com.mightyId.activities.userDetails.addFriend.FragmentAddFriend
import com.mightyId.adapters.PinAdapter
import com.mightyId.models.*
import com.mightyId.utils.Constant.Companion.CALL_REQUEST
import com.mightyId.utils.Constant.Companion.CAMERA_REQUEST_CODE
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_ACCEPTED
import com.mightyId.utils.Constant.Companion.NOTIFICATION_MESSAGE_ID
import com.mightyId.utils.Constant.Companion.ON_CHAT
import com.mightyId.utils.Constant.Companion.PRIVACY_PRIVATE
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_INFO
import com.mightyId.utils.Constant.Companion.TOPIC
import com.mightyId.utils.Constant.Companion.USER_INFO
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.workManager.DownloadFileWorker
import com.mightyId.notification.cancelNotification
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.jetbrains.anko.clipboardManager
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class ChatRoomActivity : BaseActivity(), View.OnClickListener, MediaAdapter.ItemClickListener,
    ChatRoomAdapter.ItemMessageListener, DialogMessageOption.DialogMessageCallback, FragmentCreateTask.CreateTaskListener,
    ImageDialog.ImageDialogListener,
    FragmentAddFriend.UpdateFriendStatus, PinAdapter.PinListener {

    private val chatRoomAdapter = ChatRoomAdapter(this, arrayListOf(), this, this)
    private val pinAdapter = PinAdapter(arrayListOf(), this)

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var socket: Socket
    private val viewModel: ChatRoomViewModel by viewModels()
    private val callViewModel: InvitationViewModel by viewModels()
    private var requestCall = RequestCall(CALL_REQUEST)
    private var lastMessageId: Int? = null
    private val gson = Gson()
    private var account = Account()
    private lateinit var pinExpandDialog: Dialog

    private var isLoading = true
    private var taskTitle = ""
    private var editMessageId: Int? = null
    private var messageParent: MessageItem? = null
    private val workManager = WorkManager.getInstance(this)
    private var userInfo = PersonalChatInfo()
    private var meetingType = ""
    private var currentTopicInfo: TopicItem? = null
    private var pinList = arrayListOf<MessageItem>()

    private lateinit var chatRoomModel: ChatRoomModel
    private lateinit var chatRoomType: String
    private lateinit var emojiPopup: EmojiPopup

    companion object {
        const val TAG = "ChatRoomActivity"
        const val RECEIVE_MESSAGE = "sentMessage"
        const val DELETE_MESSAGE = ""
        const val CUSTOMER_ID = "customer_id"
        const val TYPING = "TYPING"
        const val STOP_TYPING = "STOP_TYPING"
        const val EVENT_ONLINE = "ONLINE"
        const val USER_TYPING = "userTyping"
        const val USER_PHOTO_URL = "userPhotoUrl"
        const val TOPIC_INFO = "topicInfo"
        const val TOPIC_NAME = "topicName"
        const val TOPIC_ID = "topic_id"
        const val TOPIC_PHOTO = "topicPhoto"
        const val TOPIC_TYPE = "topicType"

        const val ACTION_UPLOAD_CAPTURE_IMAGE = 10
        const val ACTION_UPDATE_TOPIC_NAME = 12
        const val ACTION_UPLOAD_IMAGE = 11
        const val ACTION_UPLOAD_TOPIC_PHOTO = 13
        const val REQUEST_STORAGE_PERMISSION = 102
        const val MENU_OPTION_REQUEST_CODE = 1005
        const val IMAGE_URI = "imageUri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room)
        chatRoomType = intent.getStringExtra(TOPIC_TYPE).toString()
        settingPinList()
        Timber.tag("ChatRoomActivity").d("onCreate: info: $chatRoomType")
        //Detect network connection
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, theme)))
        }
        Common.isConnected.observe(this){
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
        //Detect if it is private or public topic
        when (chatRoomType) {
            "private" -> {
                val chatRoomKey: PersonalChatInfo =
                    intent.getBundleExtra(TOPIC_INFO)!!.getInfoExtra(TOPIC_INFO)
                Timber.tag("ChatRoomActivity").d("onCreate: data receive: $chatRoomKey")
                requestCall.topicId = chatRoomKey.topicId
                requestCall.callerName = chatRoomKey.customerName
                requestCall.callerPhotoURL = chatRoomKey.customerPhotoUrl
                requestCall.privacyMode = PRIVACY_PRIVATE
                requestCall.callerCustomerId = chatRoomKey.customerId
                if (chatRoomKey.listCustomerId!=null) {
                    for (userId in chatRoomKey.listCustomerId!!) {
                        if (userId != Common.currentAccount!!.customerId) {
                            requestCall.callerCustomerId = userId
                        }
                    }
                }
                chatRoomModel = ChatRoomModel(
                    chatRoomKey.customerName.toString(),
                    chatRoomKey.topicId,
                    chatRoomKey.customerPhotoUrl,
                    chatRoomType
                )

                binding.topicName.text = chatRoomKey.customerName
                binding.topicStatus.text =
                    if (chatRoomKey.isOnline == true) getString(R.string.currently_online)
                    else getString(R.string.user_last_seen)
                binding.holderMessageAvatarContainer.loadImage(chatRoomKey.customerPhotoUrl, null)
                binding.emptyListAvatarPartner.loadImage(chatRoomKey.customerPhotoUrl, null)
                if (!chatRoomKey.topicId.isNullOrEmpty()) {
                    //Get initial private message
                    viewModel.getMessage(chatRoomKey.topicId!!,lastMessageId = null)
                    establishConnectionWithSocket()
                    viewModel.getPinMessage(chatRoomKey.topicId!!)
                } else if (!chatRoomKey.customerId.isNullOrEmpty()) {
                    //Topic id null, initial message will get when receive topicId from api
                    viewModel.getPrivateMessageInfo(chatRoomKey.customerId!!)
                }

                when (chatRoomKey.friendStatus) {
                    FRIEND_STATUS_ACCEPTED -> {
                        binding.emptyListFindStatus.text =
                            getString(R.string.message_friend_accepted, chatRoomKey.customerName)
                        binding.emptyListMessage.text =
                            getString(R.string.start_a_message, chatRoomKey.customerName)
                    }
                    else -> {
                        binding.emptyListFindStatus.text =
                            getString(R.string.message_not_friend, chatRoomKey.customerName)
                        binding.emptyListMessage.text = getString(R.string.message_stranger)
                        //TODO: Handle add friend event
//                        binding.actionAddFriend.apply {
//                            makeLinks(Pair(getString(R.string.action_add_friend),
//                                View.OnClickListener {
//                                    FragmentAddFriend.newInstance(account, chatRoomKey.friendStatus)
//                                        .show(supportFragmentManager, "Fragment Add Friend")
//                                }))
//                            visibility = View.VISIBLE
//                        }
                        binding.actionAddFriend.visibility = View.GONE
                    }
                }
            }
            else -> { //Topic Chat
                val chatRoomKey: PublicChatInfo =
                    intent.getBundleExtra(TOPIC_INFO)!!.getInfoExtra(TOPIC_INFO)
                chatRoomModel = ChatRoomModel(
                    chatRoomKey.topicName.toString(),
                    chatRoomKey.topicId!!,
                    chatRoomKey.topicPhotoUrl.toString(),
                    chatRoomType
                )
                //Get initial topic message:
                viewModel.getMessage(chatRoomModel.topicId!!, lastMessageId = null)
                //Get pin messages
                viewModel.getPinMessage(chatRoomKey.topicId!!)
                //Set up call condition
                Timber.tag("ChatRoomActivity").d("onCreate: data receive: $chatRoomKey")
                requestCall.callerName = chatRoomKey.topicName
                requestCall.callerPhotoURL = chatRoomKey.topicPhotoUrl
                requestCall.topicId = chatRoomKey.topicId
                viewModel.getTopicInfo(chatRoomKey.topicId!!)
                binding.topicName.text = chatRoomKey.topicName
                binding.topicStatus.text =
                    getString(R.string.number_participant, chatRoomKey.numberOfParticipants)
                //TODO: Change topic photo's URL
                binding.holderMessageAvatarContainer.loadImage(chatRoomKey.topicPhotoUrl, null)
                establishConnectionWithSocket()
                //Retrieve messages for the first time
            }
        }

        observeViewModel()

        settingUpEmojiKeyboard()

        binding.chatList.adapter = chatRoomAdapter

        binding.backSpace.setOnClickListener(this)
        binding.actionSendMessage.setOnClickListener(this)
        binding.holderActionCall.setOnClickListener(this)
        binding.holderActionVideoCall.setOnClickListener(this)
        binding.holderMoreInfo.setOnClickListener(this)
        binding.actionMinimizeTypingBox.setOnClickListener(this)
        binding.topicChatContent.setOnClickListener(this)
        binding.actionAssignTask.setOnClickListener(this)
        binding.actionUploadImage.setOnClickListener(this)
        binding.replyDismiss.setOnClickListener(this)
        binding.pinExpand.setOnClickListener(this)
        binding.emojiIcon.setOnClickListener(this)
        binding.linearLayout.setOnClickListener(this)
        binding.topicChatContent.setOnClickListener(this)

        binding.topicChatContent.doOnTextChanged { text, _, _, _ ->
            when {
                text.isNullOrEmpty() -> {
                    val jsonObject = JSONObject()
                    jsonObject.put(TOPIC_ID, chatRoomModel.topicId)
                    jsonObject.put(USER_TYPING, Common.currentAccount!!.customerName)
                    jsonObject.put(USER_PHOTO_URL, Common.currentAccount!!.photoUrl)
                    socket.emit(STOP_TYPING, jsonObject)
                }
                text.length == 1 -> {
                    val jsonObject = JSONObject()
                    jsonObject.put(TOPIC_ID, chatRoomModel.topicId)
                    jsonObject.put(USER_TYPING, Common.currentAccount!!.customerName)
                    jsonObject.put(USER_PHOTO_URL, Common.currentAccount!!.photoUrl)
                    socket.emit(TYPING, jsonObject)
                }
            }
        }

        binding.chatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (lastMessageId!! > 0) {
                    val layoutManager = binding.chatList.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val childCount = layoutManager.childCount
                    val totalCount = layoutManager.itemCount
                    Timber.tag("ChatRoomActivity")
                        .d("onScrolled: firstVisibleItemPos: ${layoutManager.findFirstVisibleItemPosition()}")
                    Timber.tag("ChatRoomActivity")
                        .d("onScrolled: childCount : ${layoutManager.childCount}")
                    if (firstVisibleItemPosition > 0 && firstVisibleItemPosition + childCount >= totalCount) {
                        if (!isLoading) {
                            isLoading = true
                            binding.progressBar.visibility = View.VISIBLE
                            viewModel.getMessage(chatRoomModel.topicId!!, lastMessageId)
                        }
                    }
                }
            }
        })
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        registerReceiver(updateExistingMeeting, IntentFilter(Constant.CALL_RESPONSE))
    }

    private fun settingUpEmojiKeyboard() {
        emojiPopup = EmojiPopup.Builder.fromRootView(binding.root)
            .setKeyboardAnimationStyle(R.style.emoji_slide_animation_style)
            .build(binding.topicChatContent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        when {
            binding.topicChatContent.hasFocus() -> {
                hideKeyboard()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!chatRoomModel.topicId.isNullOrEmpty() && chatRoomAdapter.itemCount == 0 && !isLoading) {
            viewModel.getMessage(chatRoomModel.topicId!!, lastMessageId)
        }
    }

    private fun observeViewModel() {
        //Get list messages
        viewModel.chatList.observe(this) {
            lastMessageId = it.lastMessageId
            Timber.tag("ChatRoomActivity")
                .d("obverseViewModel: current lastMessageId : $lastMessageId")
            isLoading = false
//            if (lastMessageId!!<0) binding.chatList.scrollToPosition(0) //move focus on last message
            chatRoomAdapter.update(it.result)
            binding.progressBar.visibility = View.GONE
            if (chatRoomAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
            }
        }
        //Get info of private topic
        viewModel.messageInfo.observe(this) {
            userInfo = it
            account = Account(
                customerName = userInfo.customerName,
                customerId = userInfo.customerId,
                photoUrl = userInfo.customerPhotoUrl,
                workId = userInfo.customerWorkId
            )
            chatRoomModel.topicId = it.topicId.toString()
            //Setting up request call
            Timber.tag("ChatRoomActivity").d("obverseViewModel: data receive: $it")
            viewModel.getPinMessage(chatRoomModel.topicId!!)
            establishConnectionWithSocket()
            //Received topicId, get initial message
            try {
                viewModel.getMessage(chatRoomModel.topicId!!, lastMessageId = null)
            }catch (e: IOException){
               //TODO: update when no internet connection found
            }
        }
        //Handle pin message
        viewModel.pinMessage.observe(this) {
            Timber.tag("ChatRoomActivity")
                .d("observeViewModel: Called ,showPinListDialog: pinList: $it")
            if (it.isNullOrEmpty()) {
                binding.pinLayout.visibility = View.GONE
            } else {
                pinList = it
                binding.pinLayout.visibility = View.VISIBLE
                binding.pinTitle.text = getString(R.string.message_pinned)
                binding.pinContent.text = it[0].messageContent
            }
        }
        //Change UI if meeting are happening
        viewModel.topicInfo.observe(this) {
            currentTopicInfo = it
            if (currentTopicInfo?.callId != null) {
                binding.topicStatus.apply {
                    text = getString(R.string.meeting_in_progress)
                    setTextColor(ColorStateList.valueOf(resources.getColor(R.color.default_green,
                        theme)))
                }
                binding.holderActionCall.apply {
                    setImageResource(R.drawable.ic_group_call)
                    imageTintList = ColorStateList.valueOf(resources.getColor(R.color.white, theme))
                    backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.default_green, theme))
                }
                binding.holderActionVideoCall.apply {
                    setImageResource(R.drawable.ic_video_group_call)
                    imageTintList = ColorStateList.valueOf(resources.getColor(R.color.white, theme))
                    backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.default_green, theme))
                }
            }
        }
        callViewModel.meetingInfo.observe(this) {
            val data = RequestCall(
                messageType = CALL_REQUEST,
                callerName = Common.currentAccount!!.customerName,
                callerEmail = Common.currentAccount!!.customerEmail,
                callerPhotoURL = Common.currentAccount!!.photoUrl
            )
            JitsiMeetUtils.establishConnection(it.serverMeet)
            val jitsiConnection =
                JitsiMeetUtils.configurationMeeting(
                    data,
                    meetingType,
                    it.meetingId
                )
            JitsiMeetUtils.launch(this, jitsiConnection, it.callId, it.topicId,it.privacyMode)
        }
        //Response to request assign task
        viewModel.taskAssigned.observe(this){
            if (it==true){
                val newMessage = MessageItem(
                        messageContentType = TYPE_NOTIFY,
                        messageContent = getString(
                            R.string.new_todo,
                            Common.currentAccount!!.customerName!!.capitalize(Locale.getDefault()),
                            taskTitle
                        )
                    )
                    chatRoomAdapter.addMessage(newMessage)
                    binding.chatList.scrollToPosition(0)
                Toast.makeText(this,"Task assigned",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun establishConnectionWithSocket() {
        Timber.tag("ChatRoomActivity").d("establishConnectionWithSocket: Called")
        try {
            socket = IO.socket(Common.SOCKET_URL)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag("ChatRoomActivity").e("establishConnectionWithSocket: $e")
        }
        socket.connect()
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(RECEIVE_MESSAGE, onUpdateChat)
        socket.on(DELETE_MESSAGE, onMessageDeleted)
        socket.on(TYPING, onTyping)
        socket.on(STOP_TYPING, onStopTyping)
        val jsonObject = JSONObject()
        jsonObject.put("customerId", Common.currentAccount!!.customerId)
        jsonObject.put("customerName", Common.currentAccount!!.customerName)
        jsonObject.put("customerAvatar", Common.currentAccount!!.photoUrl)
        socket.emit(EVENT_ONLINE,jsonObject)
    }

    private val onConnect = Emitter.Listener {
        val jsonObject = JSONObject()
        jsonObject.put(TOPIC, chatRoomModel.topicId)
        jsonObject.put(CUSTOMER_ID, Common.currentAccount!!.customerId)
        socket.emit("joinTopic", jsonObject)
    }

    private val onUpdateChat = Emitter.Listener {
        runOnUiThread {
            binding.emptyList.visibility = View.GONE
        }
        //TODO: Change to unique integer foreach notification
        cancelNotification(NOTIFICATION_MESSAGE_ID)
        val chat: MessageItem = gson.fromJson(it[0].toString(), MessageItem::class.java)
        if (chat.customerId != Common.currentAccount!!.customerId) {
            addItemToRecyclerView(chat)
        } else {
            runOnUiThread {
                chatRoomAdapter.updateMessage(chat)
            }
        }
    }

    private val onTyping = Emitter.Listener {
        val typing = gson.fromJson(it[0].toString(), Typing::class.java)
        Timber.tag("ChatRoomActivity").d("onTyping: binding, data received: $typing")
        if (typing.userTyping != Common.currentAccount!!.customerName) {
            binding.partnerAvatar.loadImage(typing.userPhoto, null)
            binding.typing.visibility = View.VISIBLE
        }
    }

    private val onStopTyping = Emitter.Listener {
        val typing = gson.fromJson(it[0].toString(), Typing::class.java)
        Timber.tag("ChatRoomActivity").d("onStopTyping: Called, data received: $typing")
        if (typing.userTyping != Common.currentAccount!!.customerName) {
            binding.typing.visibility = View.GONE
        }
    }

    private val onMessageDeleted = Emitter.Listener {
        val deleteMessage = gson.fromJson(it[0].toString(), MessageItem::class.java)
        chatRoomAdapter.removeMessage(deleteMessage.messageId!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("ChatRoomActivity").d("onDestroy: Called")
        unregisterReceiver(br)
        socket.disconnect()
        socket.off()
    }

    private fun setMenuIconColor(menuItem: MenuItem) {
        val drawable = menuItem.icon
        drawable.mutate()
        drawable.setTintList(ColorStateList.valueOf(resources.getColor(R.color.primary_color,
            theme)))
        drawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            resources.getColor(R.color.primary_color, theme),
            BlendModeCompat.SRC_ATOP)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        for (menuItem in menu) {
            menuItem.iconTintList = ColorStateList.valueOf(resources.getColor(R.color.primary_color,
                theme))
            setMenuIconColor(menuItem)
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivityForResult(intent, ACTION_UPLOAD_CAPTURE_IMAGE)
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent(Intent.ACTION_PICK).also {
                        it.type = "image/*"
                        val mimeTypes = arrayOf("image/jpeg", "image/png")
                        it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                        startActivityForResult(it, ACTION_UPLOAD_IMAGE)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_UPLOAD_CAPTURE_IMAGE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras?.get(("data")) as Bitmap
                    val scaledBitmap =
                        Bitmap.createScaledBitmap(selectedImage, 1500, 1500, false)
                            .resizeBitmap(1500)
                    val uri = getImageUri(this, scaledBitmap!!)
                    viewModel.uploadImage(chatRoomModel.topicId!!, uri!!)
                }
            }
            ACTION_UPLOAD_IMAGE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.data!!
                    viewModel.uploadImage(chatRoomModel.topicId!!, selectedImage)

                    //Update UI local
                    val chat = MessageItem()
                    chat.customerId = Common.currentAccount!!.customerId
                    chat.link = selectedImage.toString()
                    chat.messageContentType = "image"
                    val timeSentLong = Instant.now().toEpochMilli()
                    chat.timeSent =
                        DateUtils.formatDateTime(this, timeSentLong, DateUtils.FORMAT_SHOW_TIME)
                    addItemToRecyclerView(chat)
                }
            }
            ACTION_UPDATE_TOPIC_NAME ->{
                binding.topicName.text = data!!.getStringExtra(TOPIC_NAME)
            }
            ACTION_UPLOAD_TOPIC_PHOTO ->{
                binding.holderMessageAvatarContainer.loadImage(data!!.getStringExtra(TOPIC_PHOTO),null)
            }
        }
    }

    private fun initiateMeeting(requestCall: RequestCall, meetingType: String) {
        Timber.tag("ChatRoomActivity").d("initiateMeeting: Data willing to sent: $requestCall")
        val intent = Intent(this, OutGoingInvitationActivity::class.java)
        requestCall.meetingType = meetingType
        intent.putExtra(TOPIC_TYPE, chatRoomType)
        intent.putExtra(Constant.TAG, TAG)
        intent.putInfoExtra(REMOTE_MSG_CALLER_INFO, requestCall)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onClick(v: View?) {
        when (v) {
            binding.actionSendMessage -> {
                binding.topicChatContent.showKeyboard()
                val message = binding.topicChatContent.text.toString()
                if (message.isNotEmpty()) {
                    if (editMessageId == null) {
                        showTypingBoxOption()
                        val chat = MessageItem()
                        chat.customerId = Common.currentAccount!!.customerId
                        chat.messageContent = binding.topicChatContent.text.toString()
                        chat.messageContentType = "text"
                        chat.parentId = messageParent?.messageId
                        if (messageParent != null) {
                            Timber.tag("ChatRoomActivity")
                                .d("onClick: Message parent not null!:${messageParent!!.messageId} ")
                            chat.hasParent = true
                            chat.messageParent = messageParent
                        }
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00")).time
                        val dateTime = SimpleDateFormat("mm:ss")
                        chat.timeSent = dateTime.format(calendar)
                        addItemToRecyclerView(chat)
                        if (message.isNotEmpty()) {
                            binding.emptyList.visibility = View.GONE
                            viewModel.sendMessage(chatRoomModel.topicId!!, message, chat.parentId)
                        }
                    } else {
                        chatRoomAdapter.editMessage(messageContent = message,
                            messageId = editMessageId!!)
                        viewModel.editMessage(messageContent = message, messageId = editMessageId!!)
                    }
                }
                binding.topicChatContent.setText("")
                binding.replyBox.visibility = View.GONE
                messageParent = null
            }
            binding.holderMoreInfo, binding.linearLayout -> {
                val intent = Intent(this, MenuOptionActivity::class.java).apply {
                    putInfoExtra(TOPIC_INFO, chatRoomModel)
                    putInfoExtra(USER_INFO, account)
                }
                startActivityForResult(intent, MENU_OPTION_REQUEST_CODE)
                overridePendingTransition(R.anim.in_right_no_transition, 0)
            }
//            binding.actionCaptureImage -> {
//                inDevelop()
//                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(arrayOf(Manifest.permission.CAMERA),
//                        CAMERA_REQUEST_CODE)
//                } else {
//                    val intent = Intent(this, CameraActivity::class.java)
//                    startActivityForResult(intent, ACTION_UPLOAD_CAPTURE_IMAGE)
//                }
//            }
            binding.actionAssignTask -> {
                FragmentCreateTask.newInstance(chatRoomModel.topicId!!)
                    .show(supportFragmentManager, FragmentCreateTask.TAG)
            }
            binding.actionUploadImage -> {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_PERMISSION
                    )
                } else {
                    Intent(Intent.ACTION_PICK).also {
                        it.type = "image/*"
                        val mimeTypes = arrayOf("image/jpeg", "image/png")
                        it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                        startActivityForResult(it, ACTION_UPLOAD_IMAGE)
                    }
                }
            }
            binding.replyDismiss -> {
                messageParent = null
                binding.replyBox.visibility = View.GONE
            }
            binding.holderActionCall -> {
                if (currentTopicInfo?.callId != null) {
                    meetingType = "audio"
                    callViewModel.joinExistingTopic(topicItem = currentTopicInfo!!,
                        meetingType = meetingType)
                } else {
                    initiateMeeting(requestCall, "audio")
                }
            }
            binding.holderActionVideoCall -> {
                if (currentTopicInfo?.callId != null) {
                    meetingType = "video"
                    callViewModel.joinExistingTopic(topicItem = currentTopicInfo!!,
                        meetingType = meetingType)
                } else {
                    initiateMeeting(requestCall, "video")
                }
            }
            binding.actionMinimizeTypingBox -> {
                showTypingBoxOption()
            }
            binding.topicChatContent -> {
                emojiPopup.dismiss()
                hideTypingBoxOption()
            }
            binding.backSpace -> {
                super.onBackPressed()
            }
            binding.pinExpand -> {
//                pinExpandDialog.show()
                viewModel.unpinMessage(chatRoomModel.topicId!!,pinList[0].messageId!!)
                binding.pinLayout.visibility = View.GONE
            }
            binding.emojiIcon -> {
                if (!emojiPopup.isShowing) emojiPopup.toggle()
            }
            binding.topicChatContent -> {
                if (emojiPopup.isShowing) emojiPopup.dismiss()
            }
        }
    }

    private fun hideTypingBoxOption() {
        binding.actionUploadImage.visibility = View.GONE
        binding.actionAssignTask.visibility = View.GONE
        binding.actionMinimizeTypingBox.visibility = View.VISIBLE
    }

    private fun showTypingBoxOption() {
        binding.actionUploadImage.visibility = View.VISIBLE
        binding.actionAssignTask.visibility = View.VISIBLE
        binding.actionMinimizeTypingBox.visibility = View.GONE
    }

    private fun addItemToRecyclerView(message: MessageItem) {
        //Since this function is inside of the listener,
        //You need to do it on UIThread!
        runOnUiThread {
            viewModel.chatList.value?.result!!.add(0, message)
            chatRoomAdapter.addMessage(message)
            binding.chatList.scrollToPosition(0) //move focus on last message
        }
    }

    override fun onStart() {
        super.onStart()
        Common.commonInfo += ON_CHAT to 1
    }

    override fun onStop() {
        super.onStop()
        Common.commonInfo += ON_CHAT to 0
        Timber.tag("ChatRoomActivity").d("onStop: Called ${Common.commonInfo[ON_CHAT]}")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus == binding.chatRoomLayout) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            showTypingBoxOption()
            binding.topicChatContent.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onItemClick(imageName: String, imageUrl: String) {
        ImageDialog.newInstance(imageName, imageUrl).show(supportFragmentManager, ImageDialog.TAG)
    }

    override fun onMessageLongClick(messageId: Int, messageType: String, messageViewType: Int) {
        DialogMessageOption.newInstance(messageId, messageType, messageViewType)
            .show(supportFragmentManager,
                DialogMessageOption.TAG)
    }

    override fun onMessageOpenWebUrl(url: String?) {
        Timber.tag("ChatRoomActivity").d("onMessageOpenWebUrl: onClick called")
        FragmentWebView.newInstant(url).show(supportFragmentManager, FragmentWebView.TAG)
    }

    override fun onMessageFileDownload(fileName: String, url: String) {
        val inputData = Data.Builder()
            .putString("fileDownload", url)
            .putString("fileName", fileName)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(DownloadFileWorker::class.java)
            .setInitialDelay(3, TimeUnit.SECONDS)
            .setInputData(inputData)
            .addTag("fileDownload")
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
        binding.root.snackbar("File downloading", getString(android.R.string.cancel)) {
            workManager.cancelUniqueWork("downloadFile")
        }
    }

    @SuppressLint("ServiceCast")
    override fun onDialogMessageCallback(messageId: Int, action: String) {
        when (action) {
            DialogMessageOption.ACTION_REPLY -> {
                val messageItem = chatRoomAdapter.findMessage(messageId)!!
                binding.replyBox.visibility = View.VISIBLE
                binding.replyUser.text = messageItem.customerName
                when (messageItem.messageContentType) {
                    ChatRoomAdapter.TYPE_TEXT -> {
                        binding.replyMessage.text = messageItem.messageContent
                    }
                    ChatRoomAdapter.TYPE_IMAGE -> {
                        binding.replyImage.visibility = View.VISIBLE
                        binding.replyImage.loadImage(messageItem.link, null)
                        binding.replyMessage.text = getString(R.string.reply_picture)
                    }
                }
                messageParent = messageItem
            }
            DialogMessageOption.ACTION_COPY -> {
                val messageItem = chatRoomAdapter.findMessage(messageId)
                val clip =
                    ClipData.newPlainText("label", messageItem?.messageContent)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.message_copied), Toast.LENGTH_SHORT).show()
            }
            DialogMessageOption.ACTION_FORWARD -> {
                val messageItem = chatRoomAdapter.findMessage(messageId)
                DialogForwardMessage.newInstance(messageItem!!).show(
                    supportFragmentManager, DialogForwardMessage.TAG
                )
            }
            DialogMessageOption.ACTION_EDIT -> {
                val messageItem = chatRoomAdapter.findMessage(messageId)
                val messageEditing = messageItem?.messageContent
                binding.topicChatContent.apply {
                    setText(messageEditing)
                    showKeyboard()
                    hideTypingBoxOption()
                    requestFocus()
                    placeCursorToEnd()
                }
                editMessageId = messageId
            }
            DialogMessageOption.ACTION_PIN -> {
                runOnUiThread {
                    val messageItem = chatRoomAdapter.findMessage(messageId)!!
                    Timber.tag("ChatRoomActivity")
                        .d("onDialogMessageCallback: message: ${chatRoomModel.topicId} ${messageItem.messageId}")
                    binding.pinLayout.visibility = View.VISIBLE
                    binding.pinTitle.text = getString(R.string.message_pinned)
                    binding.pinContent.text = messageItem.messageContent
                    viewModel.pinMessage(chatRoomModel.topicId!!, messageItem.messageId!!)
//                    val newMessage = MessageItem(
//                        messageContentType = TYPE_NOTIFY,
//                        messageContent = getString(
//                            R.string.new_pin_message,
//                            Common.currentAccount!!.customerName!!.capitalize(Locale.getDefault())
//                        )
//                    )
//                    chatRoomAdapter.addMessage(newMessage)
                }
            }
            DialogMessageOption.ACTION_DELETE -> {
                chatRoomAdapter.removeMessage(messageId)
                viewModel.deleteMessage(messageId)
            }
            DialogMessageOption.ACTION_SAVE -> {
                inDevelop()
            }
        }
    }

    override fun onAssignTaskCallback(
        title: String,
        content: String,
        assignee: MutableList<Account>,
        deadline: String,
    ) {
        taskTitle = title
        val listAssignee = mutableListOf<String>()
        for (item in assignee) {
            listAssignee.add(item.customerId!!)
        }
        viewModel.createTodo(title,chatRoomModel.topicId!!, content,listAssignee as ArrayList<String>,deadline)
    }

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == DownloadFileWorker.downloadId) {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val updateExistingMeeting = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Timber.tag("ChatRoomActivity").d("onReceive: Called")
            val data: RequestCall = intent.getInfoExtra(CALL_REQUEST)
            viewModel.getTopicInfo(data.topicId!!)
        }
    }

    override fun onImageDownloadCallback() {
        binding.root.snackbar("File downloading", getString(android.R.string.cancel)) {
            workManager.cancelUniqueWork("downloadFile")
        }
    }

    override fun onUpdateFriendStatus() {
    }

    private fun settingPinList() {
        pinExpandDialog = Dialog(this)
        val binding: DialogPinExpandBinding =
            DataBindingUtil.inflate(LayoutInflater.from(pinExpandDialog.context),
                R.layout.dialog_pin_expand,
                this.binding.root as ViewGroup,
                false)
        pinExpandDialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            window?.apply {
                setBackgroundDrawableResource(R.drawable.border_outline_button)
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                attributes.gravity = Gravity.TOP
            }
        }

        binding.pinList.adapter = pinAdapter
        pinAdapter.update(pinList)
        Timber.tag("ChatRoomActivity").d("showPinListDialog: pinList $pinList")
    }

    override fun onRemovePin(messageItem: MessageItem) {
        pinExpandDialog.dismiss()
        pinAdapter.removePin(messageItem)
        binding.root.snackbar("Pin message removed", null, null)
    }
}