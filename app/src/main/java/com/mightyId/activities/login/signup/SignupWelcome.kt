package com.mightyId.activities.login.signup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.mightyId.R
import com.mightyId.databinding.FragmentSignupWelcomeBinding
import com.mightyId.activities.main.MainActivity

class SignupWelcome : Fragment() {

    private lateinit var binding: FragmentSignupWelcomeBinding
    private lateinit var viewRoot: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup_welcome,container,false)
        viewRoot = binding.root

        binding.startExperience.setOnClickListener {
           startActivity(Intent(requireContext(), MainActivity::class.java))
        }
        return viewRoot
    }

}