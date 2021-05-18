package com.mightyId.adapters

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderTopicMemberBinding
import com.mightyId.utils.Common
import com.mightyId.utils.getProgressDrawable
import com.mightyId.utils.loadImage
import com.mightyId.models.Account

class ListTopicMemberAdapter(
    private val listener: ItemClickListener,
    private var listMember: MutableList<Account>,
) : RecyclerView.Adapter<ListTopicMemberAdapter.ViewHolder>() {

    companion object {
        const val SEND_MESSAGE = 200
        const val VOICE_CALL = 201
        const val VIDEO_CALL = 203
        const val VIEW_PROFILE = 204
        const val REMOVE_FROM_TOPIC = 205
        const val DELETE_TOPIC = 206
    }

    interface ItemClickListener {
        fun retrieveUserInfo(account: Account, position: Int)
        fun onContextMenuItemSelected(menuItem: MenuItem)
    }

    fun update(newListMember: MutableList<Account>) {
        listMember = newListMember
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_topic_member, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listMember[position].let { holder.setData(it,position) }
    }

    override fun getItemCount(): Int = listMember.size


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnCreateContextMenuListener {
        private var binding = HolderTopicMemberBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))

        fun setData(account: Account, position: Int) {
            if (account.photoUrl != null && account.photoUrl!!.isNotEmpty()) {
                binding.holderFriendAvatar.loadImage(account.photoUrl, progressDrawable)
            }
            binding.holderFriendName.text = account.customerName
            binding.customerId.text = account.customerId
            binding.holderFriendDetail.text = itemView.context.getString(R.string.holder_id,account.customerId)
            binding.holderFriendMenu.setOnCreateContextMenuListener(this)
            binding.holderFriendMenu.setOnClickListener {
                it.showContextMenu(it.x, it.y)
                listener.retrieveUserInfo(account,position)
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?,
        ) {
            val binding = DataBindingUtil.bind<HolderTopicMemberBinding>(itemView)
            if (binding!!.customerId.text == Common.currentAccount!!.customerId) {
                menu.addItemClickListener(DELETE_TOPIC, 0, "Leave and delete topic")
            } else {
                menu.addItemClickListener(SEND_MESSAGE, 0, "Send message")
                menu.addItemClickListener(VOICE_CALL, 1, "Voice call")
                menu.addItemClickListener(VIDEO_CALL, 2, "Video call")
                menu.addItemClickListener(VIEW_PROFILE, 3, "View profile")
                menu.addItemClickListener(REMOVE_FROM_TOPIC, 4, "Remove from topic")
            }
        }
        private fun ContextMenu.addItemClickListener(itemId:Int, order:Int, title: String){
            this.add(this@ViewHolder.adapterPosition, itemId, order, title)
                .setOnMenuItemClickListener{
                listener.onContextMenuItemSelected(it)
                return@setOnMenuItemClickListener true
            }
        }
    }
}