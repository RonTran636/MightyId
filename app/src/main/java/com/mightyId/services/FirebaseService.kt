package com.mightyId.services

import android.content.Intent
import android.content.SharedPreferences
import android.os.Looper
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mightyId.activities.main.ui.message.home.MessageFragment.Companion.NEW_MESSAGE
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.*
import com.mightyId.notification.*
import com.mightyId.utils.Common
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.ADD_FRIEND
import com.mightyId.utils.Constant.Companion.ADD_FRIEND_ACCEPTED
import com.mightyId.utils.Constant.Companion.ASSIGN_TASK
import com.mightyId.utils.Constant.Companion.CALL_ACCEPTED_ELSE_WHERE
import com.mightyId.utils.Constant.Companion.CALL_REQUEST
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.CHAT_REQUEST
import com.mightyId.utils.Constant.Companion.EXISTING_CALL_REQUEST
import com.mightyId.utils.Constant.Companion.EXISTING_CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.MEMBER_IN_TOPIC
import com.mightyId.utils.Constant.Companion.MESSAGE_TYPE
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_CALL_ID
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_EXIST_CALL
import com.mightyId.utils.Constant.Companion.ON_CHAT
import com.mightyId.utils.Constant.Companion.PENDING_FRIEND_LIST
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_INFO
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_CALLER_NAME
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_EXISTING_CALL_CANCEL
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_MEETING_TYPE
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_MISSED
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.IntentUtils.retrieveDataFromFcm
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jitsi.meet.sdk.BroadcastIntentHelper
import timber.log.Timber
import java.lang.reflect.Type


