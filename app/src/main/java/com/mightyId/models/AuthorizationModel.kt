package com.mightyId.models

import com.google.gson.annotations.SerializedName
import com.mightyId.models.Account

data class AuthorizationModel(
    @SerializedName("success")
    val success:Boolean,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("result")
    val currentUser: Account
)