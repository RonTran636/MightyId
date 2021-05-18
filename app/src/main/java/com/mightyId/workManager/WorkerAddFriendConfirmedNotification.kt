package com.mightyId.workManager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mightyId.models.RequestAddFriendModel
import com.mightyId.notification.addFriendConfirmedNotification
import timber.log.Timber
import java.lang.reflect.Type

class WorkerAddFriendConfirmedNotification(context: Context, params: WorkerParameters) :
    Worker(context, params) {
    private val _context = context
    override fun doWork(): Result {
        Timber.tag("WorkerAddFriendConfirmedNotification").d("doWork: Called")
        val data = inputData.getString("requestAddFriend")
        val type: Type = object : TypeToken<RequestAddFriendModel>() {}.type
        val requestAddFriend: RequestAddFriendModel = Gson().fromJson(data, type)
        _context.addFriendConfirmedNotification(requestAddFriend)
        return Result.success()
    }
}