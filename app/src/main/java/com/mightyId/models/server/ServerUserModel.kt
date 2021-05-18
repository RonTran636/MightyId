package com.mightyId.models.server

import com.mightyId.models.Account

data class ServerUserModel(
    val message: String,
    val msg: String,
    val result: Account
)

