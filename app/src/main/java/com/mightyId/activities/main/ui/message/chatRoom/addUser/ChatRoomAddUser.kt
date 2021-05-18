package com.mightyId.activities.main.ui.message.chatRoom.addUser

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.main.ui.contact.ContactViewModel
import com.mightyId.databinding.FragmentChatRoomAddUserBinding
import com.mightyId.databinding.HolderAddParticipantBinding
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.search.SearchViewModel
import com.mightyId.adapters.AddParticipantAdapter
import com.mightyId.adapters.ListParticipantAdapter
import com.mightyId.callback.ItemClickListener
import com.mightyId.models.Account
import org.jetbrains.anko.textColor
import timber.log.Timber

class ChatRoomAddUser : BaseBottomSheetDialogFragment(), View.OnClickListener, ItemClickListener,
    ListParticipantAdapter.OnRemoveParticipant {

    private lateinit var binding: FragmentChatRoomAddUserBinding
    private lateinit var viewRoot: View
    private lateinit var viewModel: ChatRoomAddUserViewModel
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var mBinding: HolderAddParticipantBinding
    private lateinit var topicId: String

    private var listMemberJoined: ArrayList<Account>? = null
    private val listFriendAdapter = AddParticipantAdapter(this, arrayListOf())
    private val listParticipantAdapter = ListParticipantAdapter(this, arrayListOf())
    private val listParticipantLiveData = MutableLiveData<MutableList<Account>>()
    private var listParticipant = mutableListOf<Account>()


    companion object {
        const val TAG = "ChatRoomAddUser"
        const val LIST_MEMBER_JOINED = "listMember"
        fun newInstance(topicId: String, listMember: ArrayList<Account>?) =
            ChatRoomAddUser().apply {
                arguments = Bundle().apply {
                    putString(ChatRoomActivity.TOPIC_ID, topicId)
                    putInfoExtra(LIST_MEMBER_JOINED, listMember)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    )
            : View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chat_room_add_user,
            container,
            false
        )
        viewModel = ViewModelProvider(this).get(ChatRoomAddUserViewModel::class.java)
        contactViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        binding.listFriend.adapter = listFriendAdapter
        binding.listParticipant.adapter = listParticipantAdapter
        contactViewModel.getCurrentUserFriendList()
        viewRoot = binding.root
        topicId = requireArguments().getString(ChatRoomActivity.TOPIC_ID).toString()
        Timber.tag("ChatRoomAddUser").d("onCreateView: $topicId")
        listMemberJoined = requireArguments().getInfoExtra(LIST_MEMBER_JOINED)
//        if (!listMemberJoined.isNullOrEmpty()){
//            listParticipant = listMemberJoined as ArrayList<Account>
//            listParticipantAdapter.notifyDataSetChanged()
//        }
        searchViewModel.configureAutoComplete()
        obverseViewModel()

        binding.backSpace.setOnClickListener(this)
        binding.actionAdd.setOnClickListener(this)
        updateUIForSearchBar()
        return viewRoot
    }

    private fun updateUIForSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s?.length!! > 1) {
                    binding.shimmerFrameLayout.visibility = View.VISIBLE
                    binding.shimmerFrameLayout.startShimmer()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.emptyList.visibility = View.INVISIBLE
                binding.listFriend.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 0) {
                    searchViewModel.onInputStateChanged(s.toString())
                }
            }
        })
    }

    private fun obverseViewModel() {
        contactViewModel.listFriend.observe(viewLifecycleOwner) {
            listFriendAdapter.update(it)
            binding.shimmerFrameLayout.visibility = View.GONE
            binding.shimmerFrameLayout.stopShimmer()
        }
        listParticipantLiveData.observe(this) {
            listParticipantAdapter.update(it)
            if (it.isEmpty()) {
                binding.actionAdd.isEnabled = false
                binding.actionAdd.textColor =
                    resources.getColor(R.color.md_blue_grey_200, requireContext().theme)
            } else {
                binding.actionAdd.isEnabled = true
                binding.actionAdd.textColor =
                    resources.getColor(R.color.primary_color, requireContext().theme)
            }
        }
        searchViewModel.searchResult.observe(viewLifecycleOwner) {
            listFriendAdapter.update(it)
            listFriendAdapter.removeDuplicate(listParticipant)
            binding.listFriend.visibility = View.VISIBLE
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            if (listFriendAdapter.itemCount == 0) {
                binding.emptyList.visibility = View.VISIBLE
            }
        }
        viewModel.isAddMemberSuccess.observe(viewLifecycleOwner) {
            if (it == true) {
                Toast.makeText(requireContext(), "Add member successful", Toast.LENGTH_SHORT).show()
                dismiss()
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
            mBinding.checkbox.speed = -5F
            mBinding.checkbox.playAnimation()
            unSelect(account)
        } else {
            mBinding.checkbox.speed = 5F
            mBinding.checkbox.playAnimation()
            select(account)
        }
    }

    private fun select(account: Account) {
        listParticipant.add(account)
        listParticipantLiveData.value = listParticipant
    }

    private fun unSelect(account: Account) {
        listParticipant.remove(account)
        listParticipantLiveData.value = listParticipant
    }

    override fun onRemoveParticipant(account: Account) {
        mBinding.checkbox.speed = -5F
        mBinding.checkbox.playAnimation()
        listParticipant.remove(account)
        listParticipantLiveData.value = listParticipant
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.backSpace -> {
                dismiss()
            }
            binding.actionAdd -> {
                //TODO: Show progress bar
                val listMember = arrayListOf<String>()
                for (member in listParticipant) {
                    listMember.add(member.customerId!!)
                }
                viewModel.addMember(topicId, listMember)
            }
        }
    }

}