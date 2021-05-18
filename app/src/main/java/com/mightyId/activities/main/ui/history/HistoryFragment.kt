package com.mightyId.activities.main.ui.history

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mightyId.R
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.databinding.FragmentHistoryBinding
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.INVITE_USER_INFO
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.displayBadge
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.callback.CallListener
import com.mightyId.callback.MessageListener
import com.mightyId.models.Account
import com.mightyId.models.RequestCall
import com.mightyId.models.TopicItem
import timber.log.Timber

class HistoryFragment : Fragment(), CallListener, HistoryAdapter.OnUserDetail, MessageListener {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var viewRoot: View
    private lateinit var historyAdapter: HistoryAdapter

    companion object {
        const val UPDATE_HISTORY = 105
        const val TAG_PRIVATE = "HistoryFragmentPrivate"
        const val TAG_PUBLIC = "HistoryFragmentPublic"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history, container, false)
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        viewRoot = binding.root
        observeViewModel()
        historyAdapter = HistoryAdapter(this, this, this, arrayListOf())
        binding.historyList.adapter = historyAdapter
        return viewRoot
    }

    override fun onUserDetailClick(account: Account) {
        val intent = Intent(requireContext(), UserDetailActivity::class.java)
        intent.putInfoExtra(INVITE_USER_INFO, account)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun observeViewModel() {
            viewModel.getCallHistory()
        viewModel.isNetworkAvailable.observe(viewLifecycleOwner){
            if (!it){
                Handler(Looper.myLooper()!!).postDelayed({
                    binding.shimmerFrameLayout.visibility = View.INVISIBLE
                    binding.emptyList.visibility = View.VISIBLE
                    binding.emptyListImage.setImageResource(R.drawable.pic_4)
                    binding.emptyListFindFriend.text = getString(R.string.connection_lost_message)
                }, 500)
            }
        }
        viewModel.listHistory.observe(viewLifecycleOwner, {
            historyAdapter.update(it)
            binding.shimmerFrameLayout.stopShimmer()
            binding.shimmerFrameLayout.visibility = View.GONE
            binding.historyList.scrollToPosition(it.size - 1)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPDATE_HISTORY) {
            observeViewModel()
        }
    }

    override fun onResume() {
        //Update badge
        val navView: BottomNavigationView = requireActivity().findViewById(R.id.nav_view)
        navView.getOrCreateBadge(R.id.navigation_history).also {
            Common.notifyCentral.totalMissedCall = 0
            it.displayBadge(Common.notifyCentral.totalMissedCall)
        }
        binding.shimmerFrameLayout.startShimmer()
        super.onResume()
    }

    override fun onPause() {
        binding.shimmerFrameLayout.stopShimmer()
        super.onPause()
    }

    override fun initiateMeeting(account: Account, type: String) {
        Timber.tag("HistoryFragment").d("initiateMeeting account: Data waiting to send: $account")
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
        val requestCall = RequestCall(Constant.CALL_REQUEST)
        requestCall.callerName = account.customerName
        requestCall.callerPhotoURL = account.photoUrl
        requestCall.callerCustomerId = account.customerId
        requestCall.meetingType = type
        intent.putExtra(Constant.TAG, TAG_PRIVATE)
        intent.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, requestCall)
        startActivityForResult(intent, UPDATE_HISTORY)
    }

    override fun initiateMeeting(topicItem: TopicItem, type: String) {
        val intent = Intent(requireContext(), OutGoingInvitationActivity::class.java)
        Timber.tag("HistoryFragment").d("initiateMeeting topic: Data waiting to send: $topicItem")
        val requestCall = RequestCall(Constant.CALL_REQUEST)
        requestCall.callerName = topicItem.topicName
        requestCall.topicId = topicItem.topicId
        requestCall.callerPhotoURL = topicItem.topicPhoto
        requestCall.meetingType = type
        intent.putExtra(Constant.TAG, TAG_PUBLIC)
        intent.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, requestCall)
        startActivityForResult(intent, UPDATE_HISTORY)
    }

    override fun moveToChatRoom(chatRoomType: String, chatRoomKey: Bundle) {
        val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.TOPIC_TYPE, chatRoomType)
        intent.putExtra(ChatRoomActivity.TOPIC_INFO, chatRoomKey)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, 0)
    }
}