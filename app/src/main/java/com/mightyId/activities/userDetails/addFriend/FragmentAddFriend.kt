package com.mightyId.activities.userDetails.addFriend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.databinding.FragmentAddFriendBinding
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS
import com.mightyId.utils.Constant.Companion.USER_INFO
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.hideKeyboard
import com.mightyId.callback.CallListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.*
import com.mightyId.utils.loadImage
import com.mightyId.utils.showKeyboard
import timber.log.Timber
import kotlin.properties.Delegates

class FragmentAddFriend : BaseBottomSheetDialogFragment(),View.OnClickListener, CallListener,
    MessageListener {

    private lateinit var binding: FragmentAddFriendBinding
    private lateinit var viewRoot: View
    private val viewModel: FragmentAddFriendViewModel by viewModels()
    private var friendStatus by Delegates.notNull<Int>()
    private lateinit var userInfo : Account
    private lateinit var listener : UpdateFriendStatus

    companion object{
        fun newInstance(userInfo: Account, friendStatus: Int) =
            FragmentAddFriend().apply {
            arguments = Bundle().apply {
                putInt(FRIEND_STATUS,friendStatus)
                putInfoExtra(USER_INFO,userInfo)
            }
        }
        const val TAG = "FragmentAddFriend"
    }

    interface UpdateFriendStatus{
        fun onUpdateFriendStatus()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as UpdateFriendStatus
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_friend,container,false)
        viewRoot = binding.root
        userInfo = arguments?.getInfoExtra(USER_INFO)!!
        friendStatus = arguments?.getInt(FRIEND_STATUS)!!
        //Update UI with given user info:
        binding.outgoingCallerAvatar.loadImage(userInfo.photoUrl,null)
        binding.addFriendName.text = userInfo.customerName
        binding.addFriendWorkId.text = getString(R.string.holder_id,userInfo.workId)
        binding.requestFriendMessage.apply {
            showKeyboard()
            requestFocus()
        }
        binding.actionSendFriendRequest.setOnClickListener(this)
        binding.backSpace.setOnClickListener(this)
        binding.holderActionCall.setOnClickListener(this)
        binding.holderActionVideoCall.setOnClickListener(this)
        binding.holderActionMessage.setOnClickListener(this)
        binding.actionSendFriendRequest.setOnClickListener(this)

        return viewRoot
    }

    override fun onClick(v: View?) {
        when (v){
            binding.backSpace-> { dismiss() }
            binding.actionSendFriendRequest-> {
                val remoteMsgItem = RequestAddFriendModel(Constant.ADD_FRIEND)
                remoteMsgItem.senderId = userInfo.customerId
                remoteMsgItem.messageDetail = binding.requestFriendMessage.text.toString()
                Timber.tag("UserDetailActivity").d("onClick: $remoteMsgItem")
                viewModel.sendFriendRequest(remoteMsgItem)
                listener.onUpdateFriendStatus()
                dismiss()
            }
            binding.holderActionCall-> { initiateMeeting(userInfo,"audio") }

            binding.holderActionVideoCall->{ initiateMeeting(userInfo,"video")}

            binding.holderActionMessage-> {
                val bundle = Bundle()
                bundle.putInfoExtra(
                    ChatRoomActivity.TOPIC_INFO,
                    PersonalChatInfo(
                        userInfo.customerId!!,
                        userInfo.customerName,
                        userInfo.photoUrl,null,
                        friendStatus)
                )
                moveToChatRoom("private",bundle) }
        }
    }

    override fun dismiss() {
        requireActivity().hideKeyboard()
        super.dismiss()
    }

    override fun onDestroy() {
        requireActivity().hideKeyboard()
        super.onDestroy()
    }

    override fun initiateMeeting(account: Account, type: String) {
        Timber.tag("FragmentAddFriend").d("initiateMeeting: Data waiting to send: $account")
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
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

    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE,chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right,0)
    }
}