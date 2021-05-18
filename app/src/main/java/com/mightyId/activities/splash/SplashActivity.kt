package com.mightyId.activities.splash

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.mightyId.BuildConfig
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.activities.login.LoginActivity
import com.mightyId.activities.main.MainActivity
import com.mightyId.activities.onboarding.OnBoardingActivity
import com.mightyId.databinding.ActivitySplashBinding
import com.mightyId.models.Account
import com.mightyId.utils.Constant.Companion.FIRST_RUN
import com.mightyId.utils.Key
import com.mightyId.utils.connectionLostSnackBar
import timber.log.Timber
import kotlin.properties.Delegates

class SplashActivity : BaseActivity() {

    private lateinit var dataSave: SharedPreferences
    private var isFirstRun by Delegates.notNull<Boolean>()
    private val handler by lazy { Handler(Looper.myLooper()!!) }
    private val gson = Gson()
    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "DEBUGGING", Toast.LENGTH_LONG).show()
            Key.KEY = Key.DEBUGGING_KEY
            Common.BASE_URL = Constant.DEBUGGING_BASE_URL
            Common.SOCKET_URL = Constant.DEBUGGING_SOCKET_URL
        } else {
            Key.KEY = Key.PRODUCTION_KEY
            Common.BASE_URL = Constant.PRODUCTION_BASE_URL
            Common.SOCKET_URL = Constant.PRODUCTION_SOCKET_URL
        }
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            statusBarColor = Color.TRANSPARENT
//            setLightStatusBar(false)
        }
        EmojiManager.install(GoogleEmojiProvider())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        //Detect first run - show OnBoarding Activity accordingly
        dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        isFirstRun = dataSave.getBoolean(FIRST_RUN, true)
        val connectionSnackbar = binding.root.connectionLostSnackBar().also { snackbar->
            snackbar.setAction(R.string.immersive_cling_positive) { }
                .setActionTextColor(ColorStateList.valueOf(resources.getColor(R.color.accent_red, theme)))
        }
        Common.isConnected.observe(this){
            if (!it) connectionSnackbar.show()
            else connectionSnackbar.dismiss()
        }
    }


    override fun onStart() {
        super.onStart()
        handler.postDelayed({ checkExistUser() }, 500)
    }

    private fun checkExistUser() {
        dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val json = dataSave.getString(Constant.USER_INFO, "")
        if (!json.isNullOrEmpty()) {
            Common.currentAccount = gson.fromJson(json, Account::class.java)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        } else {
            //Handle whether user will see OnBoarding Activity or not
            if (isFirstRun) {
                startActivity(Intent(this, OnBoardingActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}