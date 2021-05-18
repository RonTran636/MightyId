package com.mightyId.activities.login.signup

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mightyId.R
import com.mightyId.activities.base.BaseBottomSheetDialogFragment
import com.mightyId.activities.login.home.LoginHomeViewModel
import com.mightyId.databinding.FragmentSignupBinding
import com.mightyId.utils.*
import com.mightyId.utils.Constant.Companion.FRAGMENT_SIGN_UP
import com.mightyId.utils.Constant.Companion.NAVIGATE_FROM
import java.util.*


class FragmentSignUp : BaseBottomSheetDialogFragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var viewModel: LoginHomeViewModel
    private lateinit var viewRoot: View

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var email: String

    companion object{
        private const val EMAIL = "email"
        private const val DISPLAY_NAME = "displayName"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
        viewModel = ViewModelProvider(this).get(LoginHomeViewModel::class.java)
        viewRoot = binding.root
        activity?.setLightStatusBar(false)

        verifyInput()

        observeViewModel()

        if (arguments!=null){
            binding.etEmail.setText(arguments?.getString(EMAIL))
            binding.etUserName.setText(arguments?.getString(DISPLAY_NAME))
        }

        binding.termAndPrivacy.makeLinks(Pair("Privacy Policy", View.OnClickListener {
            retrieveUserEntered()
            findNavController().navigate(R.id.action_fragmentSignUp_to_fragmentWebView)
        }))
        binding.backSpace.setOnClickListener {
            dismiss()
        }
        binding.buttonCreateAccount.setOnClickListener {
            binding.buttonCreateAccount.startAnimation()
            it.hideKeyboard()
            retrieveUserEntered()
            when {
//                password.isValidPassword() &&
                    (password == confirmPassword) && (binding.checkBox.isChecked) -> {
                    viewModel.createNewAccount(email, password, username)
                }
                password != confirmPassword -> {
                    binding.buttonCreateAccount.revertAnimation{
                        it.setBackgroundResource(R.drawable.border_login_button)
                    }
                    binding.etConfirmPasswordLayout.error =
                        viewRoot.context.getString(R.string.password_not_match)
                }
                !password.isValidPassword() -> {
                    binding.buttonCreateAccount.revertAnimation{
                        it.setBackgroundResource(R.drawable.border_login_button)
                    }
                    binding.etConfirmPassword.error =
                        viewRoot.context.getString(R.string.invalid_password)
                    binding.passwordRules.visibility = View.VISIBLE
                }
                !binding.checkBox.isChecked ->{
                    binding.buttonCreateAccount.revertAnimation{
                        it.setBackgroundResource(R.drawable.border_login_button)
                    }
                    binding.termAndPrivacy.setTextColor(Color.RED)
                    Toast.makeText(requireContext(),getString(R.string.term_privacy_reminder),Toast.LENGTH_SHORT).show()
                }
            }
        }
        return viewRoot
    }

    private fun observeViewModel() {
        viewModel.loginSuccessDetails.observe(viewLifecycleOwner, {
            if (it == true) {
                val bundle = Bundle()
                bundle.putString(NAVIGATE_FROM, FRAGMENT_SIGN_UP)
                findNavController().navigate(R.id.action_fragmentSignUp_to_forgotPasswordStep2Fragment,
                    bundle)
            }
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            binding.buttonCreateAccount.revertAnimation()
            binding.etEmailLayout.error = it
        })
    }

    private fun verifyInput() {
        binding.etPassword.doOnTextChanged { password, _, _, _ ->
            binding.etPasswordLayout.error = null
            if (!password.isNullOrEmpty()) {
//                verifyPassword(password)
            }
        }

        binding.etConfirmPassword.doOnTextChanged { text, _, _, _ ->
            if (text.toString() != binding.etPassword.text.toString()) {
                binding.etConfirmPasswordLayout.error = getString(R.string.password_not_match)
            } else {
                binding.etConfirmPasswordLayout.error = null
            }
        }

        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            binding.etEmailLayout.error = null
        }
    }

    private fun verifyPassword(text: CharSequence) {
        updateUI(binding.is1CapitalLetter,text.checkPasswordStrength("(?=.*?[A-Z])"))
        updateUI(binding.is1LowercaseLetter,text.checkPasswordStrength("(?=.*?[a-z])"))
        updateUI(binding.is1SpecialChar,text.checkPasswordStrength("(?=.*?[#?!@$%^&*-])"))
        updateUI(binding.is1Number,text.checkPasswordStrength("(?=.*?[0-9])"))
        updateUI(binding.is6Chars,text.checkPasswordStrength(".{6,}"))
    }

    private fun showDrawable(view: TextView, drawable: Int, colorCode: Int) {
        view.setTextColor(ColorStateList.valueOf(resources.getColor(colorCode,
            requireContext().theme)))
        val temp = ContextCompat.getDrawable(requireContext(), drawable)
        temp!!.setTint(resources.getColor(colorCode, requireContext().theme))
        view.setCompoundDrawablesWithIntrinsicBounds(null, null, temp, null)
    }

    private fun updateUI(view: TextView,condition: Boolean){
        if (condition) {
            showDrawable(view, R.drawable.ic_baseline_check_24, R.color.md_green_500)
        } else {
            showDrawable(view, R.drawable.ic_baseline_clear_24, R.color.colorRedError)
        }
    }

    private fun retrieveUserEntered(){
        username = binding.etUserName.text.toString().trim()
        email = binding.etEmail.text.toString().trim().decapitalize(Locale.ROOT)
        password = binding.etPassword.text.toString().trim()
        confirmPassword = binding.etConfirmPassword.text.toString().trim()
    }
}