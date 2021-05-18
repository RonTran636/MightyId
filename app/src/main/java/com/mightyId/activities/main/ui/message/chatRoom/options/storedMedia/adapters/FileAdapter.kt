package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.databinding.HolderTopicFileBinding
import com.mightyId.models.MessageItem
import java.util.*

class FileAdapter(
    private var listFile: MutableList<MessageItem>,
    private var listener: FileListener,
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    interface FileListener {
        fun onFileDownload(fileName: String, fileUrl: String)
    }

    fun update(newList: MutableList<MessageItem>) {
        if (listFile != newList) {
            listFile = newList
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.holder_topic_file, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        listFile[position].let { holder.setData(it) }
    }

    override fun getItemCount(): Int = listFile.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = HolderTopicFileBinding.bind(itemView)
        fun setData(fileItem: MessageItem) {
            binding.holderFileName.apply {
                text = fileItem.messageContent
                setOnClickListener{ listener.onFileDownload(fileItem.messageContent!!, fileItem.link!!) }
            }
            binding.holderFileSender.text = fileItem.customerName?.capitalize(Locale.getDefault())
            binding.holderActionSave.setOnClickListener {
                listener.onFileDownload(fileItem.messageContent!!, fileItem.link!!)
            }
        }
    }
}