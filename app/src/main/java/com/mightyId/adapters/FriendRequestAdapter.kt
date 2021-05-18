package com.mightyId.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderFriendRequestedBinding
import com.mightyId.utils.getProgressDrawable
import com.mightyId.models.RequestAddFriendModel
import com.mightyId.utils.loadImage

class FriendRequestAdapter(
    private val listener: FriendResponseListener,
    private var list: MutableList<RequestAddFriendModel>
)
    : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    interface FriendResponseListener{
        fun onAccept(requestAddFriend: RequestAddFriendModel)
        fun onReject(requestAddFriend: RequestAddFriendModel)
    }

    fun update(newRequest: MutableList<RequestAddFriendModel>) {
        list = newRequest
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_friend_requested, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(list[position], position)
    }

    override fun getItemCount(): Int  = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = HolderFriendRequestedBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))

        fun setData(requestAddFriend: RequestAddFriendModel, position: Int){
            if (position==0){
                binding.separateLine.visibility = View.GONE
            }
            binding.holderContactAvatarContainer.loadImage(
                requestAddFriend.senderPhoto,
                progressDrawable
            )
            binding.holderContactName.text = requestAddFriend.senderName
            binding.holderMessageDetail.text = requestAddFriend.messageDetail
            binding.holderAccept.setOnClickListener {
                removeAt(position)
                listener.onAccept(requestAddFriend)
            }
            binding.holderReject.setOnClickListener {
                removeAt(position)
                listener.onReject(requestAddFriend)
            }
        }
    }

    fun removeAt(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }
}