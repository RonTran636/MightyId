package com.mightyId.utils

import android.content.Intent
import android.os.Bundle
import androidx.work.Data
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.lang.reflect.Type


object IntentUtils {

    inline fun <reified T> retrieveDataFromFcm(message: RemoteMessage): T {
        val params = message.data as Map<String, String>
        val jsonObject = JSONObject(params).toString()
        return Gson().fromJson(jsonObject, T::class.java)
    }

    fun <T> Intent.putInfoExtra(name: String, value: T) {
        val gson = Gson()
        this.putExtra(name, gson.toJson(value))
    }

    inline fun <reified T> Intent.getInfoExtra(name: String): T {
        val gson = Gson()
        return gson.fromJson(this.getStringExtra(name), T::class.java)
    }

    fun <T> Bundle.putInfoExtra(name: String, value: T) {
        val gson = Gson()
        this.putString(name, gson.toJson(value))
    }

    inline fun <reified T> Bundle.getInfoExtra(name: String): T {
        val type: Type = object : TypeToken<T>() {}.type
        val gson = Gson()
        return gson.fromJson(this.getString(name), type)
    }

    inline fun <reified T> Data.getInfoExtra(name:String): T{
        val type: Type = object : TypeToken<T>() {}.type
        val gson = Gson()
        return gson.fromJson(this.getString(name), type)
    }
}