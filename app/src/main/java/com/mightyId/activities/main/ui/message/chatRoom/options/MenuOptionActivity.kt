package com.mightyId.activities.main.ui.message.chatRoom.options

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.activities.main.ui.message.chatRoom.addUser.ChatRoomAddUser
import com.mightyId.activities.main.ui.message.chatRoom.captureImage.CameraActivity
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_INFO
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_NAME
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_PHOTO
import com.mightyId.activities.main.ui.message.chatRoom.listMember.ChatRoomListMember
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.StoredMediaActivity
import com.mightyId.activities.main.ui.message.createNewTopic.CreateNewTopicFragment
import com.mightyId.activities.main.ui.message.home.MessageViewModel
import com.mightyId.databinding.ActivityMenuOptionBinding
import com.mightyId.databinding.DialogEditTopicNameBinding
import com.mightyId.models.Account
import com.mightyId.models.ChatRoomModel
import com.mightyId.utils.*
import com.mightyId.utils.Constant.Companion.PRIVACY_PRIVATE
import com.mightyId.utils.Constant.Companion.PRIVACY_PUBLIC
import com.mightyId.utils.Constant.Companion.USER_INFO
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.r0adkll.slidr.Slidr
import timber.log.Timber

class MenuOptionActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMenuOptionBinding

    private lateinit var pinViewModel: MessageViewModel
    private lateinit var chatRoomModel: ChatRoomModel
    private val viewModel: MenuOptionViewModel by viewModels()
    private var listMember = arrayListOf<Account>()
    private var account = Account()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_option)
        pinViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        chatRoomModel = intent.getInfoExtra(TOPIC_INFO)
        account = intent.getInfoExtra(USER_INFO)
        Timber.tag("MenuOptionActivity").d("onCreate: $account")
        val progressDrawable = getProgressDrawable(this)
        binding.topicAvatar.loadImageWithRoundBorder(chatRoomModel.photoUrl, progressDrawable)
        binding.topicName.text = chatRoomModel.name

        changeUiDependOnTopicType(chatRoomModel.type)
