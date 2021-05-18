package com.mightyId.activities.main.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderContactBinding
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_ACCEPTED
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.getProgressDrawable
import com.mightyId.activities.main.ui.contact.ContactFragment.Companion.listGroupCall
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.callback.CallListener
import com.mightyId.callback.ItemClickListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.PersonalChatInfo
import com.mightyId.utils.loadImage
import timber.log.Timber

class ContactAdapter(
    private val listener: CallListener,
    private val itemClickListener: ItemClickListener,
    private val messageListener: MessageListener,
    private var listContact: MutableList<Account>
) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    fun update(newContact: MutableList<Account>) {
        listContact = newContact
        notifyDataSetChanged()
    }

    fun searchInContact(keyword: String,contactList: MutableList<Account>){
        val temp = arrayListOf<Account>()
        for (item in contactList){
            if (item.customerName!!.contains(keyword,true) || 
                item.workId!!.contains(keyword,true) ||
                item.customerEmail!!.contains(keyword,true)){
                temp.add(item)
                Timber.tag("ContactAdapter").d("searchInContact: Called, temp: $temp")
            }
        }
        listContact = temp
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_contact, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listContact[position].let { holder.setData(it, position) }
    }

    override fun getItemCount(): Int = listContact.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = HolderContactBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))

        fun setData(account: Account, position: Int) {
            if (position == 0) binding.separateLine.visibility = View.GONE
            if (account.photoUrl != null && account.photoUrl!!.isNotEmpty()) {
                binding.holderContactAvatarContainer.loadImage(account.photoUrl, progressDrawable)
            }
            binding.holderContactActive.setImageResource(if (account.isOnline) R.color.green else R.color.bottom_nav_default)
            binding.holderContactName.text = account.customerName
            binding.holderWorkId.text = account.workId

            binding.holderActionVideoCall.setOnClickListener {
                if (listGroupCall.isNullOrEmpty()) { listener.initiateMeeting(account, "video") }
            }
            binding.holderActionCall.setOnClickListener {
                if (listGroupCall.isNullOrEmpty()) { listener.initiateMeeting(account, "audio") }
            }
            binding.holderActionMessage.setOnClickListener{
                val bundle = Bundle()
                bundle.putInfoExtra(
                    ChatRoomActivity.TOPIC_INFO,
                    PersonalChatInfo(
                        account.customerId!!,
                        account.customerName,account.photoUrl,null, FRIEND_STATUS_ACCEPTED)
                )
                if (listGroupCall.isNullOrEmpty()) {
                    messageListener.moveToChatRoom("private",bundle) }
            }
            itemView.setOnLongClickListener {
                itemClickListener.onItemLongClick(account, binding)
                return@setOnLongClickListener true
            }
            itemView.setOnClickListener { itemClickListener.onItemClick(account, binding) }
        }
    }
}