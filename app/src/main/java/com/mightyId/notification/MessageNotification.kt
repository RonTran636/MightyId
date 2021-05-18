package com.mightyId.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.mightyId.R
import com.mightyId.activities.main.ui.message.chatRoom.home.ChatRoomActivity
import com.mightyId.broadcast.NotificationReceiver
import com.mightyId.models.MessageItem
import com.mightyId.models.PersonalChatInfo
import com.mightyId.models.PublicChatInfo
import com.mightyId.utils.Constant.Companion.CHANNEL_FRIEND_ID
import com.mightyId.utils.Constant.Companion.CHANNEL_NAME_CHAT
import com.mightyId.utils.Constant.Companion.CHAT_REQUEST
import com.mightyId.utils.Constant.Companion.DIRECT_REPLY
import com.mightyId.utils.Constant.Companion.KEY_TEXT_REPLY
import com.mightyId.utils.Constant.Companion.MESSAGE_REQUEST_CODE
import com.mightyId.utils.Constant.Companion.NOTIFICATION_MESSAGE_ID
import com.mightyId.utils.IntentUtils.putInfoExtra
import com.mightyId.utils.buildChannel
import com.mightyId.utils.loadImageToNotificationAvatar

fun Context.showMessageNotification(data: MessageItem) {
    // Key for the string that's delivered in the action's intent.
    val replyLabel: String = resources.getString(R.string.reply_label)
    val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
        setLabel(replyLabel)
        build()
    }
    val replyIntent = Intent(this, NotificationReceiver::class.java)
    replyIntent.putExtra("id", NOTIFICATION_MESSAGE_ID)
    replyIntent.putInfoExtra(CHAT_REQUEST, data)
    replyIntent.action = DIRECT_REPLY

    val replyPendingIntent: PendingIntent =
        PendingIntent.getBroadcast(applicationContext,
            MESSAGE_REQUEST_CODE,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    val replyAction: NotificationCompat.Action =
        NotificationCompat.Action.Builder(R.drawable.ic_send,
            getString(R.string.reply),
            replyPendingIntent)
            .addRemoteInput(remoteInput)
            .build()

    //Handle click on notification
    val bundle = Bundle()
        if (data.topicType == "private") {
            bundle.putInfoExtra(
                ChatRoomActivity.TOPIC_INFO,
                PersonalChatInfo(data.customerId!!,data.customerName,data.photoUrl,data.topicId)
            )
        } else {
            bundle.putInfoExtra(
                ChatRoomActivity.TOPIC_INFO,
                PublicChatInfo(data.topicId!!,data.photoUrl,data.topicName,data.numberOfParticipant)
            )
        }
    val chatRoomIntent = Intent(this, ChatRoomActivity::class.java)
    chatRoomIntent.putExtra(ChatRoomActivity.TOPIC_TYPE,data.topicType)
    chatRoomIntent.putExtra(ChatRoomActivity.TOPIC_INFO, bundle)

    val messagePendingIntent = PendingIntent.getActivity(
        this,
        MESSAGE_REQUEST_CODE,
        chatRoomIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val avatar = loadImageToNotificationAvatar(data.photoUrl)
    val message = if (!data.topicName.isNullOrEmpty()) {
        data.customerName + ": " + data.messageContent
    }else{
        data.messageContent
    }
    val contentTitle = if (data.topicName.isNullOrEmpty()){
        data.customerName
    }else{
        data.topicName
    }

    val builder = NotificationCompat.Builder(this, CHANNEL_FRIEND_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(contentTitle)
        .setCategory(Notification.CATEGORY_MESSAGE)
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setLargeIcon(avatar)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setAutoCancel(true)
        .setColor(resources.getColor(R.color.com_facebook_messenger_blue, theme))
        .addAction(replyAction)
        .setContentIntent(messagePendingIntent)

    with(notificationManager) {
        buildChannel(CHANNEL_FRIEND_ID, CHANNEL_NAME_CHAT, NotificationManager.IMPORTANCE_HIGH)
        val notification = builder.build()
        notify(NOTIFICATION_MESSAGE_ID, notification)
    }
}