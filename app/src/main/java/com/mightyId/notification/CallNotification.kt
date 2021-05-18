package com.mightyId.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.os.Vibrator
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.mightyId.R
import com.mightyId.activities.call.invitation.IncomingInvitationActivity
import com.mightyId.activities.call.invitation.OutGoingInvitationActivity
import com.mightyId.activities.main.MainActivity
import com.mightyId.broadcast.NotificationReceiver
import com.mightyId.models.RequestCall
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.CALL_REQUEST
import com.mightyId.utils.Constant.Companion.CALL_RESPONSE
import com.mightyId.utils.Constant.Companion.CHANNEL_CALL_ID
import com.mightyId.utils.Constant.Companion.CHANNEL_NAME_INCOMING_CALL
import com.mightyId.utils.Constant.Companion.EXISTING_CALL_REQUEST
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_CALL_DURATION
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_CALL_ID
import com.mightyId.utils.Constant.Companion.NOTIFICATION_VIBRATE_PATTERN
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_ACCEPTED
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_ACTION_CALLBACK
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_CANCEL
import com.mightyId.utils.Constant.Companion.REMOTE_NOTIFICATION_REJECTED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_ALLOWED
import com.mightyId.utils.Constant.Companion.REMOTE_RESPONSE_DECLINED
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.loadImageToNotificationAvatar
import com.mightyId.models.RequestJoinExistCall
import java.util.*

fun Service.showNotificationWithFullScreenIntent(data: RequestCall) {
    val channelId: String = CHANNEL_CALL_ID
    //Handle Fullscreen Intent - Screen locked
    val lockedScreenAction = Intent(this, IncomingInvitationActivity::class.java)
    lockedScreenAction.putInfoExtra(CALL_REQUEST,data)
    lockedScreenAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    //Handle click on notification - Accept
    val receiveCallAction = Intent(this, NotificationReceiver::class.java)
    receiveCallAction.putExtra("id", NOTIFICATION_REQUEST_CALL_ID)
    receiveCallAction.putInfoExtra(CALL_RESPONSE,data)
    receiveCallAction.action = REMOTE_NOTIFICATION_ACCEPTED

    //Handle click on notification - Decline
    val cancelCallAction = Intent(this, NotificationReceiver::class.java)
    cancelCallAction.putExtra("id", NOTIFICATION_REQUEST_CALL_ID)
    cancelCallAction.putInfoExtra(CALL_RESPONSE,data)
    cancelCallAction.action = REMOTE_NOTIFICATION_REJECTED

    val receiveCallPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1200,
        receiveCallAction, PendingIntent.FLAG_UPDATE_CURRENT
    )
    val cancelCallPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1201,
        cancelCallAction,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val lockScreenIntent = PendingIntent.getActivity(
        this,
        1202,
        lockedScreenAction,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    //Customize UI of notification
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val avatar = loadImageToNotificationAvatar(data.callerPhotoURL)
    //Custom vibration for notification
    val vibrationEffect = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
//        vibrationEffect
//            .vibrate(VibrationEffect.createWaveform(NOTIFICATION_VIBRATE_PATTERN,3))
//    }else{
//        @Suppress("DEPRECATION")
//        vibrationEffect.vibrate(NOTIFICATION_VIBRATE_PATTERN, 3)
//    }

    val remoteView = RemoteViews(packageName, R.layout.holder_call_notification)
    if (avatar != null) {
        remoteView.setImageViewBitmap(R.id.noti_avatar, avatar)
    } else {
        remoteView.setImageViewResource(R.id.noti_avatar, R.drawable.ic_avatar_default)
    }

    remoteView.setTextViewText(R.id.noti_name, data.callerName)
    remoteView.setTextViewText(
        R.id.noti_message,
        getString(
            R.string.notification_call_message,
            data.meetingType!!.capitalize(Locale.getDefault())
        )
    )
    remoteView.setOnClickPendingIntent(R.id.noti_accept, receiveCallPendingIntent)
    remoteView.setOnClickPendingIntent(R.id.noti_decline, cancelCallPendingIntent)

    Looper.prepare()
    val builder = NotificationCompat.Builder(this, channelId)
        .setCustomHeadsUpContentView(remoteView)
        .setSmallIcon(R.drawable.notification_icon)
        .setContent(remoteView)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(lockScreenIntent)
        .setAutoCancel(true)
        .setOngoing(true)
        .setTimeoutAfter(NOTIFICATION_REQUEST_CALL_DURATION.toLong())
         //Rebase delete into User's detail
        .setFullScreenIntent(lockScreenIntent, true)

    with(notificationManager) {
        buildChannel()
        val notification = builder.build()
        notify(NOTIFICATION_REQUEST_CALL_ID, notification)
    }

}

