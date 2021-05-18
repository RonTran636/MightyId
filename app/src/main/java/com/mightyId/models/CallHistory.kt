package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class CallHistory(
    val result : MutableList<CallHistoryItems>
)
data class CallHistoryItems(
    //Share data
    @SerializedName("topic_type")
    val topicType:String,
    @SerializedName("time_call")
    val timeCall: String,
    @SerializedName("name")
    val callerName: String?,
    @SerializedName("meeting_type")
    val meetingType:String?,

    //Private call history data
    @SerializedName("customer_id")
    val customerId : String?,
    @SerializedName("work_id")
    val workId:String?,
    @SerializedName("email")
    val callerEmail: String?,
    @SerializedName("call_status")
    val callStatus:String?,
    @SerializedName("duration")
    var duration: String?,
    @SerializedName("is_request_call")
    val isRequestCall: Boolean?,
    @SerializedName("photo_url")
    val callerPhotoUrl: String?,
    @SerializedName("call_id")
    val callId:String?,

    //Topic call history data
    @SerializedName("topic_id")
    val topicId:String?,
    @SerializedName("total_members")
    val numberOfParticipant: Int?,
    @SerializedName("members")
    val listMember:ArrayList<String>?,
    @SerializedName("topic_photo_url")
    val topicPhoto:String?,
)