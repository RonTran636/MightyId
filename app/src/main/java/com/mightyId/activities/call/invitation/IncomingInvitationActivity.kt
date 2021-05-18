package com.mightyId.activities.call.invitation

import android.content.*
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.databinding.ActivityIncomingInvititationBinding
import com.mightyId.utils.*
import com.mightyId.broadcast.ResponseReceiver
import com.mightyId.models.Account
import com.mightyId.models.RequestCall
import com.mightyId.notification.cancelNotification
import com.mightyId.utils.Constant.Companion.CALL_ACCEPTED_ELSE_WHERE
import com.mightyId.utils.Constant.Companion.CALL_REQUEST
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_CALL_ID
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_ACCEPTED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_MISSED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_REJECTED
import com.mightyId.utils.IntentUtils.getInfoExtra
import io.socket.client.IO
import io.socket.client.Socket
import timber.log.Timber

class IncomingInvitationActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityIncomingInvititationBinding
    private lateinit var viewModel: InvitationViewModel
    private lateinit var data: RequestCall
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var dataSave: SharedPreferences
    private lateinit var socket: Socket

    private val progressDrawable by lazy { getProgressDrawable(this) }
    private val handler by lazy { Handler(Looper.myLooper()!!) }
    private val invitationResponseReceiver by lazy { ResponseReceiver() }
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()
        hideKeyboard()
        //User Logged in - Retrieve data from shared preference
        dataSave = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val gson = Gson()
        val json = dataSave.getString(Constant.USER_INFO, "")
        if (!json.isNullOrEmpty()) {
            Common.currentAccount = gson.fromJson(json, Account::class.java)
        }
        Timber.tag("IncomingInvitationActivity")
            .d("onCreate: current volume mode : ${audioManager.isVolumeFixed}")
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            statusBarColor = Color.TRANSPARENT
        }
        audioManager.mode = AudioManager.MODE_IN_CALL
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            establishingRingtone()
        }

        //Integrate with socket
        try {
            socket = IO.socket(Common.SOCKET_URL)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag("ChatRoomActivity").e("establishConnectionWithSocket: $e")
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_incoming_invititation)
        binding.incomingCallerAccept.setOnClickListener(this)
        binding.incomingCallerReject.setOnClickListener(this)
        viewModel = ViewModelProvider(this).get(InvitationViewModel::class.java)
        //Retrieve Data from intent
        data = intent.getInfoExtra(CALL_REQUEST)
        Timber.tag("IncomingInvitationActivity").d("onCreate: data : $data")
        //Display UI accordingly
//        if (data.meetingType == "video") {
//            binding.incomingCallerAccept.setLottieImage(R.raw.caller_incoming_video_call)
//        }
        binding.incomingCallerName.text = data.callerName
        binding.incomingMessage.text =
            getString(R.string.incoming_message, data.meetingType, data.callerName)
        binding.incomingCallerAvatar.loadImage(data.callerPhotoURL, progressDrawable)
        observeViewModel()
        Common.isConnected.observe(this){
            if (!it) {
                Toast.makeText(this,getString(R.string.connection_lost),Toast.LENGTH_SHORT).show()
                handler.postDelayed({
                    finish()
                }, 500)
            }
        }
    }

    private fun establishingRingtone() {
//        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.very_funny_ring_tone)
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(this@IncomingInvitationActivity, uri)
            setAudioAttributes(audioAttributes)
            isLooping = true
            prepare()
        }
        audioManager.isSpeakerphoneOn = true
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
            0)
    }

    private fun observeViewModel() {
        //Observe call status (Missed call)
        invitationResponseReceiver.responseStatus.observe(this, { responseStatus ->
            if (responseStatus == REMOTE_RESPONSE_MISSED) {
                handler.postDelayed({
                    finish()
                }, 500)
            }
        })
        //Establish connection with Jitsi
        JitsiMeetUtils.establishConnection(data.serverMeet!!)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.incomingCallerAccept -> {
                //Accepted call - send confirmation back to the caller
                data.response = REMOTE_RESPONSE_ACCEPTED
                cancelNotification(NOTIFICATION_REQUEST_CALL_ID)
                Timber.tag("IncomingInvitationActivity").d("onClick: data: $data")
                viewModel.sendResponseRequestCall(data.callId!!, data.response!!, data.topicId!!)
                //Both parties accepted call , handle connection...
                Toast.makeText(this, "Call connecting", Toast.LENGTH_LONG).show()
                val jitsiConnection = JitsiMeetUtils.configurationMeeting(data)
                JitsiMeetUtils.launch(this, jitsiConnection, data.callId!!, data.topicId!!,data.privacyMode!!)
                finish()
            }
            binding.incomingCallerReject -> {
                //Rejected call - send confirmation back to the caller
                data.response = REMOTE_RESPONSE_REJECTED
                cancelNotification(NOTIFICATION_REQUEST_CALL_ID)
                binding.incomingMessage.text = getString(R.string.rejected_call)
                viewModel.sendResponseRequestCall(data.callId!!, data.response!!, data.topicId!!)
                handler.postDelayed({
                    finish()
                }, 1000)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            mediaPlayer.start()
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(CALL_RESPONSE)
        )
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            callAcceptedElseWhere,
            IntentFilter(CALL_ACCEPTED_ELSE_WHERE)
        )
    }

    override fun onStop() {
        super.onStop()
        Timber.tag("IncomingInvitationActivity").d("onStop: Called")
    }

    override fun onDestroy() {
        turnScreenOffAndKeyguardOn()
        Timber.tag("IncomingInvitationActivity").d("onDestroy: Called")
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            mediaPlayer.release()
        }
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            invitationResponseReceiver
        )
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            callAcceptedElseWhere
        )
        super.onDestroy()
    }

    private val callAcceptedElseWhere = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.incomingMessage.text = getString(R.string.call_accept_else_where)
            handler.postDelayed({
                finish()
            }, 500)
        }
    }

    //Handle mute by volume down
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val keycode = event?.keyCode
        val action = event?.action
        return when (keycode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    Timber.tag("IncomingInvitationActivity").d("dispatchKeyEvent: volume pressed")
                    audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_TOGGLE_MUTE,
                        0)
                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
                }
                true
            }
            else -> super.dispatchKeyEvent(event)
        }
    }
}