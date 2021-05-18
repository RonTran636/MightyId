package com.mightyId.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mightyId.models.RequestCall
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.EXISTING_CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_INFO
import com.mightyId.utils.IntentUtils.getInfoExtra
import timber.log.Timber

class ResponseReceiver : BroadcastReceiver() {

    private val _responseStatus = MutableLiveData<String>()
    val responseStatus: LiveData<String> = _responseStatus

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag("ResponseReceiver").d("onReceive: action is ${intent.action}")
        when (intent.action) {
            CALL_RESPONSE -> {
                val data = intent.getInfoExtra<RequestCall>(REMOTE_MSG_CALLER_INFO)
                _responseStatus.value = data.response!!
            }
            EXISTING_CALL_RESPONSE -> {
                //TODO: change this
                val data = intent.getInfoExtra<RequestCall>(REMOTE_MSG_CALLER_INFO)
                _responseStatus.value = data.response!!
            }
        }
    }
}

