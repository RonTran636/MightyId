package com.mightyId.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderListParticipantBinding
import com.mightyId.utils.getProgressDrawable
import com.mightyId.utils.loadImage
import com.mightyId.models.Account

class ListParticipantAdapter(
    private val listener : OnRemoveParticipant,
    private var listContact: MutableList<Account>)
    : RecyclerView.Adapter<ListParticipantAdapter.ViewHolder>() {

    interface OnRemoveParticipant{
        fun onRemoveParticipant(account: Account)
    }

    fun update(newContact: MutableList<Account>) {
        listContact = newContact
        notifyDataSetChanged()
    }

    fun addMember(account: Account){
        listContact.add(account)
        notifyItemInserted(listContact.size -1)
    }

    fun removeMember(account: Account){
        notifyItemRemoved(listContact.indexOf(account))
        listContact.remove(account)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_list_participant, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listContact[position].let { holder.setData(it) }
    }

    override fun getItemCount(): Int = listContact.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = HolderListParticipantBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))

        fun setData(account: Account) {
            if (account.photoUrl != null && account.photoUrl!!.isNotEmpty()) {
                binding.holderAvatarContainer.loadImage(account.photoUrl, progressDrawable)
            }
            binding.holderName.text = account.customerName
            binding.holderCancel.setOnClickListener{ listener.onRemoveParticipant(account) }
        }
    }
}