fun Context.showIncomingInvitationActivity(data: RequestCall) {
    val intent = Intent(this, IncomingInvitationActivity::class.java)
    intent.putInfoExtra(CALL_REQUEST,data)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.showMissedCallNotification(data: RequestCall){
    val channelId = CHANNEL_CALL_ID
    //Handle click on notification - Callback
    val callbackAction = Intent(this, OutGoingInvitationActivity::class.java)
    callbackAction.putInfoExtra(Constant.REMOTE_MSG_CALLER_INFO, data)
    callbackAction.putExtra(Constant.TAG, Constant.NOTIFICATION_TAG)
    callbackAction.action = REMOTE_NOTIFICATION_ACTION_CALLBACK

    //Handle click on notification - Cancel notification
    val cancelAction = Intent(this, NotificationReceiver::class.java)
    cancelAction.action = REMOTE_NOTIFICATION_CANCEL

    //Handle click on notification - Show Call History
    val openHistoryPendingIntent = NavDeepLinkBuilder(this)
        .setComponentName(MainActivity::class.java)
        .setGraph(R.navigation.mobile_navigation)
        .setDestination(R.id.navigation_history)
        .createPendingIntent()

    val callbackActionPendingIntent = PendingIntent.getActivity(
        applicationContext,
        1200,
        callbackAction,
        PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val cancelPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1205,
        cancelAction,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val avatar = loadImageToNotificationAvatar(data.callerPhotoURL)
    
    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getString(R.string.missed_call))
        .setContentText(data.callerName?.capitalize(Locale.getDefault()))
        .setContentIntent(openHistoryPendingIntent)
        .setLargeIcon(avatar)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setColor(resources.getColor(R.color.accent_red,theme))
        .addAction(R.drawable.ic_baseline_clear_24,"Cancel",cancelPendingIntent)
        .addAction(R.drawable.ic_phone,"Callback",callbackActionPendingIntent)

    with(notificationManager) {
        buildChannel()
        val notification = builder.build()
        notify(Constant.NOTIFICATION_MISSED_CALL_ID, notification)
    }
}

fun Service.showRequestJoinNotification(data: RequestJoinExistCall){
    val channelId: String = CHANNEL_CALL_ID
    //Allow to join existing meeting
    val allowAction = Intent(this, NotificationReceiver::class.java)
    allowAction.putExtra("id", NOTIFICATION_REQUEST_CALL_ID)
    allowAction.putInfoExtra(EXISTING_CALL_REQUEST,data)
    allowAction.action = REMOTE_RESPONSE_ALLOWED

    //Decline to join existing meeting
    val declineAction = Intent(this, NotificationReceiver::class.java)
    declineAction.putExtra("id", NOTIFICATION_REQUEST_CALL_ID)
    declineAction.putInfoExtra(EXISTING_CALL_REQUEST,data)
    declineAction.action = REMOTE_RESPONSE_DECLINED

    val allowActionPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1200,
        allowAction, PendingIntent.FLAG_UPDATE_CURRENT
    )
    val declineActionPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1201,
        declineAction,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    //Customize UI of notification
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val avatar = loadImageToNotificationAvatar(data.fromPhotoURL)
    val remoteView = RemoteViews(packageName, R.layout.holder_join_call_notification)
    if (avatar != null) { remoteView.setImageViewBitmap(R.id.avatar_join_call, avatar) }
    else { remoteView.setImageViewResource(R.id.avatar_join_call, R.drawable.ic_avatar_default) }
    remoteView.setTextViewText(R.id.title_join_call, getString(R.string.incoming_request))
    remoteView.setTextViewText(
        R.id.message_join_call,
        getString(R.string.request_join_call,data.fromName.capitalize(Locale.ROOT))
    )
    remoteView.setOnClickPendingIntent(R.id.join_call_approve, allowActionPendingIntent)
    remoteView.setOnClickPendingIntent(R.id.join_call_decline, declineActionPendingIntent)

    Looper.prepare()
    val builder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.notification_icon)
        .setCustomHeadsUpContentView(remoteView)
        .setContent(remoteView)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(false)
        .setOngoing(true)
        .setTimeoutAfter(NOTIFICATION_REQUEST_CALL_DURATION.toLong())

    with(notificationManager) {
        buildChannel()
        val notification = builder.build()
        notify(Constant.NOTIFICATION_REQUEST_EXIST_CALL, notification)
    }
}

private fun NotificationManager.buildChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_CALL_ID, CHANNEL_NAME_INCOMING_CALL, importance).apply {
            vibrationPattern = NOTIFICATION_VIBRATE_PATTERN
        }
        createNotificationChannel(channel)
    }
}

fun Context.cancelNotification(id: Int) {
    val notificationManager =
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(id)
}

