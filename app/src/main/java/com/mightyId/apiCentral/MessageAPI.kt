package com.mightyId.apiCentral

import com.google.gson.JsonObject
import com.mightyId.models.*
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface MessageAPI {

//    //Get all friends/topics used to send message
//    @GET("/api/workid/chat/get-message-history")
//    fun getListMessage(
//        @Query("page") page :Int = 1,
//        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
//        @Query("api_key") apiKey: String = Key.KEY
//    ): Single<Untitled>

    //Get Topic's info
    @GET("/api/workid/chat/topic/topic-info")
    fun getTopicInfo(
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Single<JsonObject>

    //Get recent message
    @GET("api/workid/chat/get-all-recent-message")
    fun getRecentMessage(
        @Query("page") page: Int,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY,
    ): Single<RecentMessage>

    //Get all topic shared with specified user
    @GET("/api/workid/chat/topic/get-topic-with-friend")
    fun getAllTopicWith(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<Topic>

    //Get topic id's private message of specified user
    @GET("/api/workid/chat/chat-friend")
    fun getTopicId(
        @Query("customer_id") customerId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<JsonObject>

    //Get list chat message of a specified topic ID
    @GET("/api/workid/chat/get-message")
    fun getMessage(
        @Query("topic_id") topicId: String,
        @Query("last_message_id") lastMessageId : Int?,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<Message>

    //Send message with given topicId
    @POST("/api/workid/chat/sent-message")
    fun sendMessage(
        @Query("topic_id") topicId: String,
        @Query("message") messageContent:String,
        @Query("parent_id")parentId:Int?,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Sent file to topic
    @Multipart
    @POST("/api/workid/chat/sent-file")
    fun sendFile(
        @Part ("topic_id") topicId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @HeaderMap token: Map<String,String>
    ):Completable

    //Create a topic and add members
    @POST("/api/workid/chat/topic/add-member")
    fun createTopic(
        @Body body : TopicItem,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<JsonObject>

    //Add member to topic
    @POST("/api/workid/chat/topic/add-member")
    fun addMember(
        @Body body : TopicItem,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Get all user in a topic
    @GET("/api/workid/chat/topic/get-member-topic")
    fun getMember(
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<Contact>

    //Remove user from a topic
    @POST("/api/workid/chat/topic/remove-member")
    fun removeMember(
        @Body body : TopicItem,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Delete topic
    @POST("/api/workid/chat/topic/delete-topic")
    fun deleteTopic(
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Edit topic's name
    @PUT("/api/workid/chat/topic/edit-name-topic")
    fun editTopicName(
        @Query("topic_id") topicId: String,
        @Query("name") newTopicName:String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable

    //Change Topic's photo
    @Multipart
    @POST("/api/workid/chat/topic/edit-image-topic")
    fun changeTopicName(
        @Part("topic_id") topicId: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @HeaderMap token: Map<String,String>
    ): Completable

    //Get uploaded media (photos/videos)
    @GET("/api/workid/chat/get-media")
    fun getPhotoAndVideo(
        @Query("topic_id")topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<Message>

    //Delete message
    @DELETE("/api/workid/chat/delete-message")
    fun deleteMessage(
        @Query("message_id") messageId:Int,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    //Edit existing message
    @PUT("/api/workid/chat/edit-message")
    fun editMessage(
        @Query("message_id")messageId: Int,
        @Query("message") messageContent:String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    //Archive topic
    @POST("/api/workid/chat/topic/hidden-topic")
    fun archiveTopic(
        @Query("topic_id") topicArchive: ArrayList<String>,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Completable

    //Search a specified topic
    @GET("api/workid/chat/topic/search-topic")
    fun searchTopic(
        @Query("topic_name") topicName: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ):Observable<Topic>

    //Get file list
    @GET("/api/workid/chat/get-file")
    fun getTopicFile(
        @Query("topic_id") topicId: String,
        @Query("last_message_id") lastMessageId: Int?=null,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ) : Single<Message>

    //Get file links
    @GET("api/workid/chat/get-link")
    fun getTopicLink(
        @Query("topic_id") topicId: String,
        @Query("last_message_id") lastMessageId: Int?=null,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ) : Single<Message>

    //Get file links
    @GET("api/workid/todo/get-todo-topic")
    fun getTopicTodoList(
        @Query("topic_id") topicId: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ) : Single<TodoList>



    //Download file
    @GET
    @Streaming
    fun getFileFromServer(@Url url: String) : Single<ResponseBody>
}