package com.mightyId.activities.login.forgotPassword

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mightyId.R
import com.mightyId.databinding.FragmentForgotPasswordStep2Binding
import com.mightyId.activities.login.home.LoginHomeViewModel
import com.mightyId.utils.Constant.Companion.FRAGMENT_FORGOT_PASSWORD
import com.mightyId.utils.Constant.Companion.FRAGMENT_SIGN_UP
import com.mightyId.utils.Constant.Companion.NAVIGATE_FROM
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_EMAIL
import com.mightyId.utils.makeLinks
import timber.log.Timber

class ForgotPasswordStep2Fragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordStep2Binding
    private lateinit var viewRoot: View
    private lateinit var isNavigateFrom: String
    private lateinit var emailRecover: String
    private val viewModel: LoginHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password_step2, container, false)
        viewRoot = binding.root
        isNavigateFrom = arguments?.getString(NAVIGATE_FROM).toString()
        //Navigate to send email
        when (isNavigateFrom) {
//            FRAGMENT_SIGN_UP -> {
//                binding.actionSkip.visibility = View.VISIBLE
//                val actionCodeSettings = ActionCodeSettings.newBuilder()
//                    .setUrl("https://workid01.page.link/iGuj")
//                    .setHandleCodeInApp(true)
//                    .setDynamicLinkDomain("workid01.page.link")
//                    .setAndroidPackageName(
//                        "com.workid",
//                        true,
//                        "26"
//                    )
//                    .build()
//                auth.currentUser!!.sendEmailVerification(actionCodeSettings)
//            }
            FRAGMENT_FORGOT_PASSWORD -> {
                emailRecover = arguments?.getString(REMOTE_MSG_CALLER_EMAIL).toString()
                binding.actionSkip.visibility = View.GONE
                viewModel.sendPasswordRecoveryEmail(emailRecover)
            }
        }
        when (isNavigateFrom) {
            FRAGMENT_SIGN_UP -> {
                binding.actionSkip.visibility = View.VISIBLE
            }
            FRAGMENT_FORGOT_PASSWORD -> {
                binding.actionSkip.visibility = View.GONE
            }
        }
        binding.actionSkip.setOnClickListener {
            findNavController().navigate(R.id.action_forgotPasswordStep2Fragment_to_signupWelcome)
        }

        binding.actionOpenMailApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            val activities : List<ResolveInfo> = viewRoot.context.packageManager.queryIntentActivities(intent,0)
            if (activities.isNotEmpty()) startActivity(intent)
            else Timber.tag("ForgotPasswordStep2Fragment").d("onCreateView: something went wrong")
        }

        binding.tryAnotherEmail.makeLinks(Pair("try another email address",object :View.OnClickListener{
            override fun onClick(v: View?) {
              findNavController().popBackStack()
            }
        }))
        return viewRoot
    }
}