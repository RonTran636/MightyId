package com.mightyId.models.server

import com.google.gson.annotations.SerializedName

data class ServerNotify (
    @SerializedName("total_message")
    var totalUnreadMessage: Int = 0,
    @SerializedName("total_request_add_friend")
    var totalRequestAddFriend: Int = 0,
    @SerializedName("total_missed_call")
    var totalMissedCall: Int = 0,
    @SerializedName("total_notify")
    var totalNotify: Int = 0
)