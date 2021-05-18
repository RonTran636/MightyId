package com.mightyId.utils

import androidx.lifecycle.MutableLiveData
import com.mightyId.models.Account
import com.mightyId.models.server.ServerNotify

object Common {
    var currentAccount : Account? = null
    /**COMMON INFO CONTAIN:
     * ON_CHAT: Boolean
     * TOTAL_UNREAD_MESSAGE: Int
     * TOTAL_MISSED_CALL : Int
     *
     *
     */
    var commonInfo : Map<String,Int> = mutableMapOf()
    var notifyCentral = ServerNotify()
    var isConnected = MutableLiveData<Boolean>()
    var topicType : String? =null
    var isForeground :Boolean = false
    var BASE_URL =""
    var SOCKET_URL=""
}