package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class AcceptFriend(
    @SerializedName("message_type")
    val messageType:String,
    @SerializedName("message_add_friend")
    val message:String,
    @SerializedName("customer_name")
    val senderName:String,
    @SerializedName("photo_url")
    val senderPhotoUrl :String,
    @SerializedName("customer_id")
    val senderId:String
)
