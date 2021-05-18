package com.mightyId.models

data class RequestJoinExistCall(
    val messageType: String,
    val fromCustomerId: String,
    val fromName: String,
    val fromEmail: String?,
    val fromPhotoURL: String?,
    val to: String,
    val callId: String,
    val topicId: String
)
