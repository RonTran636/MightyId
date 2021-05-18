package com.mightyId.apiCentral

import com.mightyId.utils.Common
import com.mightyId.utils.Key
import com.mightyId.models.Account
import com.mightyId.models.AuthorizationModel
import com.mightyId.models.Contact
import com.mightyId.models.server.ServerNotify
import com.mightyId.models.server.ServerUserModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.http.*

interface Authentication {
    //Check if user whether user already log in
    @GET("/api/workid/user-info?")
    fun getUserInfo(
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!
    ): Single<AuthorizationModel>

    //Register User
    @POST("/api/workid/auth/register")
    fun registerUserToDatabase(@Body body: Account): Single<ServerUserModel>

    //Login with email and password
    @POST("/api/workid/auth/login")
    fun loginWithEmailAndPassword(
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<AuthorizationModel>

    //Registration stage - Checking user whether existed on our database
    @GET("/api/workid/find-by-uid?")
    fun checkUserExistedOnDatabase(
        @Query("uid") uid: String,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<ServerUserModel>

    //Logout
    @POST("/api/workid/logout")
    fun logout(
        @Query("fcm_token") fcmToken: String,
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!
    ): Completable

    //Update token
    @GET("/api/workid/refresh-token")
    fun updateServerToken(
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!
    ): Single<JSONObject>

    //Search a specified user by workId, email or by name
    @GET("/api/workid/search")
    fun searchUserByEmailOrUid(
        @Query("keyword") keyword: String,
        @Query("api_key") apiKey: String = Key.KEY,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount!!.serverToken!!,
    ): Observable<Contact>

    //Update Fcm token to server's database
    @PUT("/api/workid/update-fcm-token")
    fun updateFcmToken(
        @Query("fcm_token") fcmToken: String?,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Completable

    //Send email password recovery
    @POST("/api/workid/send-email-forgot-password")
    fun sendPasswordRecoveryEmail(
        @Query("email") email: String,
    ): Completable

    //Sent file to topic
    @Multipart
    @POST("/api/workid/update-avatar")
    fun changeAvatar(
        @Part image: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @HeaderMap token: Map<String, String>
    ): Completable

    //Get all notify info
    @GET("/api/workid/get-all-number-notify")
    fun getNotifyInfo(
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Single<ServerNotify>
}