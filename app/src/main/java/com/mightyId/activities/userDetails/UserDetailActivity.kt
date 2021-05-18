package com.mightyId.activities.userDetails

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.userDetails.addFriend.FragmentAddFriend
import com.mightyId.databinding.ActivityUserDetailBinding
import com.mightyId.utils.*
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_ACCEPTED
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_NEUTRAL
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_PENDING
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_WAITING_FOR_RESPONSE
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.adapters.PersonalHistoryAdapter
import com.mightyId.callback.CallListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

class UserDetailActivity : BaseActivity(), CallListener, MessageListener,
    FragmentAddFriend.UpdateFriendStatus {

    private lateinit var binding: ActivityUserDetailBinding
    private lateinit var viewRoot: View
    private val viewModel: UserDetailViewModel by viewModels()
    private lateinit var userInfo: Account
    private var historyAdapter = PersonalHistoryAdapter(arrayListOf())
    private val image = listOf(R.drawable.pic_1, R.drawable.pic_2, R.drawable.pic_3)
    private val random = Random()

    companion object {
        const val TAG = "UserDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_detail)
        viewRoot = binding.root
        userInfo = intent.getInfoExtra(Constant.INVITE_USER_INFO)
        viewModel.getCallHistoryOf(userInfo.customerId!!)
        setSupportActionBar(binding.toolbar5)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        observeViewModel()

        binding.userDetailName.text = userInfo.customerName?.capitalize(Locale.ROOT)
        binding.userDetailAvatar.loadImage(userInfo.photoUrl, null)
//        binding.historyList.adapter = historyAdapter
        binding.emptyListMakeCall.makeLinks(Pair("make a call?", object : View.OnClickListener {
            override fun onClick(v: View?) {
                Toast.makeText(this@UserDetailActivity,
                    "Feature are developing",
                    Toast.LENGTH_SHORT).show()
            }
        }))
        binding.holderCall.setOnClickListener { this.initiateMeeting(userInfo, "audio") }
        binding.holderVideoCall.setOnClickListener { this.initiateMeeting(userInfo, "video") }
        binding.holderMessage.setOnClickListener {
            val bundle = Bundle()
            bundle.putInfoExtra(
                ChatRoomActivity.TOPIC_INFO,
                PersonalChatInfo(userInfo.customerId!!,
                    userInfo.customerName!!,
                    userInfo.photoUrl,
                    null,
                    userInfo.friendStatus)
            )
            this.moveToChatRoom("private", bundle)
        }
        binding.backSpace.setOnClickListener { super.onBackPressed() }
        binding.holderAddFriend.setOnClickListener {
            FragmentAddFriend.newInstance(userInfo, userInfo.friendStatus)
                .show(supportFragmentManager, "Fragment Add Friend")
        }
        updateFriendStatusUI(userInfo.friendStatus)
        onResponseAddFriend()
        binding.historyList.adapter = historyAdapter
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, theme)))
        }
        Common.isConnected.observe(this){
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
    }

    private fun onResponseAddFriend() {
        binding.actionAcceptFriend.setOnClickListener {
            viewModel.sendResponseAccept(userInfo.customerId!!)
            updateFriendStatusUI(FRIEND_STATUS_ACCEPTED)
            removeFromPendingList(userInfo.customerId!!)
        }
        binding.actionRejectFriend.setOnClickListener {
            viewModel.sendResponseDecline(userInfo.customerId!!)
            updateFriendStatusUI(FRIEND_STATUS_NEUTRAL)
        }
    }

    private fun updateFriendStatusUI(friendStatus: Int) {
        when (friendStatus) {
            FRIEND_STATUS_ACCEPTED -> {
                binding.friendStatusLayout.visibility = View.GONE
                binding.holderAddFriend.visibility = View.GONE
            }
            FRIEND_STATUS_WAITING_FOR_RESPONSE -> {
                binding.friendStatus.visibility = View.VISIBLE
                binding.friendStatus.text = getString(R.string.friend_request_sent)
            }
            FRIEND_STATUS_PENDING -> {
                binding.friendStatus.visibility = View.VISIBLE
                binding.responseFriendLayout.visibility = View.VISIBLE
                binding.friendStatus.text = getString(R.string.friend_request_received,
                    userInfo.customerName?.capitalize(Locale.ROOT))
            }
            FRIEND_STATUS_NEUTRAL -> {
                binding.friendStatusLayout.visibility = View.GONE
                binding.holderAddFriend.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        Timber.tag("UserDetailActivity").d("onCreateOptionsMenu: Called")
        menuInflater.inflate(R.menu.user_detail_menu, menu)
        for (menuItem in menu) {
            menuItem.iconTintList = ColorStateList.valueOf(resources.getColor(R.color.primary_color,
                theme))
            setMenuIconColor(menuItem)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_share_contact, R.id.menu_add_nickname, R.id.menu_block_user, R.id.menu_delete_history -> {
                inDevelop()
            }
            R.id.menu_delete_contact -> {
                showPopup()
            }
        }
        return true
    }

    private fun showPopup() {
        val alertDialog = AlertDialog.Builder(this)
            .setMessage("Are you sure want to unfriend this contact?")
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.deleteFriend(arrayListOf(userInfo.customerId!!))
                updateFriendStatusUI(friendStatus = FRIEND_STATUS_NEUTRAL)
            }.setNegativeButton("Cancel", null)
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
    }

    private fun removeFromPendingList(customerId:String){
        val sharedPreferences = getSharedPreferences(
            Constant.INVITE_USER_INFO,
            Context.MODE_PRIVATE
        )
        val gson = Gson()
        val json = sharedPreferences.getString(Constant.PENDING_FRIEND_LIST, "")
        val type: Type = object : TypeToken<MutableList<RequestAddFriendModel>>() {}.type
        val pendingList: MutableList<RequestAddFriendModel>? = gson.fromJson(json, type)
        if (pendingList != null) {
            for (item in pendingList){
                if (item.senderId == customerId){
                    pendingList.removeAt(pendingList.indexOf(item))
                }
            }
        }
        val editor = sharedPreferences.edit()
        editor.putString(Constant.PENDING_FRIEND_LIST, gson.toJson(pendingList))
        editor.apply()
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

    private fun observeViewModel() {
        viewModel.listHistory.observe(this) {
            historyAdapter.update(it)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (historyAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyListImage.setImageResource(image[random.nextInt(image.size)])
            }
        }
        viewModel.isFriendDeleted.observe(this) {
            if (it == true) Toast.makeText(this, "Unfriend successful", Toast.LENGTH_SHORT).show()
        }
    }

    override fun initiateMeeting(account: Account, type: String) {
        Timber.tag("ContactFragment").d("initiateMeeting: Data waiting to send: $account")
        val intent = Intent(this, OutGoingInvitationActivity::class.java)
        val requestCall = RequestCall(Constant.CALL_REQUEST)
        requestCall.callerName = account.customerName
        requestCall.callerPhotoURL = account.photoUrl
        requestCall.callerCustomerId = account.customerId
        requestCall.meetingType = type
        intent.putExtra(Constant.TAG, TAG)
        intent.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, requestCall)
        startActivity(intent)
    }

    override fun initiateMeeting(topicItem: TopicItem, type: String) {
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerFrameLayout.stopShimmer()
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, 0)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        clearFocusOnOutsideClick()
        return super.dispatchKeyEvent(event)
    }

    override fun onUpdateFriendStatus() {
        updateFriendStatusUI(FRIEND_STATUS_WAITING_FOR_RESPONSE)
    }
}