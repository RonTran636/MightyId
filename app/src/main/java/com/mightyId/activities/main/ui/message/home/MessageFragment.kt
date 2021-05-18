package com.mightyId.activities.main.ui.message.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mightyId.R
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.main.ui.message.home.MessageAdapter.Companion.ACTION_ARCHIVE
import com.mightyId.activities.main.ui.message.home.MessageAdapter.Companion.ACTION_DELETE_CONVERSATION
import com.mightyId.activities.main.ui.message.home.MessageAdapter.Companion.ACTION_MARK
import com.mightyId.activities.main.ui.message.home.MessageAdapter.Companion.ACTION_MUTE_NOTIFICATION
import com.mightyId.activities.main.ui.message.home.MessageAdapter.Companion.ACTION_PIN
import com.mightyId.activities.main.ui.message.home.MessageAdapter.Companion.ACTION_UNPIN
import com.mightyId.databinding.FragmentMessageBinding
import com.mightyId.databinding.HolderListMessageBinding
import com.mightyId.models.MessageItem
import com.mightyId.utils.*
import com.mightyId.utils.Constant.Companion.CHAT_REQUEST
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.callback.MessageListener
import com.mightyId.models.TopicItem
import io.socket.client.IO
import io.socket.client.Socket
import timber.log.Timber
import java.util.*

class MessageFragment : Fragment(), View.OnClickListener, MessageListener,
    MessageAdapter.ItemClickListener {

    private lateinit var binding: FragmentMessageBinding
    private lateinit var viewRoot: View
    private lateinit var socket: Socket

    private val viewModel: MessageViewModel by viewModels()
    private val image = listOf(R.drawable.pic_1, R.drawable.pic_2, R.drawable.pic_3)
    private val messageAdapter = MessageAdapter(this, this, arrayListOf())
    private var topicList: MutableList<TopicItem> = arrayListOf()

    private lateinit var currentTopicBinding: HolderListMessageBinding
    private var currentTopicItem = TopicItem()
    private var currentPosition = -1
    private var totalUnreadTopic = 0

    companion object {
        const val NEW_MESSAGE = "NEW_MESSAGE"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false)
        viewRoot = binding.root
        binding.messageList.adapter = messageAdapter
        binding.actionCreateTopic.setOnClickListener(this)
        binding.emptyListImage.setImageResource(image[Random().nextInt(image.size)])

        viewModel.configureAutoComplete()
        handleSearchEvent()
        obverseViewModel()
        establishConnectionWithSocket()
        return viewRoot
    }

    private fun handleSearchEvent() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    messageAdapter.removeAll()
                    binding.messageList.visibility = View.INVISIBLE
                    binding.shimmerFrameLayout.visibility = View.VISIBLE
                    binding.shimmerFrameLayout.startShimmer()
                } else {
                    messageAdapter.update(topicList)
                    binding.shimmerFrameLayout.stopShimmer()
                    binding.shimmerFrameLayout.visibility = View.GONE
                    binding.emptyList.visibility = View.INVISIBLE
                    if (messageAdapter.itemCount == 0) {
                        binding.emptyList.visibility = View.VISIBLE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 1) {
                    viewModel.onInputStateChanged(s.toString())
                }
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.message_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun obverseViewModel() {
        viewModel.listMessage.observe(viewLifecycleOwner) {
            Timber.tag("MessageFragment").d("obverseViewModel: data: $it")
            topicList = it.result
            totalUnreadTopic = it.totalUnreadTopic
            //Update badge
            val navView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
            navView.getOrCreateBadge(R.id.navigation_message).displayBadge(totalUnreadTopic)

            messageAdapter.update(it.result)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (messageAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyListFindFriend.apply {
                    text = getString(R.string.start_a_topic)
                    makeLinks(Pair("create a topic", object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            binding.actionCreateTopic.performClick()
                        }
                    }))
                }
            }
        }
        viewModel.isTopicPinned.observe(viewLifecycleOwner) {
            currentTopicItem.isTopicPinned = it
            messageAdapter.notifyItemChanged(currentPosition)
            if (it) {
                Collections.swap(topicList, currentPosition, 0)
                messageAdapter.notifyItemMoved(currentPosition, 0)
                binding.messageList.scrollToPosition(0)
                Toast.makeText(requireContext(), "Conversation pinned", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        viewModel.searchResult.observe(viewLifecycleOwner) {
            messageAdapter.update(it)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (messageAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyListImage.setImageResource(R.drawable.empty_list)
                binding.emptyListFindFriend.text = getString(R.string.message_result_empty)
            }
        }
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner){
            if (!it){
                Handler(Looper.myLooper()!!).postDelayed({
                    binding.shimmerFrameLayout.visibility = View.INVISIBLE
                    binding.emptyList.visibility = View.VISIBLE
                    binding.emptyListImage.setImageResource(R.drawable.pic_4)
                    binding.emptyListFindFriend.text = getString(R.string.connection_lost_message)
                },500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getRecentMessage(pages = 1)
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerFrameLayout.stopShimmer()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.actionCreateTopic -> {
                findNavController().navigate(R.id.action_navigation_message_to_createNewTopicFragment)
            }
        }
    }

    private fun establishConnectionWithSocket() {
        try {
            socket = IO.socket(Common.SOCKET_URL)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag("ChatRoomFragment").e("onCreateView: $e")
        }
        socket.connect()
    }


    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, 0)
    }

    override fun isUnReadChatRoom() {
        val navView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navView.getOrCreateBadge(R.id.navigation_message).also {
            Common.notifyCentral.totalUnreadMessage--
            it.displayBadge(Common.notifyCentral.totalUnreadMessage)
        }
    }

    override fun retrieveUserInfo(
        binding: HolderListMessageBinding,
        topicItem: TopicItem,
        position: Int,
    ) {
        currentTopicBinding = binding
        currentTopicItem = topicItem
        currentPosition = position
    }

    override fun onContextMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            ACTION_PIN -> {
                viewModel.addPin(Constant.PIN_TYPE_TOPIC, currentTopicItem.topicId!!)
            }
            ACTION_UNPIN -> {
                viewModel.deletePin(Constant.PIN_TYPE_TOPIC, currentTopicItem.topicId!!)
            }
            ACTION_ARCHIVE -> {
                val listTopicArchive = arrayListOf<String>()
                listTopicArchive.add(currentTopicItem.topicId!!)
                messageAdapter.archiveTopic(currentTopicItem)
                viewModel.archiveTopic(listTopicArchive)
            }
            ACTION_MARK -> {
                messageAdapter.reverseReadState(currentPosition)
            }
            ACTION_DELETE_CONVERSATION -> {
                requireActivity().inDevelop()
            }
            ACTION_MUTE_NOTIFICATION -> {
                requireActivity().inDevelop()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(internalBroadcastReceiver, IntentFilter(CHAT_REQUEST))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).apply {
            unregisterReceiver(internalBroadcastReceiver)
        }
    }

    private val internalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newMessage = intent?.getInfoExtra(NEW_MESSAGE) as MessageItem
            val newTopicUnread = messageAdapter.onNewMessage(newMessage)
            binding.messageList
            if (newTopicUnread) {
                //Update badge
                val navView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
                navView.getOrCreateBadge(R.id.navigation_message).displayBadge(totalUnreadTopic++)
            }
        }
    }
}