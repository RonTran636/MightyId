package com.mightyId.activities.call

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_ID
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity.Companion.TOPIC_TYPE
import com.mightyId.utils.Common
import com.mightyId.utils.turnScreenOffAndKeyguardOn
import com.mightyId.utils.turnScreenOnAndKeyguardOff
import io.socket.client.IO
import io.socket.client.Socket
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.json.JSONObject
import timber.log.Timber

class MeetingActivity : JitsiMeetActivity() {

    private lateinit var viewModel: MeetingViewModel
    private lateinit var socket: Socket
    private lateinit var callId: String
    private lateinit var topicId: String
    private lateinit var topicType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        turnScreenOnAndKeyguardOff()
        Timber.tag("MeetingActivity").d("onCreate: Called")
        callId = intent.getStringExtra("call_id").toString()
        topicId = intent.getStringExtra(TOPIC_ID).toString()
        topicType = intent.getStringExtra(TOPIC_TYPE).toString()
        Common.topicType = topicType
        viewModel = ViewModelProvider(this).get(
            MeetingViewModel::
        class.java)

        try {
            socket = IO.socket(Common.SOCKET_URL)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag("ChatRoomActivity").e("establishConnectionWithSocket: $e")
        }
//        Intent(this,MeetingService::class.java).also { startService(it) }
        socket.connect()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val jsonObject = JSONObject().apply {
            put("customer_id", Common.currentAccount!!.customerId)
            put(TOPIC_ID, topicId)
        }
        socket.emit("JOIN_CALL", jsonObject)
    }

    override fun onDestroy() {
        Timber.tag("MeetingActivity").d("onDestroy: Called")
        viewModel.updateLeftState(callId)
        val jsonObject = JSONObject().apply { put(TOPIC_ID, topicId) }
        socket.emit("END_CALL", jsonObject)
        turnScreenOffAndKeyguardOn()
        super.onDestroy()
    }
}