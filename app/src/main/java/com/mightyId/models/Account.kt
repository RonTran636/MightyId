package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class Account(
    @SerializedName("customer_id")
    var customerId: String? = null,

    @SerializedName("customer_name")
    var customerName: String? = null,

    @SerializedName("customer_email")
    var customerEmail: String? = null,

    @SerializedName("customer_phone")
    var customerPhoneNumber: String? = null,

    @SerializedName("workid")
    var workId: String? = null,

    @SerializedName("photo_url")
    var photoUrl: String? = null,

    @SerializedName("password")
    var password : String?=null,

    @SerializedName("fcm_token")
    var fcmToken: String? = null,

    @SerializedName("last_login")
    var lastSeen: String? = null,

    @SerializedName("is_online")
    var isOnline : Boolean = false,

    @SerializedName("is_active")
    var isActive: Boolean? = null,

    @SerializedName("token")
    var serverToken: String? = null,

    @SerializedName("is_pin")
    var isMessagePinned: Boolean=false,

    @SerializedName("only_friend_call")
    var strangerCall: Boolean=false,

    @SerializedName("only_friend_invite_topic")
    var strangeInviteTopic: Boolean=false,

    @SerializedName("only_friend_chat")
    var strangerMessage: Boolean=false,

    @SerializedName("friend_status")
    var friendStatus: Int=0
)

data class Contact(
    @SerializedName("success")
    val success: String,
    @SerializedName("Msg")
    val msg :String,
    val result: ArrayList<Account>
)
