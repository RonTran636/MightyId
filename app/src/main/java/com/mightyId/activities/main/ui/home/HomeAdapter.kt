package com.mightyId.activities.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderSuggestingContactBinding
import com.mightyId.utils.Constant
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.getProgressDrawable
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.callback.CallListener
import com.mightyId.callback.ItemClickListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.PersonalChatInfo
import com.mightyId.utils.loadImage

class HomeAdapter(
    private val itemListener: ItemClickListener,
    private val callListener : CallListener,
    private val messageListener: MessageListener,
    private var suggestList: ArrayList<Account>) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    fun update(newSuggestList: ArrayList<Account>) {
        suggestList = newSuggestList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_suggesting_contact, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(suggestList[position], position)
    }

    override fun getItemCount(): Int = suggestList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = HolderSuggestingContactBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))

        fun setData(account: Account, position: Int) {
            if (position == 0) {
                binding.separateLine.visibility = View.GONE
            }
            binding.holderContactAvatarContainer.loadImage(account.photoUrl, progressDrawable)
            binding.holderContactName.text = account.customerName
            binding.holderContactWorkid.text =
                itemView.context.getString(R.string.holder_id, account.workId)
            binding.holderSuggestingContactContainer.setOnClickListener {
                itemListener.onItemClick(account,binding)
            }
            binding.holderActionCall.setOnClickListener {
                callListener.initiateMeeting(account,"audio")
            }
            binding.holderActionVideoCall.setOnClickListener {
                callListener.initiateMeeting(account,"video")
            }
            binding.holderActionMessage.setOnClickListener {
                val bundle = Bundle()
                bundle.putInfoExtra(
                    ChatRoomActivity.TOPIC_INFO,
                    PersonalChatInfo(account.customerId!!,
                    account.customerName,
                    account.photoUrl,
                    null,
                    Constant.FRIEND_STATUS_NEUTRAL)
                )
                messageListener.moveToChatRoom("private",bundle)
            }
        }
    }
}