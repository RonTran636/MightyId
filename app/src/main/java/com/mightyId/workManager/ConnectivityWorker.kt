package com.mightyId.workManager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber

class ConnectivityWorker(context: Context, parameters: WorkerParameters): Worker(context,parameters) {

    private val _context = context
    override fun doWork(): Result {
        val connectivityManager = _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
            }

            override fun onLost(network: Network) {
            }
        }
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Timber.tag("ConnectivityWorker").e("doWork: NetworkCallback for Wi-fi was not registered or already unregistered")
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        return Result.success()
    }
}