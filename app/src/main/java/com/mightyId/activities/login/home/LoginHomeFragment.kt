package com.mightyId.activities.login.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.mightyId.R
import com.mightyId.databinding.FragmentLoginHomeBinding
import com.mightyId.utils.hideKeyboard
import com.mightyId.activities.main.MainActivity
import timber.log.Timber

class LoginHomeFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val RC_SIGN_IN = 101
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: LoginHomeViewModel
    private lateinit var binding: FragmentLoginHomeBinding
    private lateinit var viewRoot: View

    private var isEmailAndPasswordLegit = true //TODO:Change to false
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Timber.tag("LoginHomeFragment").d("onCreateView: Called")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_home, container, false)
        viewModel = ViewModelProvider(this).get(LoginHomeViewModel::class.java)
        viewRoot = binding.root

        binding.buttonLogin.setOnClickListener(this)
        binding.buttonCreateAccount.setOnClickListener(this)
        binding.forgotPassword.setOnClickListener(this)
        binding.signInGoogle.setOnClickListener(this)
        binding.signInFacebook.setOnClickListener(this)
        return viewRoot
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)

        verifyInput()
        observeViewModel()
    }

    // [START auth_with_email_and_password]
    private fun signInWithEmailAndPassword() {
        val userEmail = binding.etEmail.text.toString()
        val userPassword = binding.etPassword.text.toString()
        if (userEmail.isEmpty()) {
            binding.etEmailLayout.error = getString(R.string.empty_field,"Email")
        }
        if (userPassword.isEmpty()) {
            binding.etPasswordLayout.error = getString(R.string.empty_field,"Password")
        }
        if (isEmailAndPasswordLegit) {
            viewModel.loginWithEmailAndPassword(userEmail,userPassword)
        }
    }

    private fun verifyInput() {
//        binding.etEmail.doOnTextChanged { s, _ , _, _ ->
//            if (s!=null) {
//                if (s.isValidEmail()) {
//                    isEmailAndPasswordLegit = false
//                    binding.etEmailLayout.error = getString(R.string.invalid_user_email)
//                } else {
//                    isEmailAndPasswordLegit = true
//                    binding.etEmailLayout.error = null
//                }
//            }
//        }
        binding.etPassword.doOnTextChanged { _, _, _, _ ->
            isEmailAndPasswordLegit = true
            binding.etPasswordLayout.error = null
        }
        binding.etEmail.doOnTextChanged { _, _, _, _ ->
            isEmailAndPasswordLegit= true
            binding.etEmailLayout.error = null
        }
    }

    private fun observeViewModel() {
        viewModel.loginSuccessDetails.observe(viewLifecycleOwner, {
//            Timber.tag("observeViewModel").d("current user server token: ${Common.currentAccount!!.serverToken}")
            when (it) {
                true -> {
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                }
                false->{
                    binding.buttonLogin.revertAnimation {
                        binding.buttonLogin.setBackgroundResource(R.drawable.border_login_button)
                    }
                }
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner){
            binding.etEmailLayout.error = it
        }
    }



    override fun onClick(v: View?) {
        when (v) {
            binding.buttonLogin -> {
                v.hideKeyboard()
                binding.buttonLogin.startAnimation()
                signInWithEmailAndPassword()
            }
            binding.buttonCreateAccount -> {
                findNavController().navigate(R.id.action_loginHomeFragment_to_fragmentSignUp)
            }
            binding.forgotPassword -> {
                findNavController().navigate(R.id.action_loginHomeFragment_to_forgotPasswordFragment)
            }
//            binding.signInGoogle -> {
//                binding.progressBarLayout.visibility = View.VISIBLE
//                signInWithGoogle()
//            }
//            binding.signInFacebook -> {
//                binding.progressBarLayout.visibility = View.VISIBLE
//                signInWithFacebook()
//            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (arguments!=null){
            findNavController().navigate(R.id.action_loginHomeFragment_to_fragmentSignUp,arguments)
        }
    }
    /**Un-used method
    // [Handle received credential]
    private fun handleAccessToken(credential: AuthCredential) {
    binding.progressBarLayout.visibility = View.VISIBLE
    auth.signInWithCredential(credential)
    .addOnCompleteListener(requireActivity()) { task ->
    if (task.isSuccessful) {
    Timber.tag("handleAccessToken").d("Called, ${auth.currentUser!!.email}")
    viewModel.loginDatabase(auth)
    } else {
    //Sign in fails, display a message to the user.`
    binding.progressBarLayout.visibility = View.INVISIBLE
    Timber.tag("handleAccessToken").e(task.exception)
    when (task.exception) {
    is FirebaseAuthInvalidCredentialsException -> {
    binding.etEmailLayout.error =
    getString(R.string.message_invalid_password)
    }
    is FirebaseAuthInvalidUserException-> {
    binding.etEmailLayout.error = getString(R.string.email_not_registered)
    }
    is FirebaseAuthMultiFactorException -> {
    Timber.tag("LoginHomeFragment")
    .d("handleAccessToken: this email is registered with other method")
    }
    is Exception -> {
    Timber.tag("LoginHomeFragment").e("handleAccessToken: Something went wrong ${task.exception}")
    }
    }
    }
    }
    }

    // [START auth_with_facebook]
    private fun signInWithFacebook() {
    LoginManager.getInstance().registerCallback(callbackManager, object :
    FacebookCallback<LoginResult> {
    override fun onSuccess(result: LoginResult) {
    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
    handleAccessToken(credential)
    }

    override fun onCancel() {
    }

    override fun onError(error: FacebookException?) {
    }
    })
    LoginManager.getInstance().logInWithReadPermissions(this, arrayListOf("email","public_profile"))
    }

    // [START auth_with_google]
    private fun signInWithGoogle() {
    val signInIntent = googleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    callbackManager.onActivityResult(requestCode, resultCode, data)
    //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    try {
    // Google Sign In was successful, authenticate with Firebase
    val account = task.getResult(ApiException::class.java)!!
    val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
    handleAccessToken(credential)
    } catch (e: ApiException) {
    // Google Sign In failed, update UI appropriately
    Timber.tag("onActivityResult").e(e)
    }
    }
    }
     */
}