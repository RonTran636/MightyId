package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class RequestAddFriendModel(
    var messageType: String,
    @SerializedName("workid")
    var senderWorkId: String?=null,
    @SerializedName("customer_id")
    var senderId: String?=null,
    @SerializedName("customer_name")
    var senderName : String? = null,
    @SerializedName("customer_email")
    var senderEmail: String?=null,
    @SerializedName("photo_url")
    var senderPhoto : String?=null,
    @SerializedName("message_add_friend")
    var messageDetail: String?=null,
    @SerializedName("friend_status")
    var friendStatus: Int?=null
)


