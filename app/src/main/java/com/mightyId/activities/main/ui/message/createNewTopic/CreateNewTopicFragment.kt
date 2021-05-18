package com.mightyId.activities.main.ui.message.createNewTopic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.main.ui.contact.ContactViewModel
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.databinding.FragmentCreateNewTopicBinding
import com.mightyId.databinding.HolderAddParticipantBinding
import com.mightyId.utils.Constant.Companion.USER_INFO
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.activities.search.SearchViewModel
import com.mightyId.adapters.AddParticipantAdapter
import com.mightyId.adapters.ListParticipantAdapter
import com.mightyId.callback.ItemClickListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.PublicChatInfo
import com.mightyId.utils.hideKeyboard
import java.util.*

class CreateNewTopicFragment : BaseBottomSheetDialogFragment(), View.OnClickListener,
    ItemClickListener,
    ListParticipantAdapter.OnRemoveParticipant, MessageListener {

    private lateinit var binding: FragmentCreateNewTopicBinding
    private lateinit var viewModel: CreateNewTopicViewModel
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var mBinding: HolderAddParticipantBinding
    private lateinit var viewRoot: View
    private lateinit var topicName: String

    private val handler: Handler by lazy { Handler(Looper.myLooper()!!) }
    private val addParticipantAdapter = AddParticipantAdapter(this, arrayListOf())
    private val listParticipantAdapter = ListParticipantAdapter(this, arrayListOf())
    private val listParticipant = mutableListOf<Account>()
    private var listContact = arrayListOf<Account>()
    private val listParticipantLiveData = MutableLiveData<MutableList<Account>>()
    private val image = listOf(R.drawable.pic_1, R.drawable.pic_2, R.drawable.pic_3)
    private var userTopicWith : Account?=null

    companion object{
        const val TAG = "CreateNewTopicFragment"
        fun newInstance(account: Account) = CreateNewTopicFragment().apply {
            arguments = Bundle().apply {
                putInfoExtra(USER_INFO,account)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_new_topic, container, false)
        viewRoot = binding.root
        viewModel = ViewModelProvider(this).get(CreateNewTopicViewModel::class.java)
        contactViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        binding.imageView.setImageResource(image[Random().nextInt(image.size)])
        userTopicWith = arguments?.getInfoExtra(USER_INFO)

        contactViewModel.getCurrentUserFriendList()
        searchViewModel.configureAutoComplete()

        binding.listFriendShareTopic.adapter = addParticipantAdapter
        binding.listParticipant.adapter = listParticipantAdapter
        binding.backSpace.setOnClickListener(this)
        binding.actionCreateTopic.setOnClickListener(this)

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s?.length!! > 0) {
                    addParticipantAdapter.removeAll()
                    binding.shimmerFrameLayout.visibility = View.VISIBLE
                    binding.shimmerFrameLayout.startShimmer()
                } else {
                    addParticipantAdapter.update(listContact)
                    binding.shimmerFrameLayout.visibility = View.GONE
                    binding.shimmerFrameLayout.stopShimmer()
                    if (addParticipantAdapter.itemCount == 0) {
                        binding.emptyList.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.emptyList.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 0) {
                    searchViewModel.onInputStateChanged(s.toString())
                }
            }
        })
        observeViewModel()
        verifyInput()
        return viewRoot
    }

    private fun verifyInput() {
        binding.topicName.doOnTextChanged { _, _, _, _ ->
            binding.topicNameLayout.error = null
        }
    }

    private fun observeViewModel() {
        contactViewModel.listFriend.observe(this) {
            listContact = it
            addParticipantAdapter.update(it)
            if (userTopicWith!=null){
                select(userTopicWith!!)
            }
            binding.shimmerFrameLayout.visibility = View.GONE
            binding.shimmerFrameLayout.stopShimmer()
            if (addParticipantAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
                binding.textView.text = getString(R.string.no_friend)
            }
        }
        listParticipantLiveData.observe(this) {
            listParticipantAdapter.update(it)

        }
        searchViewModel.searchResult.observe(this) {
            addParticipantAdapter.update(it)
            binding.listParticipant.visibility = View.VISIBLE
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (addParticipantAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
            }
        }

        viewModel.topicId.observe(this) {
            if (!it.isNullOrEmpty()) {
                handler.postDelayed({
                    val bundle = Bundle()
                    bundle.putInfoExtra(
                        ChatRoomActivity.TOPIC_INFO,
                        PublicChatInfo(it, null, topicName, listParticipant.size + 1, null)
                    )
                    moveToChatRoom("public", bundle)
                    dismiss()
                }, 500)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.backSpace -> {
                dismiss()
            }
            binding.actionCreateTopic -> {
                topicName = binding.topicName.text.toString()
                binding.actionCreateTopic.hideKeyboard()
                when {
                    topicName.isEmpty() -> {
                        binding.topicNameLayout.error = getString(R.string.message_topic_name_empty)
                    }
                    listParticipant.isEmpty() -> {
                        binding.topicNameLayout.error = getString(R.string.message_empty_topic)
                    }
                    else -> {
                        binding.progressBarLayout.visibility = View.VISIBLE
                        val listCustomerId: ArrayList<String> = arrayListOf()
                        for (user in listParticipant) {
                            listCustomerId.add(user.customerId!!)
                        }
                        viewModel.createTopic(topicName, listCustomerId)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.shimmerFrameLayout.stopShimmer()
    }

    override fun onItemLongClick(account: Account, binding: ViewDataBinding) {
    }

    override fun onItemClick(account: Account, binding: ViewDataBinding) {
        mBinding = binding as HolderAddParticipantBinding
        if (listParticipant.contains(account)) {
            unSelect(account)
        } else {
            mBinding.checkbox.speed = 5F
            mBinding.checkbox.playAnimation()
            this.binding.topicNameLayout.error = null
            select(account)
        }
    }

    private fun select(account: Account) {
        listParticipant.add(account)
        listParticipantAdapter.addMember(account)
        binding.listParticipant.scrollToPosition(listParticipantAdapter.itemCount - 1)
    }

    private fun unSelect(account: Account) {
        mBinding.checkbox.speed = -5F
        mBinding.checkbox.playAnimation()
        listParticipant.remove(account)
        listParticipantAdapter.removeMember(account)
    }

    override fun onRemoveParticipant(account: Account) {
        unSelect(account)
    }

    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireContext(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_bottom, 0)
    }
}