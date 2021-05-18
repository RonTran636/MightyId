package com.mightyId.callback

import android.os.Bundle

interface MessageListener {
    fun moveToChatRoom(chatRoomType: String,chatRoomKey :Bundle)
}