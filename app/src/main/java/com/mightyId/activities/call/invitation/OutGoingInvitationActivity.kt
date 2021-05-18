package com.mightyId.activities.call.invitation

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mightyId.R
import com.mightyId.activities.base.BaseActivity
import com.mightyId.databinding.ActivityOutgoingInvitationBinding
import com.mightyId.utils.*
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.CAMERA_REQUEST_CODE
import com.mightyId.utils.Constant.Companion.EXISTING_CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.IS_CALLING
import com.mightyId.utils.Constant.Companion.NOTIFICATION_MISSED_CALL_ID
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_CALL_DURATION
import com.mightyId.utils.Constant.Companion.PRIVACY_PRIVATE
import com.mightyId.utils.Constant.Companion.PRIVACY_PUBLIC
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_INFO
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_MEETING_TYPE
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_ACCEPTED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_ALLOWED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_DECLINED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_REJECTED
import com.mightyId.activities.call.joinExistingCall.RequestJoinMeetingDialogFragment
import com.mightyId.activities.main.ui.contact.ContactFragment
import com.mightyId.activities.main.ui.history.HistoryFragment
import com.mightyId.activities.main.ui.home.HomeFragment
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.activities.main.ui.message.chatRoom.listMember.ChatRoomListMember
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.activities.userDetails.addFriend.FragmentAddFriend
import com.mightyId.broadcast.ResponseReceiver
import com.mightyId.models.RequestCall
import com.mightyId.models.ResponseModel
import com.mightyId.models.server.ServerCallModel
import com.mightyId.notification.cancelNotification
import com.mightyId.utils.IntentUtils.getInfoExtra
import io.socket.client.Socket
import timber.log.Timber


