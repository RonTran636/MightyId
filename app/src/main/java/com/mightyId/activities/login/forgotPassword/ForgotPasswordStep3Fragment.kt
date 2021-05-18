package com.mightyId.activities.login.forgotPassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.mightyId.R
import com.mightyId.databinding.FragmentForgotPasswordStep3Binding

class ForgotPasswordStep3Fragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordStep3Binding
    private lateinit var viewRoot: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_forgot_password_step3,container,false)
        viewRoot = binding.root
        return viewRoot
    }
}