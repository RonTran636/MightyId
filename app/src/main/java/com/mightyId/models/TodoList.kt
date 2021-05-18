package com.mightyId.models

import com.google.gson.annotations.SerializedName

data class TodoList(
    @SerializedName("success")
    var message: Boolean,
    @SerializedName("result")
    var result: MutableList<TodoListItem>
)

data class TodoListItem(
    @SerializedName("topic_id")
    var topicId:String,
    @SerializedName("topic_name")
    var topicName:String,
    @SerializedName("photo_url")
    val topicPhotoUrl:String,
    @SerializedName("todo_id")
    var todoId: Int,
    @SerializedName("title")
    var todoTitle: String,
    @SerializedName("content")
    var todoContent: String,
    @SerializedName("status")
    var todoStatus:String,
    @SerializedName("start")
    val todoTimeStart: String,
    @SerializedName("deadline")
    val todoDeadline:String,
    @SerializedName("image")
    var photoUrl: String,
    @SerializedName("admin_id")
    val assigner: String,
    @SerializedName("customer_id")
    val listAssigneeId: ArrayList<String>,
    @SerializedName("customer_name")
    val listAssigneeName: String
)