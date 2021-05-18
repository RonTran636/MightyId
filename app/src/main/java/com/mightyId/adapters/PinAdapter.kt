package com.mightyId.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderMessagePinBinding
import com.mightyId.models.MessageItem

class PinAdapter(
    private var pinList: ArrayList<MessageItem>,
    private val listener: PinListener
)
    : RecyclerView.Adapter<PinAdapter.ViewHolder>() {

    interface PinListener{
        fun onRemovePin(messageItem: MessageItem)
    }

    fun update(newList: ArrayList<MessageItem>){
        pinList = newList
        notifyDataSetChanged()
    }

    fun removePin(pinMessage : MessageItem){
        for (item in pinList){
            if (item.messageId == pinMessage.messageId){
                notifyItemRemoved(pinList.indexOf(item))
                pinList.remove(pinMessage)
            }
        }
    }

    override fun getItemCount(): Int = pinList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_message_pin, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        pinList[position].let{ holder.setData(it)  }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = HolderMessagePinBinding.bind(itemView)
        fun setData(messageItem: MessageItem){
            binding.pinMessage.text = messageItem.messageContent
            binding.pinRemove.setOnClickListener { listener.onRemovePin(messageItem) }
        }
    }
}