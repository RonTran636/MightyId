package com.mightyId.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderAddParticipantBinding
import com.mightyId.utils.getProgressDrawable
import com.mightyId.callback.ItemClickListener
import com.mightyId.models.Account
import com.mightyId.utils.loadImage

class AddParticipantAdapter(
    private val itemClickListener: ItemClickListener,
    private var listContact: ArrayList<Account>
) : RecyclerView.Adapter<AddParticipantAdapter.ViewHolder>() {

    fun update(newContact: ArrayList<Account>) {
        listContact = newContact
        notifyDataSetChanged()
    }

    fun removeDuplicate(duplicateList: MutableList<Account>){
        for (item in listContact){
            for (duplicateItem in duplicateList){
                if (item.customerId == duplicateItem.customerId){
                    listContact.remove(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun removeAll(){
        listContact = arrayListOf()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_add_participant, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listContact[position].let { holder.setData(it) }
    }

    override fun getItemCount(): Int = listContact.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding = HolderAddParticipantBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))

        fun setData(account: Account) {
            if (account.photoUrl != null && account.photoUrl!!.isNotEmpty()) {
                binding.holderContactAvatarContainer.loadImage(account.photoUrl, progressDrawable)
            }
            binding.holderContactName.text = account.customerName
            binding.checkbox.setOnClickListener { itemClickListener.onItemClick(account, binding) }
            binding.root.setOnClickListener { itemClickListener.onItemClick(account, binding) }
        }
    }
}