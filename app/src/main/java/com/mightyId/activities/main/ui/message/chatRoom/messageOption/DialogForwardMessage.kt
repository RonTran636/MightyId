package com.mightyId.activities.main.ui.message.chatRoom.messageOption

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.databinding.DialogForwardMessageBinding
import com.mightyId.databinding.HolderForwardMessageBinding
import com.mightyId.models.MessageItem
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.activities.main.ui.message.home.MessageViewModel
import com.mightyId.models.TopicItem
import com.mightyId.utils.loadImage

class DialogForwardMessage : BaseBottomSheetDialogFragment(),
    ForwardMessageAdapter.ItemClickListener {

    private lateinit var binding: DialogForwardMessageBinding
    private lateinit var messageItem: MessageItem
    private lateinit var messageViewModel: MessageViewModel

    private var listRecentTopic = mutableListOf<TopicItem>()
    private val forwardMessageAdapter = ForwardMessageAdapter(this, arrayListOf())

    companion object {
        const val TAG = "DialogMessageOption"
        private const val MESSAGE_ITEM = "messageItem"

        @JvmStatic
        fun newInstance(messageItem: MessageItem) =
            DialogForwardMessage().apply {
                arguments = Bundle().apply {
                    putInfoExtra(MESSAGE_ITEM, messageItem)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_forward_message, container, false)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        messageItem = arguments?.getInfoExtra(MESSAGE_ITEM)!!

        binding.forwardUser.text = messageItem.customerName
        binding.forwardMessage.text = messageItem.messageContent
        binding.resultList.adapter = forwardMessageAdapter
        binding.backSpace.setOnClickListener{ dismiss() }
        messageViewModel.getRecentMessage(1)
        messageViewModel.configureAutoComplete()
        handleSearchEvent()
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        messageViewModel.listMessage.observe(viewLifecycleOwner) {
            forwardMessageAdapter.update(it.result)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (forwardMessageAdapter.itemCount == 0) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
            }
        }
        messageViewModel.isMessageSent.observe(viewLifecycleOwner) {
            if (it == true) {
                Toast.makeText(requireContext(), "Message forwarded", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
        messageViewModel.searchResult.observe(viewLifecycleOwner){
            listRecentTopic = it
            forwardMessageAdapter.update(it)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (forwardMessageAdapter.itemCount == 0) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun handleSearchEvent() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s?.length!!>0){
                    binding.shimmerFrameLayout.visibility = View.VISIBLE
                    binding.shimmerFrameLayout.startShimmer()
                }else{
                    forwardMessageAdapter.update(listRecentTopic)
                    binding.shimmerFrameLayout.stopShimmer()
                    binding.shimmerFrameLayout.visibility = View.GONE
                    if (forwardMessageAdapter.itemCount == 0) {
                        binding.resultEmptyLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.resultEmptyLayout.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 3) {
                    messageViewModel.onInputStateChanged(s.toString())
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.shimmerFrameLayout.stopShimmer()
    }

    override fun onMessageForward(topicId: String) {
        messageViewModel.sendMessage(topicId, messageItem.messageContent!!)
    }
}

private class ForwardMessageAdapter(
    private val listener: ItemClickListener,
    private var listMessage: MutableList<TopicItem>,
) : RecyclerView.Adapter<ForwardMessageAdapter.ViewHolder>() {

    fun update(newRequest: MutableList<TopicItem>) {
        listMessage = newRequest
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onMessageForward(topicId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.holder_forward_message, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(listMessage[position])
    }

    override fun getItemCount(): Int = listMessage.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = HolderForwardMessageBinding.bind(itemView)
        fun setData(topicItem: TopicItem) {
            binding.holderContactName.text = topicItem.topicName
            binding.holderContactAvatarContainer.loadImage(topicItem.topicPhoto, null)
            binding.actionForward.setOnClickListener {
                listener.onMessageForward(topicItem.topicId!!)
            }
        }
    }
}