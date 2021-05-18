package com.mightyId.apiCentral

import com.mightyId.models.CallHistory
import com.mightyId.models.ResponseModel
import com.mightyId.models.server.ServerCallModel
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface CallAPI {

    //Send a call request to an user or a group
    @POST("/api/workid/make-call")
    fun sendRequestCall(
        @Body bodies: ServerCallModel,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<ResponseModel>

    //Response to a request call
    @POST("/api/workid/action-call")
    fun sendResponseRequestCall(
        @Query("call_id") callId: String,
        @Query("action") responseAction: String,
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Update state when user join the meeting
    @PUT("/api/workid/update-status-when-joined")
    fun updateJoinedState(
        @Query("call_id") callId: String,
        @Query("privacy_mode") privacyMode: String,
        @Query("topic_id") topicId:String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Update state when user leave the meeting
    @PUT("/api/workid/update-status-when-out")
    fun updateLeftState(
        @Query("call_id") callId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Request to join an existing meeting
    @POST("/api/workid/request-join-call")
    fun requestJoinExistingMeeting(
        @Query("customer_id") fromCustomerId: String,
        @Query("call_id") callId: String,
        @Query("topic_id") topicId: String?,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ) :Completable

    @POST("/api/workid/response-join-call")
    fun responseJoinExistingMeeting(
        @Query("action") action:String,
        @Query("to") toCustomerId: String,
        @Query("call_id") callId: String,
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Cancel join existing meeting
    @POST("/api/workid/cancel-request-join")
    fun cancelJoinExistingMeeting(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Get all call history of current user
    @GET("/api/workid/call-history")
    fun getCallHistory(
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<CallHistory>

    //Get call history of current user with a specific other
    @GET("/api/workid/call-history-with")
    fun getHistoryCallOf(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<CallHistory>

}