//        binding.actionChangeTopicAvatar.loadImage(intent.getStringExtra(ChatRoomActivity.TOPIC_PHOTO),null)
        window.apply {
            addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setLightStatusBar(false)
            statusBarColor = resources.getColor(R.color.primary_color, theme)
        }
        Slidr.attach(
            this,
            resources.getColor(R.color.primary_color, theme),
            resources.getColor(R.color.white, theme)
        )

        obverseViewModel()
        viewModel.getMember(chatRoomModel.topicId!!)
        binding.actionEditTopicName.setOnClickListener(this)
        binding.actionChangeTopicAvatar.setOnClickListener(this)
        binding.backSpace.setOnClickListener(this)

        //Menu listener
        binding.searchText.setOnClickListener(this)
        binding.fileText.setOnClickListener(this)
        binding.storedMediaText.setOnClickListener(this)
        binding.addMemberText.setOnClickListener(this)
        binding.listMemberText.setOnClickListener(this)
        binding.pinSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            binding.pinSwitchButton.isChecked = isChecked
            if (isChecked) {
                pinViewModel.addPin(Constant.PIN_TYPE_TOPIC, chatRoomModel.topicId!!)
                Toast.makeText(this, "Conversation pinned", Toast.LENGTH_SHORT).show()
            } else {
                pinViewModel.deletePin(Constant.PIN_TYPE_TOPIC, chatRoomModel.topicId!!)
            }
        }
        binding.deleteText.setOnClickListener(this)
        binding.leaveText.setOnClickListener(this)
        binding.createGroupWithText.setOnClickListener(this)
        binding.unfriendText.setOnClickListener(this)
        //Detect network connection
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar ->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(
                    ColorStateList.valueOf(
                        resources.getColor(
                            R.color.accent_red,
                            theme
                        )
                    )
                )
        }
        Common.isConnected.observe(this) {
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
    }

    private fun changeUiDependOnTopicType(type: String) {
        when (type) {
            PRIVACY_PRIVATE -> {
                binding.createGroupWithText.text =
                    getString(R.string.create_group_with, chatRoomModel.name)
                account = intent.getInfoExtra(USER_INFO)
                binding.addMemberLayout.visibility = View.GONE
                binding.listMemberLayout.visibility = View.GONE
                binding.deleteLayout.visibility = View.GONE
                binding.leaveLayout.visibility = View.GONE
                binding.actionEditTopicName.visibility = View.GONE
            }
            PRIVACY_PUBLIC -> {
                binding.deleteLayout.visibility = View.GONE
                binding.createGroupWithLayout.visibility = View.GONE
                binding.unfriendLayout.visibility = View.GONE
            }
        }
    }

    private fun obverseViewModel() {
        viewModel.listMember.observe(this) {
            listMember = it
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constant.CAMERA_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivityForResult(intent, ChatRoomActivity.ACTION_UPLOAD_CAPTURE_IMAGE)
                }
            }
            ChatRoomActivity.REQUEST_STORAGE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent(Intent.ACTION_PICK).also {
                        it.type = "image/*"
                        val mimeTypes = arrayOf("image/jpeg", "image/png")
                        it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                        startActivityForResult(it, ChatRoomActivity.ACTION_UPLOAD_IMAGE)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            //TODO: In developing
            binding.searchText,
            binding.fileText,
            binding.deleteText,
            binding.leaveText,
            -> {
                this.inDevelop()
            }

            binding.createGroupWithText -> {
                CreateNewTopicFragment.newInstance(account)
                    .show(supportFragmentManager, CreateNewTopicFragment.TAG)
            }

            binding.unfriendText -> {
                val alertDialog = AlertDialog.Builder(this)
                    .setMessage("Are you sure want to unfriend?")
                    .setPositiveButton("Ok") { _, _ ->
                        viewModel.deleteFriend(arrayListOf(account.customerId!!))
                        finish()
                    }.setNegativeButton("Cancel", null)
                    .show()
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            }

            binding.storedMediaText -> {
                val intent = Intent(this, StoredMediaActivity::class.java)
                intent.putExtra(ChatRoomActivity.TOPIC_ID, chatRoomModel.topicId)
                startActivity(intent)
                overridePendingTransition(R.anim.in_right_no_transition, 0)
            }

            binding.listMemberText -> {
                ChatRoomListMember.newInstance(chatRoomModel.topicId!!, listMember)
                    .show(supportFragmentManager, ChatRoomListMember.TAG)
            }

            binding.addMemberText -> {
                ChatRoomAddUser.newInstance(topicId = chatRoomModel.topicId!!, listMember)
                    .show(supportFragmentManager, ChatRoomAddUser.TAG)
            }

            binding.actionEditTopicName -> {
                val dialog = Dialog(this)
                val binding: DialogEditTopicNameBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(dialog.context),
                        R.layout.dialog_edit_topic_name,
                        this.binding.root as ViewGroup,
                        false
                    )
                dialog.apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(binding.root)
                }
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                binding.actionCancel.setOnClickListener { dialog.dismiss() }
                binding.topicName.doOnTextChanged { text, _, _, _ ->
                    if (text.isNullOrEmpty()) {
                        binding.actionSave.setTextColor(
                            ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.bottom_nav_default,
                                    theme
                                )
                            )
                        )
                    } else {
                        binding.actionSave.setTextColor(
                            ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.primary_color,
                                    theme
                                )
                            )
                        )
                    }
                }
                binding.actionSave.setOnClickListener {
                    if (!binding.topicName.text.isNullOrEmpty()) {
                        val newTopicName = binding.topicName.text.toString()
                        viewModel.editTopicName(chatRoomModel.topicId!!, newTopicName)
                        this.binding.topicName.text = binding.topicName.text.toString()
                        val data = Intent().putExtra(TOPIC_NAME, newTopicName)
                        setResult(ChatRoomActivity.ACTION_UPDATE_TOPIC_NAME, data)
                        dialog.dismiss()
                    } else {
                        binding.topicNameLayout.error = getString(R.string.invalid_topic_name)
                    }
                }
                dialog.show()
            }

            binding.backSpace -> {
                finish()
                overridePendingTransition(0, R.anim.out_right_no_transition)
            }

            binding.actionChangeTopicAvatar -> {
                getContent.launch("image/*")
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.changeTopicImage(chatRoomModel.topicId!!, it)
        val intent = Intent().putExtra(TOPIC_PHOTO, it.toString())
        setResult(ChatRoomActivity.ACTION_UPLOAD_TOPIC_PHOTO, intent)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(0, R.anim.out_right_no_transition)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        clearFocusOnOutsideClick()
        return super.dispatchKeyEvent(event)
    }

}