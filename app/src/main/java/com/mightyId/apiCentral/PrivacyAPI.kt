package com.mightyId.apiCentral

import com.mightyId.utils.Common
import com.mightyId.utils.Key
import io.reactivex.rxjava3.core.Completable
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface PrivacyAPI {
    @POST("/api/workid/privacy/update-only-friend-call")
    fun blockStrangerCall(
        @Query("status") status: Int,
        @Query("type") type:String = "only_friend_call",
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    @POST("/api/workid/privacy/update-only-friend-invite-topic")
    fun blockStrangerInviteTopic(
        @Query("status") status: Int,
        @Query("type") type:String = "only_friend_invite_topic",
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    @POST("/api/workid/privacy/update-only-friend-chat")
    fun blockStrangerSendMessage(
        @Query("status") status: Int,
        @Query("type") type:String = "only_friend_chat",
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable
}