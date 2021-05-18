package com.mightyId.models.server

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

data class ServerSendFile(
    @SerializedName("topic_id")
    val topicId: String,
    @SerializedName("file")
    val file: MultipartBody.Part
)
