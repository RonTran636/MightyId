package com.mightyId.activities.main.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.mightyId.R
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.databinding.FragmentHomeBinding
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.hideKeyboard
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.activities.userDetails.UserDetailViewModel
import com.mightyId.callback.CallListener
import com.mightyId.callback.ItemClickListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.RequestCall
import com.mightyId.models.TopicItem
import com.mightyId.utils.loadImage
import timber.log.Timber
import java.util.*


class HomeFragment : Fragment(), ItemClickListener, CallListener, MessageListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var friendStatusViewModel: UserDetailViewModel
    private lateinit var viewRoot: View

    private var contactAdapter = HomeAdapter(this, this, this, arrayListOf())
    private var isFriendStatusChecked = false
    private val handler = Handler(Looper.myLooper()!!)

    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        friendStatusViewModel = ViewModelProvider(this).get(UserDetailViewModel::class.java)
        viewRoot = binding.root
        viewRoot.hideKeyboard()

        binding.holderContactName.text =
            Common.currentAccount!!.customerName?.capitalize(Locale.ROOT)
        binding.holderWorkId.text = getString(R.string.holder_id, Common.currentAccount!!.workId)
        binding.holderContactAvatarContainer.loadImage(Common.currentAccount!!.photoUrl, null)

        //Load list suggesting friend from our server
        viewModel.loadRecommendContact(Common.currentAccount!!.customerId!!)
        binding.suggestContactList.adapter = contactAdapter
        //Handle search event
        binding.searchBarLayout.setOnClickListener {
            viewRoot.findNavController().navigate(R.id.action_navigation_home_to_searchFragment)
        }
        binding.searchBar.setOnClickListener {
            viewRoot.findNavController().navigate(R.id.action_navigation_home_to_searchFragment)
        }
        observeViewModel()
        return viewRoot
    }

    override fun onResume() {
        super.onResume()
        binding.shimmerFrameLayout.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerFrameLayout.stopShimmer()
    }

    private fun observeViewModel() {
        viewModel.listSuggestContact.observe(viewLifecycleOwner, {
            contactAdapter.update(it)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
        })
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner) {
            if (!it) {
                handler.postDelayed({
                    binding.shimmerFrameLayout.visibility = View.INVISIBLE
                    binding.emptyList.visibility = View.VISIBLE
                    binding.emptyListImage.setImageResource(R.drawable.pic_4)
                    binding.emptyListFindFriend.text = getString(R.string.connection_lost_message)
                },500)
            }
        }
    }

    override fun onItemLongClick(account: Account, binding: ViewDataBinding) {
    }

    override fun onItemClick(account: Account, binding: ViewDataBinding) {
        val intent = Intent(activity, UserDetailActivity::class.java)
        intent.putInfoExtra(Constant.INVITE_USER_INFO, account)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right,
            R.anim.slide_out_left)
    }

    override fun initiateMeeting(account: Account, type: String) {
        Timber.tag("HomeFragment").d("initiateMeeting: Data waiting to send: $account")
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
        val requestCall = RequestCall(Constant.CALL_REQUEST)
        requestCall.callerName = account.customerName
        requestCall.callerPhotoURL = account.photoUrl
        requestCall.callerCustomerId = account.customerId
        requestCall.meetingType = type
        intent.putExtra(Constant.TAG, TAG)
        intent.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, requestCall)
        startActivity(intent)
    }

    override fun initiateMeeting(topicItem: TopicItem, type: String) {
    }


    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireContext(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_bottom, 0)
    }
}