package com.mightyId.apiCentral

import com.mightyId.models.Contact
import com.mightyId.models.FriendStatus
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface FriendsAPI {

    //Adding friend to friend list
    @POST("/api/workid/add-friend")
    fun sendFriendRequestToServer(
        @Query("customer_id") receiverId: String,
        @Query("message") message:String,
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!
    ): Completable

    //Load suggest friend list at Home Activity
    @GET("/api/workid/random-friend?")
    fun getRecommendContact(
        @Query("customer_id") userId: String,
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!
    ): Single<Contact>

    //Accept friend request
    @POST("/api/workid/accept-friend")
    fun responseAcceptFriend(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    //Decline friend request
    @POST("/api/workid/reject-friend")
    fun responseDeclineFriend(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    //Cancel friend request
    @POST("api/workid/destroy-request")
    fun cancelFriendRequest(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    //Load friend list of current user
    @GET("api/workid/list-friends")
    fun getCurrentUserFriendList(
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!,
        @Query("status") status: Int = 2
    ): Single<Contact>

    //Check friend status
    @GET("api/workid/get-status")
    fun getFriendStatusOf(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<FriendStatus>

    @DELETE("api/workid/delete-friends")
    fun deleteFriend(
        @Query("customer_id") deleteId: ArrayList<String>,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable
}