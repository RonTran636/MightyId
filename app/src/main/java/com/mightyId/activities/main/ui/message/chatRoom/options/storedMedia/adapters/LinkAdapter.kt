package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderTopicLinkBinding
import com.mightyId.models.MessageItem
import java.util.*

class LinkAdapter(
    private var listLink: MutableList<MessageItem>,
    private val listener: TopicLinkListener
):RecyclerView.Adapter<LinkAdapter.ViewHolder>() {

    interface TopicLinkListener{
        fun onLinkOptionClick()
        fun onLinkClick(url: String)
    }

    fun update(newList: MutableList<MessageItem>) {
        if (listLink != newList) {
            listLink = newList
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = listLink.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_topic_link, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listLink[position].let { holder.setData(it) }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val binding = HolderTopicLinkBinding.bind(itemView)
        fun setData(item: MessageItem){
            binding.holderLinkSender.text = item.customerName?.capitalize(Locale.getDefault())
            binding.holderLinkUrl.apply {
                text = item.messageContent
                setOnClickListener { listener.onLinkClick(item.messageContent!!) }
            }
            binding.holderActionOption.setOnClickListener { listener.onLinkOptionClick() }
        }
    }
}