package com.mightyId.activities.login

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.databinding.ActivityLoginBinding
import com.mightyId.utils.Common
import com.mightyId.utils.connectionLostSnackBar
import timber.log.Timber

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        window.apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }else{
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, theme)))
        }
        Common.isConnected.observe(this){
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
    }

    override fun onResume() {
        Timber.tag("LoginActivity").d("onResume: Called")
        checkForDynamicLinks()
        super.onResume()
    }

    private fun checkForDynamicLinks() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
            val deepLink: Uri?
            if (it != null) {
                deepLink = it.link
                Timber.tag("LoginActivity").d("checkForDynamicLinks: deepLink: $deepLink")
                if (deepLink != null) {
                    when (deepLink.getQueryParameter("mode")) {
                        "resetPassword" -> {
                            Timber.tag("LoginActivity").d("checkForDynamicLinks:  Move to resetPassword")
                            val pendingIntent = NavDeepLinkBuilder(this)
                                .setGraph(R.navigation.login_navigation)
                                .setDestination(R.id.forgotPasswordStep3Fragment)
                                .createPendingIntent()
                            pendingIntent.send()
                        }
                        "verifyEmail" -> {
                            Timber.tag("LoginActivity").d("checkForDynamicLinks:  Move to verify email")
                            val pendingIntent = NavDeepLinkBuilder(this)
                                .setGraph(R.navigation.login_navigation)
                                .setDestination(R.id.signupWelcome)
                                .createPendingIntent()
                            pendingIntent.send()
                        }
                    }
                }
            }
        }.addOnFailureListener {
            Timber.tag("FirebaseDynamicLinks").d("addOnFailureListener: ${it.message}")
        }
    }
}