package com.mightyId.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class ChatRoomModel(
    val name: String,
    var topicId:String?,
    val photoUrl:String?,
    val type:String
)


data class PublicChatInfo(
    @SerializedName("topic_id")
    var topicId:String?=null,
    var topicPhotoUrl:String?=null,
    var topicName:String?=null,
    var numberOfParticipants : Int?=null,
    val listCustomerId:ArrayList<String>?=null,
    var isTopicPinned : Boolean=false,
)

data class PersonalChatInfo(
    @SerializedName("customer_id")
    var customerId:String?=null,

    @SerializedName("name")
    var customerName:String?=null,

    @SerializedName("photo_url")
    var customerPhotoUrl :String?=null,

    @SerializedName("topic_id")
    var topicId:String?=null,

    val friendStatus:Int?=null,

    @SerializedName("is_pin")
    var isMessagePinned: Boolean=false,

    @SerializedName("last_login")
    var lastSeen: String? = null,

    @SerializedName("is_online")
    var isOnline : Boolean?=null,

    @SerializedName("workid")
    var customerWorkId: String?=null,

    @SerializedName("list_customer_id")
    var listCustomerId: ArrayList<String>?=null
)