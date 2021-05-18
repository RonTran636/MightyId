package com.mightyId.activities.main

import android.content.*
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.databinding.ActivityMainBinding
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.ADD_FRIEND
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.CHAT_REQUEST
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.clearFocusOnOutsideClick
import com.mightyId.models.RequestCall
import com.mightyId.utils.connectionLostSnackBar
import timber.log.Timber

class MainActivity : BaseActivity() {

    private lateinit var dataSave: SharedPreferences
    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private val gson = Gson()
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        //Save user information into shared preference
        if (Common.currentAccount != null) {
            dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            val json = gson.toJson(Common.currentAccount)
            val editor: SharedPreferences.Editor = dataSave.edit()
            editor.putString(Constant.USER_INFO, json)
            editor.apply()
        }
        //Setup Bottom Navigation Bar
        navView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Timber.tag("MainActivity").d("onCreate: token from Firebase: $it")
            viewModel.updateFcmToken(it)
        }
        viewModel.getUserInfo()
        viewModel.getNotifyInfo()
        viewModel.notifyInfo.observe(this) { serverNotify ->
            Common.notifyCentral = serverNotify
            navView.getOrCreateBadge(R.id.navigation_message).also {
                it.displayBadge(serverNotify.totalUnreadMessage)
            }
            navView.getOrCreateBadge(R.id.navigation_history).also {
                it.displayBadge(serverNotify.totalMissedCall)
            }
            navView.getOrCreateBadge(R.id.navigation_contact).also {
                it.displayBadge(serverNotify.totalRequestAddFriend)
            }
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
    fun BadgeDrawable.displayBadge(badgeNumber: Int) {
        if (badgeNumber > 0) {
            this.apply {
                isVisible = true
                number = badgeNumber
            }
        } else {
            this.apply {
                isVisible = false
                clearNumber()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(internalBroadcastReceiver, IntentFilter(CHAT_REQUEST))
            registerReceiver(internalBroadcastReceiver, IntentFilter(CALL_RESPONSE))
            registerReceiver(internalBroadcastReceiver, IntentFilter(ADD_FRIEND))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).apply {
            unregisterReceiver(internalBroadcastReceiver)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        clearFocusOnOutsideClick()
        return super.dispatchKeyEvent(event)
    }

    private val internalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            Timber.tag("MainActivity").d("onReceive: called, intent action is :${intent.action}")
            when (intent.action) {
                ADD_FRIEND -> {
                    navView.getOrCreateBadge(R.id.navigation_contact).also {
                        Common.notifyCentral.totalRequestAddFriend++
                        it.displayBadge(Common.notifyCentral.totalRequestAddFriend)
                    }
                }
                CALL_RESPONSE -> {
                    val data = intent.getInfoExtra<RequestCall>(Constant.REMOTE_MSG_CALLER_INFO)
                    if (data.response == Constant.REMOTE_RESPONSE_MISSED) {
                        navView.getOrCreateBadge(R.id.navigation_history).also {
                            Common.notifyCentral.totalMissedCall++
                            it.displayBadge(Common.notifyCentral.totalMissedCall)
                        }
                    }
                }
                CHAT_REQUEST -> {
                    navView.getOrCreateBadge(R.id.navigation_message).also {
                        Common.notifyCentral.totalUnreadMessage++
                        it.displayBadge(Common.notifyCentral.totalUnreadMessage)
                    }
                }
            }
        }
    }
}