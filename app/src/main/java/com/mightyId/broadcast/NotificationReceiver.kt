package com.mightyId.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mightyId.R
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.MessageItem
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.ADD_FRIEND
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.CHAT_REQUEST
import com.mightyId.utils.Constant.Companion.DIRECT_REPLY
import com.mightyId.utils.Constant.Companion.EXISTING_CALL_REQUEST
import com.mightyId.utils.Constant.Companion.NOTIFICATION_MESSAGE_ID
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_ADD_FRIEND_ACCEPTED
import com.mightyId.utils.Constant.Companion.REMOTE_MSG_ADD_FRIEND_REJECTED
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_ACCEPTED
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_CANCEL
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_REJECTED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_ALLOWED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_DECLINED
import com.mightyId.utils.IntentUtils.getInfoExtra
import com.mightyId.utils.JitsiMeetUtils
import com.mightyId.workManager.WorkerAddFriendConfirmedNotification
import com.mightyId.models.RequestAddFriendModel
import com.mightyId.models.RequestCall
import com.mightyId.models.RequestJoinExistCall
import com.mightyId.notification.cancelNotification
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import timber.log.Timber
import java.lang.reflect.Type

class NotificationReceiver : BroadcastReceiver() {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()

    override fun onReceive(context: Context, intent: Intent) {
//        val keyguardManager =
//            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        when (intent.action) {
            REMOTE_NOTIFICATION_ACCEPTED -> {
                val userData = intent.getInfoExtra<RequestCall>(CALL_RESPONSE)
                Timber.tag("NotificationReceiver").d("onReceive: userData: $userData")
                context.cancelNotification(Constant.NOTIFICATION_REQUEST_CALL_ID)
                performClickAction(context, userData, Constant.REMOTE_RESPONSE_ACCEPTED)
            }
            REMOTE_NOTIFICATION_REJECTED -> {
                val userData = intent.getInfoExtra<RequestCall>(CALL_RESPONSE)
                context.cancelNotification(Constant.NOTIFICATION_REQUEST_CALL_ID)
                performClickAction(context, userData, Constant.REMOTE_RESPONSE_REJECTED)
            }
            REMOTE_MSG_ADD_FRIEND_ACCEPTED -> {
                val userData = intent.getInfoExtra<RequestAddFriendModel>(ADD_FRIEND)
                removeFromPendingList(context, userData)
                context.cancelNotification(Constant.NOTIFICATION_REQUEST_ADD_FRIEND_ID)
                sendResponseAccept(userData)
                //Show Add friend confirm notification
                val dataToJson = Gson().toJson(userData)
                val inputData = Data.Builder()
                    .putString("requestAddFriend", dataToJson)
                    .build()
                val workRequest =
                    OneTimeWorkRequest.Builder(WorkerAddFriendConfirmedNotification::class.java)
                        .setInputData(inputData)
                        .addTag("requestAddFriend")
                        .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            REMOTE_MSG_ADD_FRIEND_REJECTED -> {
                val userData = intent.getInfoExtra<RequestAddFriendModel>(ADD_FRIEND)
                removeFromPendingList(context, userData)
                context.cancelNotification(Constant.NOTIFICATION_REQUEST_ADD_FRIEND_ID)
                sendResponseDecline(userData)
            }
            REMOTE_NOTIFICATION_CANCEL -> {
                context.cancelNotification(Constant.NOTIFICATION_MISSED_CALL_ID)
            }
            REMOTE_RESPONSE_ALLOWED -> {
                val userData = intent.getInfoExtra<RequestJoinExistCall>(EXISTING_CALL_REQUEST)
                Timber.tag("NotificationReceiver").d("REMOTE_RESPONSE_ALLOWED: $userData")
                responseJoinExistingMeeting(REMOTE_RESPONSE_ALLOWED,
                    userData.fromCustomerId,
                    userData.callId,
                    userData.topicId)
                context.cancelNotification(Constant.NOTIFICATION_REQUEST_EXIST_CALL)
            }
            REMOTE_RESPONSE_DECLINED -> {
                val userData = intent.getInfoExtra<RequestJoinExistCall>(EXISTING_CALL_REQUEST)
                Timber.tag("NotificationReceiver").d("REMOTE_RESPONSE_DECLINED: $userData")
                responseJoinExistingMeeting(REMOTE_RESPONSE_DECLINED,
                    userData.fromCustomerId,
                    userData.callId,
                    userData.topicId)
                context.cancelNotification(Constant.NOTIFICATION_REQUEST_EXIST_CALL)
            }
            DIRECT_REPLY -> {
                val messageData = intent.getInfoExtra<MessageItem>(CHAT_REQUEST)
                sendMessage(context, messageData.topicId!!, messageData.messageContent!!)
            }
        }
    }

