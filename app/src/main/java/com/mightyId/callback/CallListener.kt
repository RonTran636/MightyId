package com.mightyId.callback

import com.mightyId.models.Account
import com.mightyId.models.TopicItem

interface CallListener {
    fun initiateMeeting(account: Account, type: String)
    fun initiateMeeting(topicItem: TopicItem, type:String)
}