package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class ResponseModel(
    @SerializedName("call_id")
    val callId: String,
    @SerializedName("meeting_id")
    val meetingId: String,
    @SerializedName("is_private_call")
    val inPrivateCall: Boolean?,
    @SerializedName("privacy_mode")
    val privacyMode : String,
    @SerializedName("topic_id")
    val topicId: String,
    @SerializedName("server_meet")
    val serverMeet: String,
    @SerializedName("message")
    val message:String
)
