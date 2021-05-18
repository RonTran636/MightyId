package com.mightyId.activities.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderSuggestingContactBinding
import com.mightyId.utils.getProgressDrawable
import com.mightyId.callback.CallListener
import com.mightyId.callback.ItemClickListener
import com.mightyId.models.Account
import com.mightyId.utils.loadImage

class SearchContactAdapter(
    private val listener: ItemClickListener,
    private val messageListener: MessageListener,
    private val callListener: CallListener,
    private var suggestList: ArrayList<Account>
) : RecyclerView.Adapter<SearchContactAdapter.ViewHolder>() {

    interface MessageListener {
        fun moveToChatRoom(account: Account)
    }

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
                listener.onItemClick(account, binding)
            }
            binding.holderActionCall.setOnClickListener {
                callListener.initiateMeeting(account, "audio")
            }
            binding.holderActionVideoCall.setOnClickListener {
                callListener.initiateMeeting(account, "video")
            }
            binding.holderActionMessage.setOnClickListener {
                messageListener.moveToChatRoom(account)
            }
        }
    }
}