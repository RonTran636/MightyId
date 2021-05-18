package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class Topic(
    val message:String,
    val result: MutableList<TopicItem>
)

data class TopicItem(
    @SerializedName("topic_id")
    var topicId: String? = null,
    @SerializedName("name")
    var topicName: String? = null,
    @SerializedName("message")
    var lastMessage: String? = null,
    @SerializedName("time")
    var lastMessageTime: String? = null,
    @SerializedName("image")
    val topicPhoto: String? = null,
    @SerializedName("number_of_participants")
    var numberOfParticipant: Int? = null,
    @SerializedName("customer_id")
    var listCustomerId: ArrayList<String>? = null,
    @SerializedName("topic_type")
    var topicType: String? = null,
    @SerializedName("is_pin")
    var isTopicPinned: Boolean = false,
    @SerializedName("topic_name")
    var serverTopicName: String? = null,
    @SerializedName("call_id")
    val callId: Int? = null,
    @SerializedName("number_of_unread")
    var messageUnread: Int=0,
    var isRead: Boolean = true,
    @SerializedName("admin_id")
    val adminCustomerId: String?=null
)

//data class TopicItem(
//    @SerializedName("topic_id")
//    val topicId:String,
//    @SerializedName("name")
//    val topicName: String,
//    @SerializedName("message")
//    val lastMessage:String?,
//    @SerializedName("time")
//    val lastMessageTime:String?,
//    @SerializedName("image")
//    val topicPhoto:String?,
//    @SerializedName("number_of_participants")
//    var numberOfParticipant: Int=0
//)