class OutGoingInvitationActivity : BaseActivity(),
    RequestJoinMeetingDialogFragment.JoinCallDialogListener, View.OnClickListener {

    private lateinit var binding: ActivityOutgoingInvitationBinding
    private lateinit var viewModel: InvitationViewModel
    private lateinit var meetingInfo: ResponseModel
    private lateinit var data: RequestCall
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var viewFinder: PreviewView
    private lateinit var sendMissedCallHandler: Runnable
    private lateinit var tag: String

    private val progressDrawable by lazy { getProgressDrawable(this) }
    private val handler by lazy { Handler(Looper.myLooper()!!) }
    private val invitationResponseReceiver by lazy { ResponseReceiver() }
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var isSpeakerOn = false
    private var isVideoOn = false
    private var isCallConnected = false
    private var isRequestJoinExistMeeting = false
    private lateinit var socket: Socket

    private var listGroupCall = arrayListOf<String>()
    private var meetingType: String = ""
    private var topicId: String = ""
    private var topicType : String =""

    // Set up the preview use case to display camera preview.
    private val preview = Preview.Builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelNotification(NOTIFICATION_MISSED_CALL_ID)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_outgoing_invitation)
        viewModel = ViewModelProvider(this).get(InvitationViewModel::class.java)
        viewFinder = binding.previewView
        //Retrieve data from intent
        tag = intent.getStringExtra(Constant.TAG).toString()
        handleDataDependOnReceivedTag(tag)
        //Notify is calling - prevent any incoming call
        Common.commonInfo += IS_CALLING to 1

        //Set up video screen:
        establishingVideo()
        //Set up ringtone:
        establishingRingtone()
        //Observer View Model:
        observeViewModel()
        binding.outgoingEnd.setOnClickListener(this)
        binding.outgoingVideo.setOnClickListener(this)
        binding.outgoingSpeaker.setOnClickListener(this)
        Common.isConnected.observe(this) {
            if (!it) {
                Toast.makeText(this,getString(R.string.connection_lost),Toast.LENGTH_SHORT).show()
                handler.postDelayed({
                    finish()
                }, 500)
            }
        }
    }

    private fun establishingVideo() {
        if (meetingType == "video") {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            } else {
                setupFrontCamera()
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
                isVideoOn = true
                binding.previewView.visibility = View.VISIBLE
                binding.adPreview.visibility = View.INVISIBLE
                switchToActive(binding.outgoingVideo)
            }
        }
    }

    private fun handleDataDependOnReceivedTag(tag: String) {
        when (tag) {
            ChatRoomActivity.TAG -> {
                data = intent.getInfoExtra(REMOTE_MSG_CALLER_INFO)
                topicType = intent.getStringExtra(ChatRoomActivity.TOPIC_TYPE)!!
                Timber.tag("OutGoingInvitationActivity")
                    .d("handleDataDependOnReceivedTag: Called. type :$topicType")
                binding.outgoingCallerName.text = data.callerName
                binding.outgoingCallerAvatar.loadImage(data.callerPhotoURL, progressDrawable)
                if (topicType == "private") {
                    listGroupCall.add(data.callerCustomerId!!)
                    binding.outgoingMessage.text =
                        getString(R.string.outgoing_message, data.callerName)
                } else {
                    topicId = data.topicId!!
                    binding.outgoingMessage.text = getString(R.string.group_calling)
                }
                Timber.tag("OutGoingInvitationActivity")
                    .d("handleDataDependOnReceivedTag: data received: $data")
                meetingType = data.meetingType!!
                establishingCallConnection(listGroupCall, topicId, meetingType, topicType)
            }
            ContactFragment.TAG_GROUP -> {
                topicType = PRIVACY_PUBLIC
                listGroupCall = intent.getStringArrayListExtra(Constant.REMOTE_MSG_GROUP_CALL)!!
                meetingType = intent.getStringExtra(REMOTE_MSG_MEETING_TYPE)!!
                establishingCallConnection(listGroupCall, topicId, meetingType, topicType)
            }
            HistoryFragment.TAG_PUBLIC -> {
                topicType = PRIVACY_PUBLIC
                data = intent.getInfoExtra(REMOTE_MSG_CALLER_INFO)
                binding.outgoingCallerName.text = data.callerName
                binding.outgoingMessage.text = getString(R.string.group_calling)
                binding.outgoingCallerAvatar.loadImage(data.callerPhotoURL, progressDrawable)
                establishingCallConnection(listGroupCall,
                    data.topicId,
                    data.meetingType!!,
                    topicType)
            }
            ContactFragment.TAG,
            UserDetailActivity.TAG,
            ChatRoomListMember.TAG,
            Constant.NOTIFICATION_TAG,
            HomeFragment.TAG,
            HistoryFragment.TAG_PRIVATE,
            FragmentAddFriend.TAG,
            -> {
                topicType = PRIVACY_PRIVATE
                data = intent.getInfoExtra(REMOTE_MSG_CALLER_INFO)
                binding.outgoingCallerName.text = data.callerName
                binding.outgoingMessage.text = getString(R.string.outgoing_message, data.callerName)
                binding.outgoingCallerAvatar.loadImage(data.callerPhotoURL, progressDrawable)
                meetingType = data.meetingType!!
                listGroupCall.add(data.callerCustomerId!!)
                establishingCallConnection(listGroupCall, topicId, meetingType, topicType)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupFrontCamera()
                    binding.outgoingVideo.performClick()
                }
            }
        }
    }

    private fun setupFrontCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Choose the camera by requiring a lens facing
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            // Attach use cases to the camera with the same lifecycle owner
            @Suppress("UNUSED_VARIABLE")
            val camera = cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun establishingCallConnection(
        listGroupCall: ArrayList<String>?,
        topicId: String?,
        meetingType: String,
        privacyMode: String,
    ) {
        val serverCallModel = ServerCallModel(listGroupCall, meetingType, topicId, privacyMode)
        viewModel.sendRequestCall(serverCallModel)
    }

    private fun establishingRingtone() {
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.interal_call_sound)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(this@OutGoingInvitationActivity, uri)
            setAudioAttributes(audioAttributes)
            isLooping = true
            prepare()
        }
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
            0
        )
    }

    private fun cancelCallRequest() {
        //Send response missed call to the receiver
        handler.removeCallbacks(sendMissedCallHandler)
        mediaPlayer.stop()
        handler.postDelayed({ finish() }, 1000)
        if (isRequestJoinExistMeeting) {
            binding.outgoingMessage.text = getString(R.string.cancel_join_request)
            viewModel.cancelJoinExistingMeeting(listGroupCall[0])
        } else {
            binding.outgoingMessage.text = getString(R.string.message_call_end)
            val response = Constant.REMOTE_RESPONSE_MISSED
            viewModel.sendResponseRequestCall(meetingInfo.callId, response, meetingInfo.meetingId)
        }
    }

    private fun acceptCallRequest() {
        isCallConnected = true
        JitsiMeetUtils.establishConnection(meetingInfo.serverMeet)
        val jitsiConnection =
            JitsiMeetUtils.configurationMeeting(
                data,
                meetingType,
                viewModel.meetingInfo.value!!.meetingId
            )
        Timber.tag("OutGoingInvitationActivity").d("acceptCallRequest: $topicId")
        JitsiMeetUtils.launch(this, jitsiConnection, meetingInfo.callId,
            meetingInfo.topicId,topicType)
        finish()
    }

    private fun observeViewModel() {
        //Observe call status (Accepted / Rejected)
        invitationResponseReceiver.responseStatus.observe(this, { responseStatus ->
            when (responseStatus) {
                REMOTE_RESPONSE_ACCEPTED -> {
                    //Both parties accepted call - establish connection with Jitsi
                    JitsiMeetUtils.establishConnection(meetingInfo.serverMeet)
                    handler.removeCallbacks(sendMissedCallHandler)
                    //Handle connection...
                    acceptCallRequest()
                    finish()
                }
                REMOTE_RESPONSE_REJECTED -> {
                    //Call rejected by receiver
                    binding.outgoingMessage.text = getString(R.string.call_rejected)
                    Toast.makeText(this, getString(R.string.call_rejected), Toast.LENGTH_SHORT)
                        .show()
                    handler.postDelayed({ finish() }, 1000)
                }
            }
        })
        viewModel.meetingInfo.observe(this, {
            meetingInfo = it
            if (it.inPrivateCall == true) {
                //Receiver are in private call.Prevent user to send call request.
                binding.outgoingMessage.text = getString(
                    R.string.message_private_meeting,
                    data.callerName
                )
                handler.postDelayed({ finish() }, 1000)
            }
//            if (it.inPrivateCall == false) {
//                //Receiver are in public call. Pop up a dialog to confirm to join that call
//                val dialog = RequestJoinMeetingDialogFragment()
//                val bundle = Bundle()
//                bundle.putString(REMOTE_MSG_CALLER_NAME, data.callerName)
//                dialog.arguments = bundle
//                dialog.show(supportFragmentManager, "isCalling=true")
//            }
        })
    }

    private fun switchToInActive(view: ImageView) {
        view.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.primary_color, theme))
        view.imageTintList =
            ColorStateList.valueOf(resources.getColor(R.color.white, theme))
    }

    private fun switchToActive(view: ImageView) {
        view.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.white, theme))
        view.imageTintList =
            ColorStateList.valueOf(resources.getColor(R.color.primary_color, theme))
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer.start()
        LocalBroadcastManager.getInstance(applicationContext).apply {
            registerReceiver(invitationResponseReceiver, IntentFilter(CALL_RESPONSE))
            registerReceiver(internalReceiver, IntentFilter(EXISTING_CALL_RESPONSE))
        }
        /*Handle event when user won't pick up the call
          Auto cancel request call and send missed call notification*/
        sendMissedCallHandler = Runnable {
            if (isRequestJoinExistMeeting) {
                binding.outgoingMessage.text = getString(R.string.request_timed_out)
                Toast.makeText(this, getString(R.string.request_timed_out), Toast.LENGTH_SHORT)
                    .show()
                viewModel.cancelJoinExistingMeeting(listGroupCall[0])
            } else {
                Timber.tag("OutGoingInvitationActivity").d("onStart: Missed action called")
                binding.outgoingMessage.text = getString(R.string.user_not_available)
                val response = Constant.REMOTE_RESPONSE_MISSED
                viewModel.sendResponseRequestCall(meetingInfo.callId,
                    response,
                    meetingInfo.meetingId)
            }
            handler.postDelayed({ finish() }, 1000)
        }
        handler.postDelayed(sendMissedCallHandler, NOTIFICATION_REQUEST_CALL_DURATION.toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
        Common.commonInfo += IS_CALLING to 0
        mediaPlayer.release()
        LocalBroadcastManager.getInstance(applicationContext).apply {
            unregisterReceiver(invitationResponseReceiver)
            unregisterReceiver(internalReceiver)
        }
    }

    override fun onCancelJoin(dialog: DialogFragment) {
        Timber.tag("OutGoingInvitationActivity").d("onCancelJoin: Called")
        dialog.dismiss()
        cancelCallRequest()
    }

    override fun onRequestJoin(dialog: DialogFragment) {
        Timber.tag("OutGoingInvitationActivity").d("onRequestJoin: Called")
        isRequestJoinExistMeeting = true
        dialog.dismiss()
        viewModel.requestJoinExistingMeeting(
            listGroupCall[0],
            viewModel.meetingInfo.value!!.callId,
            viewModel.meetingInfo.value!!.topicId)
        isCallConnected = true
        binding.pendingJoinExistingMeeting.visibility = View.VISIBLE
    }

    override fun finish() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        super.finish()
    }

    private val internalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getInfoExtra<RequestCall>(EXISTING_CALL_RESPONSE)
            Timber.tag("OutGoingInvitationActivity").d("onReceive: data: $data")
            when (data.response) {
                REMOTE_RESPONSE_ALLOWED -> {
                    Toast.makeText(this@OutGoingInvitationActivity,
                        getString(R.string.message_accept_join),
                        Toast.LENGTH_LONG).show()
                    acceptCallRequest()
                    viewModel.updateJoinedState(
                        viewModel.meetingInfo.value!!.callId,
                        viewModel.meetingInfo.value!!.privacyMode,
                        viewModel.meetingInfo.value!!.topicId)
                }

                REMOTE_RESPONSE_DECLINED -> {
                    if (data.privacyMode == PRIVACY_PRIVATE) {
                        Toast.makeText(this@OutGoingInvitationActivity,
                            getString(R.string.message_decline_join),
                            Toast.LENGTH_LONG).show()
                        cancelCallRequest()
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.outgoingEnd -> {
                cancelCallRequest()
            }
            binding.outgoingVideo -> {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                } else {
                    if (isVideoOn) {
                        //Turn off camera
                        meetingType = "audio"
                        binding.previewView.visibility = View.INVISIBLE
                        switchToInActive(binding.outgoingVideo)
                    } else {
                        //Turn on camera
                        meetingType = "video"
                        binding.adPreview.visibility = View.INVISIBLE
                        preview.setSurfaceProvider(viewFinder.surfaceProvider)
                        binding.previewView.visibility = View.VISIBLE
                        switchToActive(binding.outgoingVideo)
                    }
                    isVideoOn = !isVideoOn
                }
            }
            binding.outgoingSpeaker -> {
                if (isSpeakerOn) {
                    //Turn off speaker
                    switchToInActive(binding.outgoingSpeaker)
                    binding.outgoingStatus.visibility = View.INVISIBLE
                } else {
                    //Turn on speaker
                    switchToActive(binding.outgoingSpeaker)
                    binding.outgoingStatus.visibility = View.VISIBLE
                }
                isSpeakerOn = !isSpeakerOn
                audioManager.isSpeakerphoneOn = isSpeakerOn
            }
        }
    }
}