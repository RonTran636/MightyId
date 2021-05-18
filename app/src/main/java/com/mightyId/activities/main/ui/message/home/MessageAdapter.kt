package com.mightyId.activities.main.ui.message.home

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.databinding.HolderListMessageBinding
import com.mightyId.models.MessageItem
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.TimeUtils.convertToHour
import com.mightyId.utils.getProgressDrawable
import com.mightyId.callback.MessageListener
import com.mightyId.models.PersonalChatInfo
import com.mightyId.models.PublicChatInfo
import com.mightyId.models.TopicItem
import com.mightyId.utils.loadImage
import java.util.*

class MessageAdapter(
    private val itemClickListener: ItemClickListener,
    private val topicListener: MessageListener,
    private var list: MutableList<TopicItem>,
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    companion object {
        const val ACTION_PIN = 101
        const val ACTION_UNPIN = 102
        const val ACTION_ARCHIVE = 103
        const val ACTION_MARK = 104
        const val ACTION_DELETE_CONVERSATION = 106
        const val ACTION_MUTE_NOTIFICATION = 107
    }

    interface ItemClickListener {
        fun retrieveUserInfo(binding: HolderListMessageBinding, topicItem: TopicItem, position: Int)
        fun onContextMenuItemSelected(menuItem: MenuItem)
        fun isUnReadChatRoom()
    }

    fun update(newRequest: MutableList<TopicItem>) {
        list = newRequest
        notifyDataSetChanged()
    }

    fun onNewMessage(messageItem: MessageItem): Boolean {
        for (item in list) {
            if (messageItem.topicId == item.topicId) {
                item.lastMessage = messageItem.messageContent
                item.lastMessageTime = messageItem.timeSent
                if (messageItem.customerId != Common.currentAccount!!.customerId) item.messageUnread++
                Collections.swap(list, list.indexOf(item), 0)
                notifyDataSetChanged()
                if (item.messageUnread == 1) return true
            }
        }
        return false
    }

    fun archiveTopic(topicItem: TopicItem) {
        notifyItemRemoved(list.indexOf(topicItem))
        list.remove(topicItem)

    }

    fun reverseReadState(position: Int) {
        list[position].isRead = !list[position].isRead
        notifyItemChanged(position)
    }

    fun removeAll() {
        list = mutableListOf()
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_list_message, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = HolderListMessageBinding.bind(itemView)
        private val progressDrawable = getProgressDrawable((itemView.context))
        private var personalChatInfo = PersonalChatInfo()
        private var publicChatInfo = PublicChatInfo()

        fun setData(topicItem: TopicItem, position: Int) {
            binding.holderMessageAvatar.loadImage(topicItem.topicPhoto, progressDrawable)
            binding.holderMessageName.text = topicItem.topicName
            binding.holderMessageDetail.text = topicItem.lastMessage
            binding.holderMessageTime.text =
                topicItem.lastMessageTime?.convertToHour(itemView.context)
            binding.isPinned.visibility = if (topicItem.isTopicPinned) View.VISIBLE else View.GONE
            binding.holderMessagePrivate.visibility =
                if (topicItem.topicType == Constant.PRIVACY_PRIVATE) View.VISIBLE else View.INVISIBLE
            binding.messageUnread.text =
                binding.root.context.getString(
                    R.string.number_unread_message,
                    topicItem.messageUnread
                )
            topicItem.isRead = topicItem.messageUnread <= 0
            if (topicItem.isRead) {
                binding.holderMessageDetail.apply {
                    typeface = Typeface.DEFAULT
                    setTextColor(
                        ColorStateList.valueOf(
                            binding.root.resources.getColor(
                                R.color.grey_af,
                                binding.root.context.theme
                            )
                        )
                    )
                }
                binding.holderMessageName.typeface = Typeface.DEFAULT
                binding.holderMessageTime.typeface = Typeface.DEFAULT
                binding.messageUnread.visibility = View.GONE
            } else {
                binding.holderMessageDetail.apply {
                    setTextColor(
                        ColorStateList.valueOf(
                            binding.root.resources.getColor(
                                R.color.primary_color,
                                binding.root.context.theme
                            )
                        )
                    )
                    typeface = Typeface.DEFAULT_BOLD
                }
                binding.holderMessageName.typeface = Typeface.DEFAULT_BOLD
                binding.holderMessageTime.typeface = Typeface.DEFAULT_BOLD
                binding.messageUnread.visibility = View.VISIBLE
            }
            binding.root.setOnClickListener {
                topicListener.moveToChatRoom(
                    topicItem.topicType!!,
                    topicItem.convertDataForChatRoom()
                )
                if (!topicItem.isRead) itemClickListener.isUnReadChatRoom()
            }
            binding.root.setOnCreateContextMenuListener { menu, _, _ ->
                if (topicItem.isTopicPinned) {
                    menu.addItemClickListener(ACTION_UNPIN, 0, "Unpin")
                } else {
                    menu.addItemClickListener(ACTION_PIN, 0, "Pin")
                }
                menu.addItemClickListener(ACTION_ARCHIVE, 1, "Hide")
//                if (topicItem.isRead) {
//                    menu.addItemClickListener(ACTION_MARK, 2, "Mark as unread")
//                } else {
//                    menu.addItemClickListener(ACTION_MARK, 2, "Mark as read")
//                }
//                menu.addItemClickListener(ACTION_DELETE_CONVERSATION, 3, "Delete conversation")
//                menu.addItemClickListener(ACTION_MUTE_NOTIFICATION, 4, "Mute")
            }
            binding.root.setOnLongClickListener {
                it.showContextMenu()
                itemClickListener.retrieveUserInfo(binding, topicItem, position)
                return@setOnLongClickListener true
            }
        }

        private fun TopicItem.convertDataForChatRoom(): Bundle {
            when (this.topicType) {
                "private" -> {
                    personalChatInfo.customerName = this.topicName
                    personalChatInfo.topicId = this.topicId
                    personalChatInfo.isMessagePinned = this.isTopicPinned
                    personalChatInfo.customerPhotoUrl = this.topicPhoto
                    personalChatInfo.listCustomerId = this.listCustomerId
                    val bundle = Bundle()
                    bundle.putInfoExtra(ChatRoomActivity.TOPIC_INFO, personalChatInfo)
                    return bundle
                }
                else -> {
                    publicChatInfo.topicName = this.topicName
                    publicChatInfo.topicId = this.topicId
                    publicChatInfo.isTopicPinned = this.isTopicPinned
                    publicChatInfo.topicPhotoUrl = this.topicPhoto
                    publicChatInfo.numberOfParticipants = this.numberOfParticipant
                    val bundle = Bundle()
                    bundle.putInfoExtra(ChatRoomActivity.TOPIC_INFO, publicChatInfo)
                    return bundle
                }
            }
        }

        private fun ContextMenu.addItemClickListener(itemId: Int, order: Int, title: String) {
            this.add(this@ViewHolder.absoluteAdapterPosition, itemId, order, title)
                .setOnMenuItemClickListener {
                    itemClickListener.onContextMenuItemSelected(it)
                    return@setOnMenuItemClickListener true
                }
        }
    }
}