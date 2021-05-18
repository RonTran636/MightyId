package com.mightyId.notification

/**
fun Context.showTodoNotification(data: TodoListItem) {
    //Handle click on notification
    val bundle = Bundle()
    bundle.putInfoExtra(
        ChatRoomActivity.TOPIC_INFO,
        PublicChatInfo(data.topicId,data.photoUrl,data.topicName))
    val chatRoomIntent = Intent(this, ChatRoomActivity::class.java)
    chatRoomIntent.putExtra(ChatRoomActivity.TOPIC_TYPE,PRIVACY_PUBLIC)
    chatRoomIntent.putInfoExtra(ChatRoomActivity.TOPIC_INFO, bundle)

    val messagePendingIntent = PendingIntent.getActivity(
        this,
        Constant.MESSAGE_REQUEST_CODE,
        chatRoomIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val avatar = loadImageToNotificationAvatar(data.photoUrl)
//    val message = if (data.topicName.isEmpty()) {
//        data.customerName + ": " + data.messageContent
//    }else{
//        data.messageContent
//    }
//    val contentTitle = if (data.topicName.isNullOrEmpty()){
//        data.customerName
//    }else{
//        data.topicName
//    }

    Looper.prepare()
    val builder = NotificationCompat.Builder(this, Constant.CHANNEL_FRIEND_ID)
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
        buildChannel(Constant.CHANNEL_FRIEND_ID, Constant.CHANNEL_NAME_CHAT, NotificationManager.IMPORTANCE_HIGH)
        val notification = builder.build()
        notify(Constant.NOTIFICATION_MESSAGE_ID, notification)
    }
}*/