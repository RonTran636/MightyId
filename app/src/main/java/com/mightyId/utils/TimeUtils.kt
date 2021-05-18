package com.mightyId.utils

import android.content.Context
import android.text.format.DateUtils
import java.time.LocalDateTime
import java.time.ZoneOffset

object TimeUtils {
    fun displayTimeStatus(context: Context, time:String):CharSequence {
        val temp = time.substring(0,19)
        val tempLong = LocalDateTime.parse(temp).toInstant(ZoneOffset.UTC).toEpochMilli()
        return DateUtils.getRelativeDateTimeString(context,tempLong,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS,0)
    }

    fun String.convertToHour(context: Context):String{
        val temp = substring(0,19)
        val tempLong = LocalDateTime.parse(temp).toInstant(ZoneOffset.UTC).toEpochMilli()
        return DateUtils.formatDateTime(context,tempLong,DateUtils.FORMAT_SHOW_TIME)
    }

    fun String.convertToDay(context: Context):String{
        val temp = substring(0,19)
        val tempLong = LocalDateTime.parse(temp).toInstant(ZoneOffset.UTC).toEpochMilli()
        return DateUtils.formatDateTime(context,tempLong,DateUtils.FORMAT_SHOW_YEAR)
    }
}