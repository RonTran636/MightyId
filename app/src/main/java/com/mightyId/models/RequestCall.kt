package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class RequestCall(
    //Request call model
    @SerializedName("messageType")
    var messageType: String,
    @SerializedName("callId")
    var callId: String? = null,
    var callerCustomerId: String?=null,
    var callerName: String? = null,
    var callerEmail: String? = null,
    @SerializedName("privacy_mode")
    var privacyMode :String?=null,
    @SerializedName("isPrivateCall")
    var isPrivateCall:Boolean?=null,
    @SerializedName("photo_url")
    var callerPhotoURL: String? = null,
    @SerializedName("server_meet")
    var serverMeet:String?=null,
    var meetingType: String? = null,
    var meetingId: String? = null,
    var topicId : String?= null,
    //Request response model
    var response: String? = null
)