    private fun performClickAction(context: Context, userData: RequestCall, response: String) {
        //Send confirmation to caller - Whether accept or decline
        sendResponseRequestCall(userData.callId!!, response, userData.topicId!!)
        if (response == Constant.REMOTE_RESPONSE_ACCEPTED) {
            //Accepted call - Establish connection
            JitsiMeetUtils.establishConnection(userData.serverMeet!!)
            val jitsiConnection = JitsiMeetUtils.configurationMeeting(userData)
            launchFromBroadcast(context, jitsiConnection, userData.callId!!)
        }
    }

    private fun removeFromPendingList(context: Context, requestAddFriend: RequestAddFriendModel) {
        val sharedPreferences = context.getSharedPreferences(
            Constant.INVITE_USER_INFO,
            Context.MODE_PRIVATE
        )
        val gson = Gson()
        val json = sharedPreferences.getString(Constant.PENDING_FRIEND_LIST, "")
        val type: Type = object : TypeToken<MutableList<RequestAddFriendModel>>() {}.type
        val pendingList: MutableList<RequestAddFriendModel>? = gson.fromJson(json, type)
        pendingList?.remove(requestAddFriend)
        val editor = sharedPreferences.edit()
        editor.putString(Constant.PENDING_FRIEND_LIST, gson.toJson(pendingList))
        editor.apply()
    }

    private fun sendResponseAccept(userData: RequestAddFriendModel) {
        disposable.add(
            myService.responseAcceptFriend(userData.senderId!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("sendResponseAccept").d("onComplete")
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("onError").e(e)
                    }
                })
        )
    }

    private fun sendResponseDecline(userData: RequestAddFriendModel) {
        disposable.add(
            myService.responseDeclineFriend(userData.senderId!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("sendResponseDecline").d("onComplete")
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("onError").e(e)
                    }
                })
        )
    }

    private fun sendResponseRequestCall(
        callId: String,
        responseAction: String,
        topicId: String,
    ) {
        disposable.add(
            myService.sendResponseRequestCall(callId, responseAction, topicId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("onComplete").d("onComplete")
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("onError").e(e)
                    }
                })
        )
    }

    private fun responseJoinExistingMeeting(
        action: String,
        toCustomerId: String,
        callId: String,
        topicId: String,
    ) {
        disposable.add(
            myService.responseJoinExistingMeeting(action, toCustomerId, callId, topicId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("NotificationReceiver").d("onComplete: sendResponseJoinCall")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("NotificationReceiver").e("sendResponseJoinCall onError: $e")
                    }
                })
        )
    }

    private fun launchFromBroadcast(
        context: Context,
        options: JitsiMeetConferenceOptions,
        callId: String,
    ) {
        val intent = Intent(context, JitsiMeetActivity::class.java)
        intent.action = "org.jitsi.meet.CONFERENCE"
        intent.putExtra("call_id", callId)
        intent.putExtra("JitsiMeetConferenceOptions", options)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun sendMessage(context: Context, topicId: String, messageContent: String) {
        disposable.add(
            myService.sendMessage(topicId, messageContent, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        context.cancelNotification(NOTIFICATION_MESSAGE_ID)
                        Toast.makeText(context,
                            context.getString(R.string.message_sent),
                            Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("sendMessage onError: $e")
                    }
                })

        )
    }
}