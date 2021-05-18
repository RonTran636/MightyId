package com.mightyId.apiCentral

import com.mightyId.models.Message
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface PinAPI {
    @GET("/api/workid/pin/get-pin")
    fun getPin()

    @POST("/api/workid/pin/add-pin")
    fun addPin(
        @Query("type") pinType: String,
        @Query("id") pinId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Completable

    @DELETE("/api/workid/pin/remove-pin")
    fun deletePin(
        @Query("type") pinType: String,
        @Query("id") pinId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Completable

    @GET("/api/workid/chat/topic/get-pin-message")
    fun getPinMessage(
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Single<Message>

    //Pin message
    @POST("/api/workid/chat/topic/add-pin-message")
    fun pinMessage(
        @Query("topic_id") topicId: String,
        @Query("message_id") messageId: Int,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ):Completable

    //Delete message
    @DELETE("/api/workid/chat/topic/delete-pin-message")
    fun unpinMessage(
        @Query("topic_id") topicId: String,
        @Query("message_id") messageId: Int,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable
}