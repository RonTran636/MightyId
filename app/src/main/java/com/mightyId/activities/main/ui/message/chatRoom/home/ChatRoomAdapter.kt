package com.mightyId.activities.main.ui.message.chatRoom.home

import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mightyId.R
import com.mightyId.databinding.RowChatNotifyBinding
import com.mightyId.databinding.RowChatPartnerBinding
import com.mightyId.databinding.RowChatTodoBinding
import com.mightyId.databinding.RowChatUserBinding
import com.mightyId.utils.*
import com.mightyId.utils.TimeUtils.convertToDay
import com.mightyId.utils.TimeUtils.convertToHour
import com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters.MediaAdapter
import com.mightyId.models.MessageItem
import com.mightyId.models.server.ElementsFromHtml
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jsoup.nodes.Document
import timber.log.Timber
import java.util.*

class ChatRoomAdapter(
    private val context: Context,
    private var chatList: MutableList<MessageItem>,
    private var itemClickListener: MediaAdapter.ItemClickListener,
    private val itemMessageListener: ItemMessageListener,
) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
        const val TYPE_VIDEO = "video"
        const val TYPE_AUDIO = "audio"
        const val TYPE_FILE = "file"
        const val TYPE_LINK = "link"
        const val TYPE_NOTIFY = "notify"
        const val TYPE_DELETE = "delete"
        const val TYPE_EDIT = "edit"
        const val TYPE_TODO = "todo"

        const val chatMine = 0
        const val chatPartner = 1
        const val chatNotify = 2
        const val chatTodo = 3

        const val TODO_PENDING = "pending"
        const val TODO_FAILED = "fail"
        const val TODO_CONFIRM = "confirm"
        const val TODO_COMPLETE = "complete"
        const val TODO_REJECT = "reject"
    }

    interface ItemMessageListener {
        fun onMessageLongClick(messageId: Int, messageType: String, messageViewType: Int)
        fun onMessageOpenWebUrl(url: String?)
        fun onMessageFileDownload(fileName: String, url: String)
    }

    fun update(newChatList: MutableList<MessageItem>) {
        chatList.addAll(newChatList)
        notifyItemInserted(this.itemCount)
    }

    fun addMessage(message: MessageItem) {
        chatList.add(0, message)
        notifyItemInserted(0)
    }

    fun removeMessage(messageId: Int) {
        for (item in chatList) {
            if (item.messageId == messageId) {
                item.messageContent = context.getString(R.string.message_deleted)
                item.messageContentType = TYPE_DELETE
                notifyItemChanged(chatList.indexOf(item))
            }
        }
    }

    fun editMessage(messageId: Int, messageContent: String) {
        for (item in chatList) {
            if (item.messageId == messageId) {
                item.messageContent = messageContent
                item.messageContentType = TYPE_EDIT
                notifyItemChanged(chatList.indexOf(item))
            }
        }
    }

    fun findMessage(messageId: Int): MessageItem? {
        for (item in chatList) {
            if (item.messageId == messageId) {
                return item
            }
        }
        return null
    }

    fun updateMessage(messageItem: MessageItem) {
        chatList[0] = messageItem
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        when (chatList[position].messageContentType) {
            TYPE_NOTIFY -> {
                chatList[position].viewType = chatNotify
            }
            TYPE_TODO -> {
                chatList[position].viewType = chatTodo
            }
            else -> {
                if (chatList[position].customerId == Common.currentAccount!!.customerId) {
                    chatList[position].viewType = chatMine
                } else {
                    chatList[position].viewType = chatPartner
                }
            }
        }
        return chatList[position].viewType!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var layout: Int? = null
        when (viewType) {
            chatMine -> layout = R.layout.row_chat_user
            chatPartner -> layout = R.layout.row_chat_partner
            chatNotify -> layout = R.layout.row_chat_notify
            chatTodo -> layout = R.layout.row_chat_todo
        }
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout!!, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        chatList[position].let { holder.setData(it, position) }
    }

    override fun getItemCount(): Int = chatList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(messageItem: MessageItem, position: Int) {
            when (messageItem.viewType) {
                chatMine -> {
                    //Message from user
                    val binding: RowChatUserBinding = DataBindingUtil.bind(itemView)!!
                    //Check whether this message has parent
                    if (messageItem.hasParent) {
                        binding.replyBox.visibility = View.VISIBLE
                        binding.replyUser.text = messageItem.messageParent?.customerName
                    } else {
                        binding.replyBox.visibility = View.GONE
                        binding.replyImage.visibility = View.GONE
                    }
                    if (messageItem.timeSent!!.length > 19) {
                        binding.userTimeSend.text =
                            messageItem.timeSent?.convertToHour(binding.root.context)
                    }
                    binding.userMessageContent.text = messageItem.messageContent
                    when (messageItem.messageContentType) {
                        TYPE_IMAGE -> {
                            binding.replyImage.visibility = View.VISIBLE
                            binding.replyMessage.text =
                                itemView.context.getString(R.string.reply_picture)
                            binding.replyImage.loadImage(messageItem.link, null)
                            binding.textLayout.visibility = View.GONE
                            binding.imageLayout.visibility = View.VISIBLE
                            binding.imageContent.loadImage(messageItem.link, null)
                            binding.imageLayout.setOnClickListener {
                                itemClickListener.onItemClick(messageItem.messageContent!!,
                                    messageItem.link.toString())
                            }
                            binding.imageLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!)
                                return@setOnLongClickListener true
                            }
                        }
                        TYPE_TEXT -> {
                            binding.replyMessage.text = messageItem.messageParent?.messageContent
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                        }

                        TYPE_EDIT -> {
                            binding.replyMessage.text = messageItem.messageParent?.messageContent
                            binding.isEdited.visibility = View.VISIBLE
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                        }
                        TYPE_FILE -> {
                            binding.replyMessage.text = messageItem.messageParent?.messageContent
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                            binding.userMessageContent.underline()
                            binding.userMessageContent.makeLinks(Pair(messageItem.messageContent!!,
                                object : View.OnClickListener {
                                    override fun onClick(v: View?) {
                                        itemMessageListener.onMessageFileDownload(messageItem.messageContent!!,
                                            messageItem.link!!)
                                    }
                                }))
                        }
                        TYPE_LINK -> {
                            binding.userMessageContent.maxLines = 2
                            binding.userMessageContent.ellipsize = TextUtils.TruncateAt.END
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                            binding.materialCardView.visibility = View.VISIBLE
                            binding.userMessageContent.makeLinks(Pair(messageItem.messageContent!!,
                                object : View.OnClickListener {
                                    override fun onClick(v: View?) {
                                        itemMessageListener.onMessageOpenWebUrl(messageItem.messageContent!!)
                                    }
                                }))
                            binding.textLayout.setOnClickListener {
                                itemMessageListener.onMessageOpenWebUrl(messageItem.messageContent!!)
                            }
                            getHTMLContent(messageItem.messageContent!!)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ result ->
                                    if (result != null) {
                                        val item = retrieveDataFromHTML(result)
                                        if (!item.title.isNullOrEmpty()) {
                                            binding.previewUrlTitle.text = item.title
                                        } else if (!item.ogTitle.isNullOrEmpty()) {
                                            binding.previewUrlTitle.text = item.title
                                        } else {
                                            binding.previewUrlTitle.visibility = View.GONE
                                        }

                                        if (item.ogDescription.isNullOrEmpty()) {
                                            binding.previewUrlDescription.visibility = View.GONE
                                        } else {
                                            binding.previewUrlDescription.text =
                                                item.ogDescription
                                        }

                                        if (!item.imageUrl.isNullOrEmpty()) {
                                            binding.previewUrlLayout.loadImage(item.imageUrl!!,
                                                null)
                                        }

                                        if (item.siteName.isNullOrEmpty()) {
                                            binding.previewUrl.visibility = View.GONE
                                        } else {
                                            binding.previewUrl.text = item.siteName
                                        }
                                    }

                                }, { throwable ->
                                    Timber.tag("ViewHolder").e("setData: error :$throwable")
                                })
                        }
                        TYPE_DELETE -> {
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.userMessageContent.setTextColor(
                                ColorStateList.valueOf(binding.root.resources.getColor(R.color.grey_af,
                                    binding.root.context.theme))
                            )
                            binding.userTimeSend.setTextColor(
                                ColorStateList.valueOf(binding.root.resources.getColor(R.color.grey_af,
                                    binding.root.context.theme))
                            )
                        }
                    }
                    displayUserTimeSent(binding, position)
                }
                chatPartner -> {
                    //Message from others but current user
                    val binding = DataBindingUtil.bind<RowChatPartnerBinding>(itemView)!!
                    //Check whether this message has parent
                    if (messageItem.hasParent) {
                        binding.replyBox.visibility = View.VISIBLE
                        binding.replyUser.text = messageItem.messageParent!!.customerName
                    }
                    Glide.with(itemView.context).clear(binding.partnerAvatar)
                    binding.partnerName.text = messageItem.customerName
                    if (messageItem.timeSent!!.length > 19) {
                        binding.partnerTimeSent.text =
                            messageItem.timeSent?.convertToHour(binding.root.context)
                    }
                    binding.partnerMessageContent.text = messageItem.messageContent
                    when (messageItem.messageContentType) {
                        TYPE_IMAGE -> {
                            binding.replyImage.setImageDrawable(null)
                            binding.imageContent.setImageDrawable(null)
                            binding.textLayout.visibility = View.GONE
                            binding.imageLayout.visibility = View.VISIBLE
                            binding.replyImage.visibility = View.VISIBLE
                            binding.replyMessage.text =
                                itemView.context.getString(R.string.reply_picture)
                            binding.replyImage.loadImage(messageItem.link, null)
                            binding.imageContent.loadImage(messageItem.link, null)
                            binding.imageLayout.setOnClickListener {
                                itemClickListener.onItemClick(messageItem.messageContent!!,
                                    messageItem.link.toString())
                            }
                            binding.imageLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                        }
                        TYPE_TEXT -> {
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.replyMessage.text = messageItem.messageParent?.messageContent
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                        }
                        TYPE_EDIT -> {
                            binding.replyMessage.text = messageItem.messageParent?.messageContent
                            binding.isEdited.visibility = View.VISIBLE
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                        }
                        TYPE_LINK -> {
                            binding.partnerMessageContent.maxLines = 2
                            binding.partnerMessageContent.ellipsize = TextUtils.TruncateAt.END
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                            if (messageItem.messageContentType == TYPE_LINK) {
                                binding.materialCardView.visibility = View.VISIBLE
                                binding.partnerMessageContent.makeLinks(Pair(messageItem.messageContent!!,
                                    object : View.OnClickListener {
                                        override fun onClick(v: View?) {
                                            itemMessageListener.onMessageOpenWebUrl(messageItem.messageContent!!)
                                        }
                                    }))
                                binding.textLayout.setOnClickListener {
                                    itemMessageListener.onMessageOpenWebUrl(messageItem.messageContent!!)
                                }
                                getHTMLContent(messageItem.messageContent!!)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ result ->
                                        if (result != null) {
                                            val item = retrieveDataFromHTML(result)
                                            if (!item.title.isNullOrEmpty()) {
                                                binding.previewUrlTitle.text = item.title
                                            } else if (!item.ogTitle.isNullOrEmpty()) {
                                                binding.previewUrlTitle.text = item.title
                                            } else {
                                                binding.previewUrlTitle.visibility = View.GONE
                                            }
                                            if (!item.imageUrl.isNullOrEmpty()) {
                                                binding.previewUrlLayout.loadImage(item.imageUrl!!,
                                                    null)
                                            }

                                            if (item.ogDescription.isNullOrEmpty()) {
                                                binding.previewUrlDescription.visibility = View.GONE
                                            } else {
                                                binding.previewUrlDescription.text =
                                                    item.ogDescription
                                            }

                                            if (item.siteName.isNullOrEmpty()) {
                                                binding.previewUrlHostName.visibility = View.GONE
                                            } else {
                                                binding.previewUrlHostName.text = item.siteName
                                            }
                                        }
                                    }, { throwable ->
                                        Timber.tag("ViewHolder").e("setData: error :$throwable")
                                    })
                            }
                        }
                        TYPE_DELETE -> {
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.partnerMessageContent.text =
                                binding.root.context.getString(R.string.message_deleted)
                            binding.partnerMessageContent.setTextColor(
                                ColorStateList.valueOf(binding.root.resources.getColor(R.color.grey_af,
                                    binding.root.context.theme))
                            )
                            binding.partnerMessageContent.setTextColor(
                                ColorStateList.valueOf(binding.root.resources.getColor(R.color.grey_af,
                                    binding.root.context.theme))
                            )
                        }
                        TYPE_FILE -> {
                            binding.replyMessage.text = messageItem.messageParent?.messageContent
                            binding.textLayout.visibility = View.VISIBLE
                            binding.imageLayout.visibility = View.GONE
                            binding.materialCardView.visibility = View.GONE
                            binding.textLayout.setOnLongClickListener {
                                itemMessageListener.onMessageLongClick(
                                    messageItem.messageId!!,
                                    messageItem.messageContentType!!,
                                    messageItem.viewType!!
                                )
                                return@setOnLongClickListener true
                            }
                            binding.partnerMessageContent.underline()
                            binding.partnerMessageContent.makeLinks(Pair(messageItem.messageContent!!,
                                object : View.OnClickListener {
                                    override fun onClick(v: View?) {
                                        itemMessageListener.onMessageFileDownload(messageItem.messageContent!!,
                                            messageItem.link!!)
                                    }
                                }))
                        }
                    }
                    if (messageItem.photoUrl != null && messageItem.photoUrl!!.isNotEmpty()) {
                        binding.partnerAvatar.setImageDrawable(null)
                        binding.partnerAvatar.loadImage(messageItem.photoUrl, null)
                    }
                    //Display partner's avatar and name
                    when {
                        chatList.size == 1 || position == chatList.size - 1 -> {
                            Timber.tag("ChatRoomAdapter")
                                .d("setData: chatList.size == 1 || position == chatList.size - 1  :$position")
                            binding.partnerAvatar.visibility = View.VISIBLE
                            binding.partnerName.visibility = View.VISIBLE
                        }
                        getItemViewType(position + 1) == chatMine -> {
                            Timber.tag("ChatRoomAdapter")
                                .d("setData: getItemViewType(position + 1) == chatMine :$position")
                            binding.partnerAvatar.visibility = View.VISIBLE
                            binding.partnerName.visibility = View.VISIBLE
                        }
                        else -> {
                            if (chatList[position + 1].customerName == chatList[position].customerName) {
                                if (getItemViewType(position + 1) != chatNotify) {
                                    binding.partnerAvatar.visibility = View.GONE
                                    binding.partnerName.visibility = View.GONE
                                }
                            }
                        }
                    }
                    displayPartnerTimeSent(binding, position)
                }
                chatNotify -> {
                    val binding: RowChatNotifyBinding = DataBindingUtil.bind(itemView)!!
                    binding.notifyMessage.text = messageItem.messageContent
                }
                chatTodo -> {
                    val binding: RowChatTodoBinding = DataBindingUtil.bind(itemView)!!
                    binding.todoStatus.text = messageItem.messageContent
                    binding.todoTitle.text = messageItem.todo!!.todoTitle
                    binding.todoStatusText.text =
                        messageItem.todo!!.todoStatus.capitalize(Locale.getDefault())
                    binding.todoStatusIcon.displayTodoIcon(messageItem.todo!!.todoStatus)
                    if (messageItem.fileAttach == null) {
                        binding.materialTextView14.visibility = View.GONE
                        binding.todoAttach.visibility = View.GONE
                    } else {
                        binding.todoAttach.text = messageItem.fileAttach!![0].fileName
                        binding.todoAttach.makeLinks(Pair(messageItem.fileAttach!![0].fileName,
                            View.OnClickListener {

                            }))
                    }
                    binding.todoAssignee.text = messageItem.todo!!.listAssigneeName
                    binding.todoDeadline.text =
                        messageItem.todo!!.todoDeadline.convertToDay(itemView.context)
                }
            }
        }
    }

    private fun displayPartnerTimeSent(binding: RowChatPartnerBinding, position: Int) {
        when {
            position == 0 || chatList[position - 1].viewType == chatMine -> {
                binding.partnerTimeSent.visibility = View.VISIBLE
            }
            else -> {
                if (chatList[position - 1].customerName != chatList[position].customerName) {
                    binding.partnerTimeSent.visibility = View.VISIBLE
                } else {
                    binding.partnerTimeSent.visibility = View.GONE
                }
            }
        }
    }

    private fun displayUserTimeSent(binding: RowChatUserBinding, position: Int) {
        when {
            position == 0 || chatList[position - 1].viewType == chatPartner -> {
                binding.userTimeSend.visibility = View.VISIBLE
            }
            else -> {
                binding.userTimeSend.visibility = View.GONE
            }
        }
    }

    private fun retrieveDataFromHTML(document: Document): ElementsFromHtml {
        val dataFromHtml = ElementsFromHtml()
        val metaTags = document.getElementsByTag("meta")
        for (item in metaTags) {
            when {
                item.attr("property").equals("og:image") -> {
                    dataFromHtml.imageUrl = item.attr("content")
                }
                item.attr("property").equals("title") -> {
                    dataFromHtml.ogTitle = item.attr("content")
                }
                item.attr("name").equals("title") -> {
                    dataFromHtml.title = item.attr("content")
                }
                item.attr("name").equals("og:description") -> {
                    dataFromHtml.ogDescription = item.attr("content")
                }
                item.attr("property").equals("og:site_name") -> {
                    dataFromHtml.siteName = item.attr("content")
                }
                item.attr("property").equals("og:url") -> {
                    dataFromHtml.ogSiteUrl = item.attr("content")
                }
            }
        }
        return dataFromHtml
    }
}