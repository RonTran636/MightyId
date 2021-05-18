package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("last_message_id")
    val lastMessageId: Int,
    val result: ArrayList<MessageItem>,
)

data class RecentMessage(
    @SerializedName("total_message")
    var totalUnreadTopic: Int = 0,
    val result: MutableList<TopicItem>,
)

class MessageItem(
    @SerializedName("messageType")
    val messageType: String? = null,

    @SerializedName("topic_id")
    var topicId: String? = null,

    //@ColumnInfo(name = "topic_name")
    @SerializedName("topic_name")
    var topicName: String? = null,

    //@ColumnInfo(name = "number_of_participants")
    @SerializedName("number_of_participants")
    var numberOfParticipant: Int = 0,

    //@ColumnInfo(name = "customer_id")
    @SerializedName("customer_id")
    var customerId: String? = null,

    //@ColumnInfo(name = "workid")
    @SerializedName("workid")
    val workId: String? = null,

    //@ColumnInfo(name = "photo_url")
    @SerializedName("photo_url")
    var photoUrl: String? = null,

    //@ColumnInfo(name = "customer_name")
    @SerializedName("customer_name")
    val customerName: String? = null,

    //@ColumnInfo(name = "message_id")
    @SerializedName("message_id")
    val messageId: Int? = null,

    //@ColumnInfo(name = "message")
    @SerializedName("message")
    var messageContent: String? = null,

    //@ColumnInfo(name = "type")
    @SerializedName("type")
    var messageContentType: String? = null,

    //@ColumnInfo(name = "link")
    @SerializedName("link")
    var link: String? = null,

    //@ColumnInfo(name = "time")
    @SerializedName("time")
    var timeSent: String? = null,

    //@ColumnInfo(name = "topic_type")`
    @SerializedName("topic_type")
    val topicType: String? = null,

    //@ColumnInfo(name = "view_type")
    var viewType: Int? = null,

    @SerializedName("parent_id")
    var parentId: Int? = null,

    @SerializedName("has_parent")
    var hasParent: Boolean = false,

    @SerializedName("parent")
    var messageParent: MessageItem? = null,

    @SerializedName("has_emoji")
    var hasEmoji: Boolean = false,

    @SerializedName("emoji")
    var emojis: MutableList<EmojiItem>? = null,

    @SerializedName("todo")
    var todo: TodoListItem? = null,

    @SerializedName("file")
    var fileAttach: ArrayList<FileAttach>? = null,
)

data class FileAttach(
    @SerializedName("file_id")
    val fileId: Int,
    @SerializedName("name")
    val fileName: String,
    @SerializedName("link")
    val filePreviewUrl: String,
    @SerializedName("download")
    val fileDownloadLink: String,
    @SerializedName("type")
    val fileType: String,
)

data class EmojiItem(
    @SerializedName("customer_id")
    var customerId: String? = null,
    @SerializedName("workid")
    val workId: String? = null,
    @SerializedName("photo_url")
    var photoUrl: String? = null,
    @SerializedName("customer_name")
    val customerName: String? = null,
    @SerializedName("emoji")
    var emojis: String? = null,
    @SerializedName("emoji_id")
    var emojiId: Int,
)