class FirebaseService : FirebaseMessagingService() {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.tag("FirebaseService").d("onNewToken: token from Firebase: $token")
        val dataSave: SharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val json = dataSave.getString(Constant.USER_INFO, "")
        if (!json.isNullOrEmpty()) {
            Common.currentAccount?.fcmToken = token
            //Update fcm token on server's database
            updateFcmToken(token)
        }
    }

    private fun updateFcmToken(token: String?) {
        disposable.add(
            myService.updateFcmToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("updateFcmToken").d("onComplete: Token updated")
                        Common.currentAccount!!.fcmToken = token
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("updateFcmToken").e("onError: $e")
                    }
                })
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.tag("FirebaseService").d("onMessageReceived: called, data is ${message.data}")
//        super.onMessageReceived(message)
        if (message.data.isNotEmpty()) {
            when (message.data[MESSAGE_TYPE]) {
                CALL_REQUEST -> {
                    //Call Request - Retrieve caller data from Service
                    val data = retrieveDataFromFcm<RequestCall>(message)
                    Timber.tag("FirebaseService").d("onMessageReceived CALL_REQUEST: data:$data")
                    if (Common.isForeground) {
                        showIncomingInvitationActivity(data)
                    } else {
                        showNotificationWithFullScreenIntent(data)
                    }
                }
                CALL_RESPONSE -> {
                    //Response call Request - Retrieve confirm code
                    val data = retrieveDataFromFcm<RequestCall>(message)
                    Timber.tag("FirebaseService").d("onMessageReceived CALL_RESPONSE: data: $data")
                    val intent = Intent(CALL_RESPONSE)
                    if (data.response == REMOTE_RESPONSE_MISSED) {
                        //TODO: Reform it better later
                        val requestCall = RequestCall(CALL_REQUEST)
                        requestCall.callerName = message.data[REMOTE_MSG_CALLER_NAME]
                        requestCall.callerCustomerId = message.data["fromCustomerId"]
                        requestCall.meetingType = message.data[REMOTE_MSG_MEETING_TYPE]
                        Timber.tag("FirebaseService").d("onMessageReceived: $requestCall")
                        cancelNotification(NOTIFICATION_REQUEST_CALL_ID)
                        showMissedCallNotification(requestCall)
                    }
                    intent.putInfoExtra(REMOTE_MSG_CALLER_INFO, data)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
                EXISTING_CALL_RESPONSE -> {
                    val data = retrieveDataFromFcm<RequestCall>(message)
                    val intent = Intent(EXISTING_CALL_RESPONSE)
                    intent.putInfoExtra(EXISTING_CALL_RESPONSE, data)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
                EXISTING_CALL_REQUEST -> {
                    val data = retrieveDataFromFcm<RequestJoinExistCall>(message)
                    val intent = Intent(EXISTING_CALL_REQUEST)
                    showRequestJoinNotification(data)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
                REMOTE_MSG_EXISTING_CALL_CANCEL -> {
                    cancelNotification(NOTIFICATION_REQUEST_EXIST_CALL)
                }
                ADD_FRIEND -> {
                    sharedPreferences =
                        getSharedPreferences(Constant.INVITE_USER_INFO, MODE_PRIVATE)
                    //Save user's request into share preference
                    val gson = Gson()
                    val intent = Intent(ADD_FRIEND)
                    val data = retrieveDataFromFcm<RequestAddFriendModel>(message)
                    Timber.tag("FirebaseService")
                        .d("onMessageReceived ADD_FRIEND: data is :$data")
                    //Retrieve exist pending request list:
                    val json = sharedPreferences.getString(PENDING_FRIEND_LIST, "")
                    val type: Type =
                        object : TypeToken<MutableList<RequestAddFriendModel>>() {}.type
                    var pendingList: MutableList<RequestAddFriendModel>? = gson.fromJson(json, type)
                    if (pendingList.isNullOrEmpty()) pendingList = arrayListOf()
                    if (pendingList.size == 2) {
                        pendingList.add(data)
                        pendingList.removeFirst()
                    } else {
                        pendingList.add(data)
                    }
                    //Add new request into share preference
                    Timber.tag("FirebaseService").d("onMessageReceived: pending list: $pendingList")
                    val editor = sharedPreferences.edit()
                    editor.putString(PENDING_FRIEND_LIST, gson.toJson(pendingList))
                    editor.apply()
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                    showAddFriendNotification(data)
                }
                CHAT_REQUEST -> {
                    val data = retrieveDataFromFcm<MessageItem>(message)
                    val intent = Intent(CHAT_REQUEST).apply {
                        putInfoExtra(NEW_MESSAGE, data)
                    }
                    Timber.tag("FirebaseService")
                        .d("onMessageReceived CHAT_REQUEST: $data")
                    if (data.customerId != Common.currentAccount!!.customerId) {
                        Timber.tag("FirebaseService")
                            .d("onMessageReceived: are on chat: ${Common.commonInfo[ON_CHAT]}")
                        if (Common.commonInfo[ON_CHAT] == 0 || Common.commonInfo[ON_CHAT] == null) showMessageNotification(
                            data)
                    }
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
                ADD_FRIEND_ACCEPTED -> {
                    val data = retrieveDataFromFcm<AcceptFriend>(message)
                    Timber.tag("FirebaseService")
                        .d("onMessageReceived ADD_FRIEND_ACCEPTED: data is :$data")
                    val account = Account()
                    account.customerId = data.senderId
                    account.customerName = data.senderName
                    account.photoUrl = data.senderPhotoUrl
                    addFriendConfirmedNotification(account)
                }
                MEMBER_IN_TOPIC -> {
                    Timber.tag("FirebaseService").d("onMessageReceived: MEMBER_IN_TOPIC = 0")
                    Looper.prepare()
                    Toast.makeText(this, "Call ended", Toast.LENGTH_LONG).show()
                    val intent = BroadcastIntentHelper.buildHangUpIntent()
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                ASSIGN_TASK -> {
                    val data = retrieveDataFromFcm<TodoListItem>(message)
                    val intent = Intent(ASSIGN_TASK)
                    Timber.tag("FirebaseService").d("onMessageReceived ASSIGN_TASK: data is :$data")
//                    showTodoNotification(data)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
                CALL_ACCEPTED_ELSE_WHERE -> {
                    Timber.tag("FirebaseService").d("onMessageReceived CALL_ACCEPTED_ELSE_WHERE: Called")
                    Intent(CALL_ACCEPTED_ELSE_WHERE).apply {
                        LocalBroadcastManager.getInstance(this@FirebaseService).sendBroadcast(this)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}
