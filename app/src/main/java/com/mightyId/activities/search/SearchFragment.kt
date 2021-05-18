package com.mightyId.activities.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.activities.main.ui.contact.ContactFragment
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.databinding.FragmentSearchBinding
import com.mightyId.utils.Constant
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.hideKeyboard
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.activities.userDetails.UserDetailViewModel
import com.mightyId.callback.CallListener
import com.mightyId.callback.ItemClickListener
import com.mightyId.models.Account
import com.mightyId.models.PersonalChatInfo
import com.mightyId.models.RequestCall
import com.mightyId.models.TopicItem
import com.mightyId.utils.showKeyboard
import timber.log.Timber

class SearchFragment : BaseBottomSheetDialogFragment(),
    ItemClickListener, SearchContactAdapter.MessageListener, CallListener {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var friendStatusViewModel: UserDetailViewModel
    private lateinit var viewRoot: View
    private lateinit var contactAdapter: SearchContactAdapter

    private var isFriendStatusChecked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        friendStatusViewModel = ViewModelProvider(this).get(UserDetailViewModel::class.java)
        viewRoot = binding.root
        binding.searchBar.showKeyboard()

        contactAdapter = SearchContactAdapter(this, this, this, arrayListOf())
        viewModel.configureAutoComplete()
        observeViewModel()

        binding.backSpace.setOnClickListener {
            dismiss()
            requireActivity().hideKeyboard()
        }
        binding.resultList.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = contactAdapter
        }
        binding.searchBar.requestFocus()
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            if (text?.length!! > 1) {
                binding.shimmerFrameLayout.visibility = View.VISIBLE
                binding.shimmerFrameLayout.startShimmer()
                viewModel.onInputStateChanged(text.toString())
            }
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s?.length!! > 1) {
                    binding.shimmerFrameLayout.visibility = View.VISIBLE
                    binding.shimmerFrameLayout.startShimmer()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.resultEmptyLayout.visibility = View.INVISIBLE
                binding.resultList.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 1) {
                    viewModel.onInputStateChanged(s.toString())
                }
            }
        })
        return viewRoot
    }

    override fun onDestroy() {
        binding.searchBar.hideKeyboard()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerFrameLayout.stopShimmer()
    }

    private fun observeViewModel() {
        viewModel.searchResult.observe(viewLifecycleOwner, {
            contactAdapter.update(it)
            binding.resultList.visibility = View.VISIBLE
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (contactAdapter.itemCount == 0) {
                binding.resultEmptyLayout.visibility = View.VISIBLE
            }
        })
    }

    override fun onItemLongClick(account: Account, binding: ViewDataBinding) {
    }

    override fun onItemClick(account: Account, binding: ViewDataBinding) {
        val intent = Intent(activity, UserDetailActivity::class.java)
        intent.putInfoExtra(Constant.INVITE_USER_INFO, account)
        this.viewRoot.hideKeyboard()
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun moveToChatRoom(account: Account) {
        val bundle = Bundle()
        bundle.putInfoExtra(
            ChatRoomActivity.TOPIC_INFO,
            PersonalChatInfo(
                customerId = account.customerId!!,
                customerName = account.customerName,
                customerPhotoUrl = account.photoUrl,
                friendStatus = account.friendStatus
            )
        )
        val intent = Intent(requireContext(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, "private")
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, bundle)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, 0)
        dismiss()
    }

    override fun initiateMeeting(account: Account, type: String) {
        Timber.tag("SearchFragment").d("initiateMeeting: Data waiting to send: $account")
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
        val requestCall = RequestCall(
            messageType = Constant.CALL_REQUEST,
            callerName = account.customerName,
            callerPhotoURL = account.photoUrl,
            callerCustomerId = account.customerId,
            meetingType = type
        )
        viewRoot.hideKeyboard()
        intent.putExtra(Constant.TAG, ContactFragment.TAG)
        intent.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, requestCall)
        startActivity(intent)
    }

    override fun initiateMeeting(topicItem: TopicItem, type: String) {
    }
}