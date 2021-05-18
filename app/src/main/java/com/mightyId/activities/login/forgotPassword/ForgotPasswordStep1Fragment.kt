package com.mightyId.activities.login.forgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.databinding.DialogForgotPasswordStep1Binding
import com.mightyId.utils.Constant.Companion.FRAGMENT_FORGOT_PASSWORD
import com.mightyId.utils.Constant.Companion.NAVIGATE_FROM
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_EMAIL
import com.mightyId.utils.showKeyboard
import timber.log.Timber

class ForgotPasswordStep1Fragment : BaseBottomSheetDialogFragment() {

    private lateinit var binding : DialogForgotPasswordStep1Binding
    private lateinit var viewRoot:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.tag("ForgotPasswordStep1Fragment").d("onCreateView: Called")
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_forgot_password_step1,container, false)
        viewRoot = binding.root
        //Handle Click event:
        binding.backSpace.setOnClickListener {
            dismiss()
        }
        binding.etEmail.apply {
            requestFocus()
            showKeyboard()
        }
        binding.actionSendVerification.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val bundle = Bundle()
            bundle.putString(NAVIGATE_FROM, FRAGMENT_FORGOT_PASSWORD)
            bundle.putString(REMOTE_MSG_CALLER_EMAIL,email)
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_forgotPasswordStep2Fragment,bundle)
        }
        return viewRoot
    }

}