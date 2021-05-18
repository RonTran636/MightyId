package com.mightyId.activities.base

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.common.api.internal.ConnectionCallbacks
import com.mightyId.R
import com.mightyId.utils.Common
import com.mightyId.utils.hideKeyboard
import timber.log.Timber

@Suppress("DEPRECATION")
open class BaseActivity:AppCompatActivity(),LifecycleObserver,ConnectionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Handle Fullscreen Mode
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(true)
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            }else{
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            statusBarColor = Color.TRANSPARENT
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    Common.isConnected.value = true
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    Common.isConnected.value = false
                }
            }
        }
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Timber.tag("ConnectivityWorker").e("doWork: NetworkCallback for Wi-fi was not registered or already unregistered")
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Common.isForeground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Common.isForeground = true
    }

    override fun onConnected(p0: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }
}