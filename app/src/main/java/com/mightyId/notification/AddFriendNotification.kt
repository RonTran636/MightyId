package com.mightyId.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.mightyId.R
import com.mightyId.activities.main.MainActivity
import com.mightyId.activities.userDetails.UserDetailActivity
import com.mightyId.broadcast.NotificationReceiver
import com.mightyId.models.Account
import com.mightyId.models.RequestAddFriendModel
import com.mightyId.utils.Constant
import com.mightyId.utils.Constant.Companion.ADD_FRIEND
import com.mightyId.utils.Constant.Companion.CHANNEL_FRIEND_ID
import com.mightyId.utils.Constant.Companion.CHANNEL_NAME_CHAT
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS
import com.mightyId.utils.Constant.Companion.FRIEND_STATUS_ACCEPTED
import com.mightyId.utils.Constant.Companion.INVITE_USER_INFO
import com.mightyId.utils.Constant.Companion.NOTIFICATION_REQUEST_ADD_FRIEND_ID
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.buildChannel
import com.mightyId.utils.loadImageToNotificationAvatar
import org.jetbrains.anko.notificationManager
import timber.log.Timber
import java.util.*

fun Context.showAddFriendNotification(data: RequestAddFriendModel){
    //Handle action click on notification - Accept
    val acceptAddFriendAction = Intent(this, NotificationReceiver::class.java)
    acceptAddFriendAction.putInfoExtra(ADD_FRIEND,data)
    acceptAddFriendAction.action = Constant.REMOTE_MSG_ADD_FRIEND_ACCEPTED

    //Handle action click on notification - Decline
    val declineAddFriendAction = Intent(this, NotificationReceiver::class.java)
    declineAddFriendAction.putInfoExtra(ADD_FRIEND,data)
    declineAddFriendAction.action = Constant.REMOTE_MSG_ADD_FRIEND_REJECTED

    //Handle click on notification
    val bundle = Bundle()
    bundle.putInfoExtra(ADD_FRIEND,data)
    val detailAddFriendPendingIntent = NavDeepLinkBuilder(this)
        .setComponentName(MainActivity::class.java)
        .setGraph(R.navigation.mobile_navigation)
        .setDestination(R.id.navigation_contact)
        .setArguments(bundle)
        .createPendingIntent()

    val acceptAddFriendPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1200,
        acceptAddFriendAction,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val declineAddFriendPendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        1201,
        declineAddFriendAction,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val avatar = loadImageToNotificationAvatar(data.senderPhoto)
    val message = data.senderName+": "+data.messageDetail

    Looper.prepare()
    val builder = NotificationCompat.Builder(this, CHANNEL_FRIEND_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getString(R.string.message_friend_request))
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setLargeIcon(avatar)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setColor(resources.getColor(R.color.com_facebook_messenger_blue,theme))
        .addAction(R.drawable.ic_baseline_clear_24,"Delete",declineAddFriendPendingIntent)
        .addAction(R.drawable.ic_baseline_check_24,"Accept",acceptAddFriendPendingIntent)
        .setContentIntent(detailAddFriendPendingIntent)
//        .setSound(Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.funny_indian))

    with(notificationManager) {
        buildChannel(CHANNEL_FRIEND_ID, CHANNEL_NAME_CHAT,NotificationManager.IMPORTANCE_HIGH)
        val notification = builder.build()
        notify(NOTIFICATION_REQUEST_ADD_FRIEND_ID, notification)
    }
}

fun Context.addFriendConfirmedNotification(data: Account) {
    val avatar = loadImageToNotificationAvatar(data.photoUrl)
    val friendDetail = Intent(this, UserDetailActivity::class.java)
    friendDetail.putInfoExtra(INVITE_USER_INFO,data)
    val friendDetailPendingIntent =
        PendingIntent.getActivity(applicationContext, 1200, friendDetail, PendingIntent.FLAG_UPDATE_CURRENT)

    val builder = NotificationCompat.Builder(this, CHANNEL_FRIEND_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getString(R.string.work_id))
        .setContentText(getString(R.string.message_friend_accepted_notification, data.customerName))
        .setLargeIcon(avatar)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setColor(resources.getColor(R.color.com_facebook_messenger_blue, theme))
        .setContentIntent(friendDetailPendingIntent)

    with(notificationManager) {
        buildChannel(CHANNEL_FRIEND_ID,
            FRIEND_STATUS,
            NotificationManager.IMPORTANCE_HIGH)
        val notification = builder.build()
        notify(NOTIFICATION_REQUEST_ADD_FRIEND_ID, notification)
    }
}

fun Context.addFriendConfirmedNotification(data: RequestAddFriendModel){
    Timber.tag("WorkerAddFriendConfirmedNotification").d("addFriendConfirmedNotification: notificationManager")
    val avatar = loadImageToNotificationAvatar(data.senderPhoto)
    val account = Account(
        customerName = data.senderName,
        customerId = data.senderId,
        photoUrl = data.senderPhoto,
        friendStatus = FRIEND_STATUS_ACCEPTED
    )
    val friendDetail = Intent(this, UserDetailActivity::class.java).apply {
        putInfoExtra(INVITE_USER_INFO,account)
    }
    val friendDetailPendingIntent =
        PendingIntent.getActivity(applicationContext, 1200, friendDetail, PendingIntent.FLAG_UPDATE_CURRENT)
    val builder = NotificationCompat.Builder(this, CHANNEL_FRIEND_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(getString(R.string.work_id))
        .setContentText(getString(R.string.message_friend_accepted_confirmation,
            data.senderName!!.capitalize(Locale.getDefault())))
        .setLargeIcon(avatar)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setColor(resources.getColor(R.color.com_facebook_messenger_blue, theme))
        .setContentIntent(friendDetailPendingIntent)
    with(notificationManager) {
        buildChannel(CHANNEL_FRIEND_ID,
            FRIEND_STATUS,
            NotificationManager.IMPORTANCE_HIGH)
        val notification = builder.build()
        notify(NOTIFICATION_REQUEST_ADD_FRIEND_ID, notification)
    }
}

