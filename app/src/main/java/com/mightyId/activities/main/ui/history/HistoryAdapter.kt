package com.mightyId.activities.main.ui.history

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderHistoryCallBinding
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_MISSED
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.TimeUtils
import com.mightyId.utils.getProgressDrawable
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.callback.CallListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.*
import com.mightyId.utils.loadImage
import timber.log.Timber

class HistoryAdapter(
    private val userDetail: OnUserDetail,
    private val listener: CallListener,
    private val messageListener: MessageListener,
    private var historyList: MutableList<CallHistoryItems>,
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    interface OnUserDetail {
        fun onUserDetailClick(account: Account)
    }

    fun update(newList: MutableList<CallHistoryItems>) {
        historyList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_history_call, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        historyList[position].let { holder.setData(it) }
    }

    override fun getItemCount(): Int = historyList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = HolderHistoryCallBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))
        private var publicChatInfo = PublicChatInfo()
        private var personalChatInfo = PersonalChatInfo()

        fun setData(callHistory: CallHistoryItems) {
            Timber.tag("ViewHolder").d("setData: Data received: $callHistory")
            binding.holderContactName.text = callHistory.callerName
            binding.holderTimeStatus.text =
                TimeUtils.displayTimeStatus(itemView.context, callHistory.timeCall)
            when (callHistory.topicType) {
                "private" -> {
                    //Private call history:
                    val account = callHistory.convertToAccount()
                    if (callHistory.callerPhotoUrl != null && callHistory.callerPhotoUrl.isNotEmpty()) {
                        binding.holderContactAvatar.loadImage(callHistory.callerPhotoUrl,
                            progressDrawable)
                    }
                    displayCallStatus(callHistory)
                    binding.holderActionCall.setOnClickListener {
                        listener.initiateMeeting(account, "audio")
                    }
                    binding.holderActionVideoCall.setOnClickListener {
                        listener.initiateMeeting(account, "video")
                    }
                    binding.holderActionMessage.setOnClickListener {
                        personalChatInfo.customerId = callHistory.customerId
                        personalChatInfo.customerName = callHistory.callerName
                        personalChatInfo.customerPhotoUrl = callHistory.callerPhotoUrl
                        val bundle = Bundle()
                        bundle.putInfoExtra(ChatRoomActivity.TOPIC_INFO, personalChatInfo)
                        messageListener.moveToChatRoom(callHistory.topicType, bundle)
                    }
                    binding.root.setOnClickListener {
                        userDetail.onUserDetailClick(account)
                    }
                }
                "public" -> {
                    //Topic call history
                    val topicItem = callHistory.convertToTopic()
                    if (callHistory.topicPhoto != null && callHistory.topicPhoto.isNotEmpty()) {
                        binding.holderContactAvatar.loadImage(callHistory.topicPhoto,
                            progressDrawable)
                    }
                    binding.holderContactStatus.visibility = View.GONE
                    binding.holderActionCall.setOnClickListener {
                        listener.initiateMeeting(topicItem, "audio")
                    }
                    binding.holderActionVideoCall.setOnClickListener {
                        listener.initiateMeeting(topicItem, "video")
                    }
                    binding.holderActionMessage.setOnClickListener {
                        publicChatInfo.topicId = callHistory.topicId.toString()
                        publicChatInfo.numberOfParticipants = callHistory.numberOfParticipant!!
                        publicChatInfo.topicName = callHistory.callerName!!
                        publicChatInfo.topicPhotoUrl = callHistory.callerPhotoUrl!!
                        val bundle = Bundle()
                        bundle.putInfoExtra(ChatRoomActivity.TOPIC_INFO, publicChatInfo)
                        messageListener.moveToChatRoom(callHistory.topicType, bundle)
                    }
                    binding.root.setOnClickListener {
                        publicChatInfo.topicId = callHistory.topicId.toString()
                        publicChatInfo.numberOfParticipants = callHistory.numberOfParticipant!!
                        publicChatInfo.topicName = callHistory.callerName!!
                        publicChatInfo.topicPhotoUrl = callHistory.callerPhotoUrl
                        val bundle = Bundle()
                        bundle.putInfoExtra(ChatRoomActivity.TOPIC_INFO, publicChatInfo)
                        messageListener.moveToChatRoom(callHistory.topicType, bundle)
                    }
                }
            }
        }

        private fun CallHistoryItems.convertToAccount(): Account {
            return Account(
                customerName = this.callerName,
                photoUrl = this.callerPhotoUrl,
                customerId = this.customerId
            )
        }

        private fun CallHistoryItems.convertToTopic(): TopicItem {
            return TopicItem(
                topicId = this.topicId.toString(),
                topicPhoto = this.topicPhoto
            )
        }

        private fun displayCallStatus(callHistory: CallHistoryItems) {
            if (callHistory.isRequestCall == true) {
                binding.holderContactStatus.backgroundTintList =
                    ColorStateList.valueOf(binding.root.resources.getColor(R.color.default_green,
                        binding.root.context.theme))
                binding.holderContactStatus.setImageResource(R.drawable.ic_baseline_arrow_back)
                binding.holderContactStatus.rotation = 135F
            } else {
                if (callHistory.callStatus == REMOTE_RESPONSE_MISSED) {
                    binding.holderContactStatus.backgroundTintList =
                        ColorStateList.valueOf(binding.root.resources.getColor(R.color.accent_red,
                            binding.root.context.theme))
                    binding.holderContactStatus.setImageResource(R.drawable.ic_call_missed)
                } else {
                    binding.holderContactStatus.backgroundTintList =
                        ColorStateList.valueOf(binding.root.resources.getColor(R.color.accent_red,
                            binding.root.context.theme))
                    binding.holderContactStatus.setImageResource(R.drawable.ic_baseline_arrow_back)
                    binding.holderContactStatus.rotation = -45F
                }
            }
        }
    }
}