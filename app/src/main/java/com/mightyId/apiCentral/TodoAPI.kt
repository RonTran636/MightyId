package com.mightyId.apiCentral

import com.google.gson.JsonObject
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface TodoAPI {

    //Create to-do list
    @POST("api/workid/todo/create-todo")
    fun createTodo(
        @Query("title") title:String,
        @Query("topic_id") topicId:String,
        @Query("content") content: String,
        @Query("customer_id") assignee: ArrayList<String>,
        @Query("deadline") deadline: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Single<JsonObject>

    //Update to-do status
    @PUT("/api/workid/todo/update-status")
    fun updateStatus(
        @Query("todo_id") todoId:Int,
        @Query("status") todoStatus: String,
        @Header("Authorization") token: String = "Bearer " + Common.currentAccount?.serverToken,
        @Query("api_key") apiKey: String = Key.KEY
    ): Completable
}