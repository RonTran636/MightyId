package com.mightyId.activities.main.ui.setting.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.databinding.FragmentPrivacyBinding
import com.mightyId.utils.Common
import timber.log.Timber

class PrivacyFragment: BaseBottomSheetDialogFragment() {
    private lateinit var binding: FragmentPrivacyBinding
    private val viewModel : PrivacyViewModel by viewModels()
    private var tempCall = Common.currentAccount!!.strangerCall
    private var tempInviteTopic = Common.currentAccount!!.strangeInviteTopic
    private var tempMessage = Common.currentAccount!!.strangerMessage

    companion object{
        const val TAG = "PrivacyFragment"
        fun newInstance() = PrivacyFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_privacy,container,false)
        Timber.tag("PrivacyFragment").d("onCreateView: current user : ${Common.currentAccount}")
        binding.switchReceiveCall.apply {
            isChecked = tempCall
            setOnCheckedChangeListener { _, isChecked ->
                tempCall = isChecked
            }
        }
        binding.switchReceiveInviteTopic.apply {
            isChecked = tempInviteTopic
            setOnCheckedChangeListener { _, isChecked ->
                tempInviteTopic = isChecked
            }
        }
        binding.switchReceiveMessage.apply {
            isChecked = tempMessage
            setOnCheckedChangeListener{_, isChecked->
                tempMessage = isChecked
            }
        }
        return binding.root
    }

    override fun onDestroy() {
        if (tempCall != Common.currentAccount!!.strangerCall){
            Common.currentAccount!!.strangerCall = tempCall
            val privacy = if (tempCall) 1 else 0
            Timber.tag("PrivacyFragment").d("onDestroy: $privacy")
            viewModel.blockStrangerCall(privacy)
        }
        if (tempInviteTopic != Common.currentAccount!!.strangeInviteTopic){
            Common.currentAccount!!.strangeInviteTopic = tempInviteTopic
            val privacy = if (tempInviteTopic) 1 else 0
            Timber.tag("PrivacyFragment").d("onDestroy: $privacy")
            viewModel.blockStrangerInviteTopic(privacy)
        }
        if (tempMessage != Common.currentAccount!!.strangerMessage){
            Common.currentAccount!!.strangerMessage = tempInviteTopic
            val privacy = if (tempInviteTopic) 1 else 0
            Timber.tag("PrivacyFragment").d("onDestroy: $privacy")
            viewModel.blockStrangerSendMessage(privacy)
        }
        super.onDestroy()
    }
}