package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.models.MessageItem
import com.mightyId.utils.loadImage
import timber.log.Timber

class MediaAdapter(
    private var listMedia: MutableList<MessageItem>,
    private val itemListener: ItemClickListener,
) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(imageName: String, imageUrl: String)
    }

    fun update(newList: MutableList<MessageItem>) {
        if (listMedia != newList) {
            listMedia = newList
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = ImageView(parent.context)
        val desireWidth = parent.width / 4
        imageView.apply {
            layoutParams = ViewGroup.LayoutParams(desireWidth, desireWidth)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(8, 8, 8, 8)
        }
        return ViewHolder(imageView)
    }

    override fun getItemCount(): Int = listMedia.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listMedia[position].let { holder.setData(it) }
    }

    inner class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView
        fun setData(messageItem: MessageItem) {
            Timber.tag("ViewHolder").d("setData: ${messageItem.link}")
            imageView.loadImage(messageItem.link, null)
            imageView.setOnClickListener {
                itemListener.onItemClick(messageItem.messageContent!!,
                    messageItem.link.toString())
            }
        }
    }
}