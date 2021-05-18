package com.mightyId.activities.main.ui.contact

import android.content.*
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mightyId.R
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.databinding.FragmentContactBinding
import com.mightyId.databinding.HolderContactBinding
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.ADD_FRIEND
import com.mightyId.utils.Constant.Companion.CALL_REQUEST
import com.mightyId.utils.Constant.Companion.INVITE_USER_INFO
import com.mightyId.utils.Constant.Companion.PENDING_FRIEND_LIST
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_INFO
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_GROUP_CALL
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_MEETING_TYPE
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.displayBadge
import com.mightyId.workManager.WorkerAddFriendConfirmedNotification
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.activities.userDetails.UserDetailViewModel
import com.mightyId.adapters.FriendRequestAdapter
import com.mightyId.callback.CallListener
import com.mightyId.callback.ItemClickListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.RequestAddFriendModel
import com.mightyId.models.RequestCall
import com.mightyId.models.TopicItem
import com.mightyId.utils.makeLinks
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

class ContactFragment : Fragment(), FriendRequestAdapter.FriendResponseListener,
    CallListener, ItemClickListener, MessageListener {

    private lateinit var viewModel: ContactViewModel
    private lateinit var friendStatusViewModel: UserDetailViewModel
    private lateinit var binding: FragmentContactBinding
    private lateinit var viewRoot: View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var friendRequestAdapter: FriendRequestAdapter
    private lateinit var contactAdapter: ContactAdapter

    private var pendingList: MutableList<RequestAddFriendModel>? = null
    private val isEmptyList = MutableLiveData<MutableList<String>>()
    private var json: String? = null
    private val gson = Gson()
    private val type: Type by lazy {
        object : TypeToken<MutableList<RequestAddFriendModel>>() {}.type
    }
    private val image = listOf(R.drawable.pic_1, R.drawable.pic_2, R.drawable.pic_3)
    private var isFriendStatusChecked = false
    private var contactList = mutableListOf<Account>()

    companion object {
        val listGroupCall = mutableListOf<String>()
        const val TAG = "ContactFragment"
        const val TAG_GROUP = "ContactFragmentGroupCall"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact, container, false)
        viewRoot = binding.root
        viewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        friendStatusViewModel = ViewModelProvider(this).get(UserDetailViewModel::class.java)
        //Activate Socket.IO
//        mSocket.on(Socket.EVENT_RECONNECT,onUserActive)
//        mSocket.on(Socket.EVENT_DISCONNECT,onUserInActive)
        //Retrieve list pending request from share preference:
        sharedPreferences = requireContext().getSharedPreferences(INVITE_USER_INFO, MODE_PRIVATE)
        json = sharedPreferences.getString(PENDING_FRIEND_LIST, "")
        pendingList = gson.fromJson(json, type)
        //Set up adapter
        if (pendingList.isNullOrEmpty()) {
            pendingList = arrayListOf()
        } else {
            binding.friendRequestLayout.visibility = View.VISIBLE
        }
        Timber.tag("ContactFragment").d("onCreateView: pending list : $pendingList")
        friendRequestAdapter = FriendRequestAdapter(this, pendingList!!)
        contactAdapter = ContactAdapter(this, this, this, arrayListOf())
        binding.pendingFriendList.adapter = friendRequestAdapter
        binding.friendList.adapter = contactAdapter
        binding.holderActionGroupCall.setOnClickListener {
            initiateGroupMeeting(listGroupCall, "audio")
        }
        binding.holderActionVideoGroupCall.setOnClickListener {
            initiateGroupMeeting(listGroupCall, "video")
        }
        binding.emptyListFindFriend.makeLinks(Pair("find some friend", View.OnClickListener {
            viewRoot.findNavController().navigate(R.id.action_navigation_contact_to_searchFragment)
        }))
        handleSearchEvent()
        observerViewModel()
        return viewRoot
    }

    private fun handleSearchEvent() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s?.length!! > 1) {
                    binding.shimmerFrameLayout.visibility = View.VISIBLE
                    binding.shimmerFrameLayout.startShimmer()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.emptyList.visibility = View.INVISIBLE
                binding.friendList.visibility = View.INVISIBLE
                if (s.isNullOrEmpty()) {
                    contactAdapter.update(contactList)
                    binding.shimmerFrameLayout.visibility = View.INVISIBLE
                    binding.friendList.visibility = View.VISIBLE
                } else {
                    contactAdapter.searchInContact(s.toString(), contactList)
                    if (contactAdapter.itemCount == 0) {
                        binding.shimmerFrameLayout.visibility = View.INVISIBLE
                        binding.emptyList.visibility = View.VISIBLE
                    } else {
                        binding.shimmerFrameLayout.visibility = View.INVISIBLE
                        binding.friendList.visibility = View.VISIBLE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun observerViewModel() {
        receiver.pendingListReceiver.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                binding.friendRequestLayout.visibility = View.VISIBLE
                friendRequestAdapter.update(it)
            }
        })
        viewModel.listFriend.observe(viewLifecycleOwner, {
            if (it != null) {
                contactAdapter.update(it)
                contactList = it
                binding.shimmerFrameLayout.stopShimmer()
                binding.shimmerFrameLayout.visibility = View.GONE
                if (contactAdapter.itemCount == 0) {
                    binding.emptyList.visibility = View.VISIBLE
                    binding.emptyListImage.setImageResource(image[Random().nextInt(image.size)])
                }
            } else {
                binding.shimmerFrameLayout.stopShimmer()
                binding.shimmerFrameLayout.visibility = View.GONE
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyListImage.setImageResource(image[Random().nextInt(image.size)])
            }
        })
        isEmptyList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.holderActionGroupCall.visibility = View.VISIBLE
                binding.holderActionVideoGroupCall.visibility = View.VISIBLE
            } else {
                binding.holderActionGroupCall.visibility = View.GONE
                binding.holderActionVideoGroupCall.visibility = View.GONE
            }
        }
        friendStatusViewModel.friendStatus.observe(viewLifecycleOwner) {

        }
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner) {
            if (!it) {
                Handler(Looper.myLooper()!!).postDelayed({
                    binding.shimmerFrameLayout.visibility = View.INVISIBLE
                    binding.emptyList.visibility = View.VISIBLE
                    binding.emptyListImage.setImageResource(R.drawable.pic_4)
                    binding.emptyListFindFriend.text = getString(R.string.connection_lost_message)
                }, 500)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        val pendingListReceiver = MutableLiveData<MutableList<RequestAddFriendModel>?>()
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ADD_FRIEND) {
                //Get list pending friend from share preference
                val pendingList: MutableList<RequestAddFriendModel>? = gson.fromJson(json, type)
                if (pendingList != null) {
                    pendingListReceiver.value = pendingList
                }
            }
        }
    }

    override fun onAccept(requestAddFriend: RequestAddFriendModel) {
        viewModel.getCurrentUserFriendList()
        removeFromPendingList(requestAddFriend)
        viewModel.sendResponseAccept(requestAddFriend)
        //Show Add friend confirm notification
        val dataToJson = Gson().toJson(requestAddFriend)
        val inputData = Data.Builder()
            .putString("requestAddFriend", dataToJson)
            .build()
        val workRequest =
            OneTimeWorkRequest.Builder(WorkerAddFriendConfirmedNotification::class.java)
                .setInputData(inputData)
                .addTag("requestAddFriend")
                .build()
        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }

    override fun onReject(requestAddFriend: RequestAddFriendModel) {
        removeFromPendingList(requestAddFriend)
        viewModel.sendResponseDecline(requestAddFriend)
    }

    private fun initiateGroupMeeting(listGroupCall: MutableList<String>, type: String) {
        if (listGroupCall.size > 1) {
            val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
            intent.putExtra(Constant.TAG, TAG_GROUP)
            intent.putExtra(REMOTE_MSG_GROUP_CALL, listGroupCall as ArrayList)
            intent.putExtra(REMOTE_MSG_MEETING_TYPE, type)
            startActivity(intent)
        }
    }

    override fun initiateMeeting(account: Account, type: String) {
        Timber.tag("ContactFragment").d("initiateMeeting: Data waiting to send: $account")
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
        val requestCall = RequestCall(CALL_REQUEST)
        requestCall.callerName = account.customerName
        requestCall.callerPhotoURL = account.photoUrl
        requestCall.callerCustomerId = account.customerId
        requestCall.meetingType = type
        intent.putExtra(Constant.TAG, TAG)
        intent.putInfoExtra(REMOTE_MSG_CALLER_INFO, requestCall)
        startActivity(intent)
    }

    override fun initiateMeeting(topicItem: TopicItem, type: String) {
    }

    private fun removeFromPendingList(requestAddFriend: RequestAddFriendModel) {
        receiver.pendingListReceiver.value?.remove(requestAddFriend)
        pendingList?.remove(requestAddFriend)
        if (pendingList.isNullOrEmpty()) {
            binding.friendRequestLayout.visibility = View.GONE
        }
        val editor = sharedPreferences.edit()
        editor.putString(PENDING_FRIEND_LIST, gson.toJson(pendingList))
        editor.apply()
    }

    override fun onItemClick(account: Account, binding: ViewDataBinding) {
        val mBinding = binding as HolderContactBinding
        if (listGroupCall.isNullOrEmpty()) {
            val intent = Intent(requireContext(), UserDetailActivity::class.java)
            intent.putInfoExtra(INVITE_USER_INFO, account)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right,
                R.anim.slide_out_left)
        } else {
            if (listGroupCall.contains(account.customerId)) {
                //User already contain in the list - remove it
                unSelectUser(account, mBinding)
            } else {
                //User don't have in the list - add it in
                selectUser(account, mBinding)
            }
        }
    }

    override fun onItemLongClick(account: Account, binding: ViewDataBinding) {
        val mBinding = binding as HolderContactBinding
        if (listGroupCall.contains(account.customerId)) {
            //User already contain in the list - remove it
            unSelectUser(account, mBinding)
        } else {
            //User don't have in the list - adding in
            selectUser(account, mBinding)
        }
    }

    private fun selectUser(account: Account, binding: HolderContactBinding) {
        listGroupCall.add(account.customerId!!)
        isEmptyList.value = listGroupCall
        binding.holderContactActive.visibility = View.GONE
        binding.holderContactMultiSelect.speed = 3.5F
        binding.holderContactMultiSelect.playAnimation()
        binding.holderActionVideoCall.visibility = View.GONE
        binding.holderActionCall.visibility = View.GONE
        binding.holderActionMessage.visibility = View.GONE
    }

    private fun unSelectUser(account: Account, binding: HolderContactBinding) {
        listGroupCall.remove(account.customerId!!)
        isEmptyList.value = listGroupCall
        binding.holderContactActive.visibility = View.VISIBLE
        binding.holderContactMultiSelect.speed = -3.5F
        binding.holderContactMultiSelect.playAnimation()
        binding.holderActionVideoCall.visibility = View.VISIBLE
        binding.holderActionCall.visibility = View.VISIBLE
        binding.holderActionMessage.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            receiver,
            IntentFilter(ADD_FRIEND)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            receiver
        )
    }

    override fun onResume() {
        //Update badge
        val navView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navView.getOrCreateBadge(R.id.navigation_contact).also {
            Common.notifyCentral.totalRequestAddFriend = 0
            it.displayBadge(Common.notifyCentral.totalRequestAddFriend)
        }
        viewModel.getCurrentUserFriendList()
        binding.shimmerFrameLayout.startShimmer()
        super.onResume()
    }

    override fun onPause() {
        binding.shimmerFrameLayout.stopShimmer()
        super.onPause()
    }

    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireContext(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_bottom, 0)
    }
}