package com.mightyId.activities.main.ui.message.chatRoom.listMember

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.activities.main.ui.message.chatRoom.addUser.ChatRoomAddUser
import com.mightyId.activities.main.ui.message.chatRoom.addUser.ChatRoomAddUser.Companion.LIST_MEMBER_JOINED
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_ID
import com.mightyId.databinding.FragmentChatRoomRemoveUserBinding
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS
import com.mightyId.utils.Constant.Companion.INVITE_USER_INFO
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.adapters.ListTopicMemberAdapter
import com.mightyId.adapters.ListTopicMemberAdapter.Companion.DELETE_TOPIC
import com.mightyId.adapters.ListTopicMemberAdapter.Companion.REMOVE_FROM_TOPIC
import com.mightyId.adapters.ListTopicMemberAdapter.Companion.SEND_MESSAGE
import com.mightyId.adapters.ListTopicMemberAdapter.Companion.VIDEO_CALL
import com.mightyId.adapters.ListTopicMemberAdapter.Companion.VIEW_PROFILE
import com.mightyId.adapters.ListTopicMemberAdapter.Companion.VOICE_CALL
import com.mightyId.callback.CallListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.PersonalChatInfo
import com.mightyId.models.RequestCall
import com.mightyId.models.TopicItem

class ChatRoomListMember : BaseBottomSheetDialogFragment(), View.OnClickListener,
    ListTopicMemberAdapter.ItemClickListener, CallListener, MessageListener {

    private lateinit var binding: FragmentChatRoomRemoveUserBinding
    private lateinit var viewModel: ChatRoomRemoveUserViewModel
    private lateinit var topicId: String

    private val topicMemberAdapter = ListTopicMemberAdapter(this, arrayListOf())
    private var listMemberInTopic = arrayListOf<Account>()
    private var currentFriend = Account()
    private var currentPosition = -1

    companion object {
        const val TAG = "ChatRoomRemoveUser"
        fun newInstance(topicId: String, listMemberInTopic: ArrayList<Account>) =
            ChatRoomListMember().apply {
                arguments = Bundle().apply {
                    putString(TOPIC_ID, topicId)
                    putInfoExtra(LIST_MEMBER_JOINED, listMemberInTopic)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_chat_room_remove_user,
            container,
            false)
        viewModel = ViewModelProvider(this).get(ChatRoomRemoveUserViewModel::class.java)
        binding.listTopicMember.adapter = topicMemberAdapter
        topicId = requireArguments().getString(TOPIC_ID).toString()
        listMemberInTopic = requireArguments().getInfoExtra(LIST_MEMBER_JOINED)

        topicMemberAdapter.update(listMemberInTopic)

        binding.actionAdd.setOnClickListener(this)
        binding.backSpace.setOnClickListener(this)

        obverseViewModel()
        return binding.root
    }

    private fun obverseViewModel() {
//        viewModel.isTopicDeleted.observe(viewLifecycleOwner){
//            if (it==true){
//                Toast.makeText(requireContext(),"Topic deleted",Toast.LENGTH_SHORT).show()
//                val pendingIntent = NavDeepLinkBuilder(requireContext())
//                    .setComponentName(MainActivity::class.java)
//                    .setGraph(R.navigation.mobile_navigation)
//                    .setDestination(R.id.fragment)
//                    .createPendingIntent()
//                pendingIntent.send()
//                dismiss()
//            }
//        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet as View)
            bottomSheetBehavior.isFitToContents = false
            bottomSheetBehavior.halfExpandedRatio = 0.65F
            bottomSheetBehavior.isDraggable = false
        }
        return dialog
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.backSpace -> {
                dismiss()
            }
            binding.actionAdd -> {
                ChatRoomAddUser.newInstance(topicId, listMemberInTopic)
                    .show(childFragmentManager, ChatRoomAddUser.TAG)
                dismiss()
            }
        }
    }

    override fun retrieveUserInfo(account: Account, position: Int) {
        currentFriend = account
        currentPosition = position
    }

    override fun onContextMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            SEND_MESSAGE -> {
                val personalChatInfo = PersonalChatInfo().apply {
                    customerId = currentFriend.customerId
                    customerPhotoUrl = currentFriend.photoUrl
                    customerName = currentFriend.customerName
                }
                val bundle = Bundle().apply {
                    putInfoExtra(ChatRoomActivity.TOPIC_INFO, personalChatInfo)
                }
                moveToChatRoom("private", bundle)
            }
            VOICE_CALL -> {
                initiateMeeting(currentFriend, "audio")
            }
            VIDEO_CALL -> {
                initiateMeeting(currentFriend, "video")
            }
            VIEW_PROFILE -> {
                val intent = Intent(requireContext(), UserDetailActivity::class.java)
                intent.putInfoExtra(INVITE_USER_INFO, currentFriend)
                intent.putExtra(FRIEND_STATUS, Constant.FRIEND_STATUS_ACCEPTED)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
            REMOVE_FROM_TOPIC -> {
                val listMember = arrayListOf<String>()
                listMember.add(currentFriend.customerId!!)
                viewModel.removeUser(topicId, listMember)
            }
            DELETE_TOPIC -> {
                showDeleteTopicDialog()
            }
        }
    }

    private fun showDeleteTopicDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete this topic?")
            .setMessage("Are you sure want to delete this topic?\nWhen topic is deleted, ALL MESSAGE will be erased!")
            .setPositiveButton("Delete") { _, _ ->
                //TODO: HTTP 405 Method Not Allowed
                viewModel.deleteTopic(topicId)
            }.setNegativeButton("Cancel", null)
            .show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
    }

    override fun initiateMeeting(account: Account, type: String) {
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
        val requestCall = RequestCall(Constant.CALL_REQUEST)
        requestCall.callerName = account.customerName
        requestCall.callerPhotoURL = account.photoUrl
        requestCall.callerCustomerId = account.customerId
        requestCall.meetingType = type
        intent.putExtra(Constant.TAG, TAG)
        intent.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, requestCall)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun initiateMeeting(topicItem: TopicItem, type: String) {
    }

    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, 0)
    }
}