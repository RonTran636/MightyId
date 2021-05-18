package com.mightyId.models.server

import com.google.gson.annotations.SerializedName

data class ServerCallModel(
    @SerializedName("customer_id")
    var customerId: ArrayList<String>? = null,
    @SerializedName("meeting_type")
    var meetingType: String? = null,
    @SerializedName("topic_id")
    val topicId: String? = null,
    @SerializedName("privacy_mode")
    var privacy: String? = null,
    @SerializedName("call_id")
    var callId: Int? = null,
)
