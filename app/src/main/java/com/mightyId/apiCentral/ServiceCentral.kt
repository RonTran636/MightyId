package com.mightyId.apiCentral

import com.google.gson.JsonObject
import com.mightyId.models.*
import com.mightyId.models.server.ServerCallModel
import com.mightyId.models.server.ServerNotify
import com.mightyId.models.server.ServerUserModel
import com.mightyId.utils.Common.BASE_URL
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS

class ServiceCentral {

    private val clientOkHttpClient = OkHttpClient().newBuilder()
        .readTimeout(60, SECONDS)
        .writeTimeout(60, SECONDS)
        .connectTimeout(60, SECONDS)
    private inline fun <reified T> invoke(): T {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(clientOkHttpClient.build())
            .build()
            .create(T::class.java)
    }

    //POST Data to server:
    fun registerUserToDatabase(body: Account): Single<ServerUserModel> =
        invoke<Authentication>().registerUserToDatabase(body)

    fun sendFriendRequestToServer(remoteMessage: RequestAddFriendModel): Completable =
        invoke<FriendsAPI>().sendFriendRequestToServer(
            remoteMessage.senderId!!,
            remoteMessage.messageDetail!!
        )

    fun sendRequestCall(serverCallModel: ServerCallModel): Single<ResponseModel> =
        invoke<CallAPI>().sendRequestCall(serverCallModel)

    fun sendResponseRequestCall(
        callId: String,
        responseAction: String,
        topicId: String,
    ): Completable = invoke<CallAPI>().sendResponseRequestCall(callId, responseAction, topicId)

    fun requestJoinExistingMeeting(
        fromCustomerId: String,
        callId: String,
        topicId: String,
    ): Completable =
        invoke<CallAPI>().requestJoinExistingMeeting(fromCustomerId, callId, topicId)

    fun sendPasswordRecoveryEmail(email: String) =
        invoke<Authentication>().sendPasswordRecoveryEmail(email)

    fun getTopicInfo(topicId: String): Single<JsonObject> =
        invoke<MessageAPI>().getTopicInfo(topicId)

    fun getNotifyInfo(): Single<ServerNotify> = invoke<Authentication>().getNotifyInfo()

    fun responseJoinExistingMeeting(
        action: String,
        toCustomerId: String,
        callId: String,
        topicId: String,
    ): Completable =
        invoke<CallAPI>().responseJoinExistingMeeting(action, toCustomerId, callId, topicId)

    fun cancelJoinExistingMeeting(customerID: String) =
        invoke<CallAPI>().cancelJoinExistingMeeting(customerID)

    fun sendMessage(topicId: String, messageContent: String,parentId: Int?) =
        invoke<MessageAPI>().sendMessage(topicId, messageContent,parentId)

    fun sendFile(
        topicId: RequestBody,
        file: MultipartBody.Part,
        apiKey: RequestBody,
        token: Map<String, String>,
    ): Completable = invoke<MessageAPI>().sendFile(topicId,file,apiKey, token)

    fun changeAvatar(
        avatar : MultipartBody.Part,
        apiKey: RequestBody,
        token: Map<String, String>,
    ):Completable = invoke<Authentication>().changeAvatar(avatar,apiKey, token)

    fun createTodo(
        title:String,
        topicId: String,
        content:String,
        assignee:ArrayList<String>,
        deadline:String
    ): Single<JsonObject> = invoke<TodoAPI>().createTodo(title, topicId, content, assignee, deadline)

    fun createTopic(
        topicName: String,
        listMember: ArrayList<String>,
    ): Single<JsonObject> {
        val serverTopic = TopicItem()
        serverTopic.listCustomerId = listMember
        serverTopic.serverTopicName = topicName
        return invoke<MessageAPI>().createTopic(serverTopic)
    }

    fun addMember(topicId: String, listMember: ArrayList<String>): Completable {
        val topicItem = TopicItem()
        topicItem.topicId = topicId
        topicItem.listCustomerId = listMember
        return invoke<MessageAPI>().addMember(topicItem)
    }

    fun removeMember(topicId: String, listMember: ArrayList<String>): Completable  {
        val topicItem = TopicItem()
        topicItem.topicId = topicId
        topicItem.listCustomerId = listMember
        return invoke<MessageAPI>().removeMember(topicItem)
    }
    fun deleteTopic(topicId: String): Completable = invoke<MessageAPI>().deleteTopic(topicId)

    fun changeTopicImage(
        topicId: RequestBody,
        image : MultipartBody.Part,
        apiKey: RequestBody,
        token: Map<String, String>,
    ):Completable = invoke<MessageAPI>().changeTopicName(topicId,image,apiKey, token)

    fun updateStatus(todoId:Int, status: String): Completable = invoke<TodoAPI>().updateStatus(todoId,status)

    fun loginWithEmailAndPassword(email:String,password:String) : Single<AuthorizationModel> =
        invoke<Authentication>().loginWithEmailAndPassword(email, password)

    fun logout(fcmToken:String) = invoke<Authentication>().logout(fcmToken)

    fun archiveTopic(listTopicId: ArrayList<String>) = invoke<MessageAPI>().archiveTopic(listTopicId)

    fun blockStrangerCall(status: Int) = invoke<PrivacyAPI>().blockStrangerCall(status)

    fun blockStrangerInviteTopic(status: Int) = invoke<PrivacyAPI>().blockStrangerInviteTopic(status)

    fun blockStrangerSendMessage(status: Int)= invoke<PrivacyAPI>().blockStrangerSendMessage(status)

    fun pinMessage(topicId: String,messageId: Int) = invoke<PinAPI>().pinMessage(topicId, messageId)

    fun getPinMessage(topicId: String) = invoke<PinAPI>().getPinMessage(topicId)

    fun unpinMessage(topicId: String,messageId: Int) = invoke<PinAPI>().unpinMessage(topicId, messageId)

    //GET Data from Server:
    fun checkUserExistedOnDatabase(uid: String): Single<ServerUserModel> =
        invoke<Authentication>().checkUserExistedOnDatabase(uid)

    fun searchUserByEmailOrUid(keyword: String): Observable<Contact> =
        invoke<Authentication>().searchUserByEmailOrUid(keyword)

    fun getRecommendContact(customerID: String): Single<Contact> =
        invoke<FriendsAPI>().getRecommendContact(customerID)

    fun getCurrentUserFriendList(): Single<Contact> =
        invoke<FriendsAPI>().getCurrentUserFriendList()

    fun getCallHistory(): Single<CallHistory> = invoke<CallAPI>().getCallHistory()

    fun getCallHistoryOf(customerID: String): Single<CallHistory> =
        invoke<CallAPI>().getHistoryCallOf(customerID)

    fun getFriendStatusOf(customerID: String): Single<FriendStatus> =
        invoke<FriendsAPI>().getFriendStatusOf(customerID)

    fun getAllTopicWith(customerID: String): Single<Topic> =
        invoke<MessageAPI>().getAllTopicWith(customerID)

    fun getPhotoAndVideo(topicId: String):Single<Message> =
        invoke<MessageAPI>().getPhotoAndVideo(topicId)

    fun getRecentMessage(pages: Int): Single<RecentMessage> =
        invoke<MessageAPI>().getRecentMessage(pages)

    fun getTopicId(customerID: String): Single<JsonObject> =
        invoke<MessageAPI>().getTopicId(customerID)

    fun getMessage(topicId: String,lastMessageId:Int?): Single<Message> =
        invoke<MessageAPI>().getMessage(topicId, lastMessageId)

    fun getMember(topicId: String): Single<Contact> = invoke<MessageAPI>().getMember(topicId)

    fun updateServerToken(): Single<JSONObject> = invoke<Authentication>().updateServerToken()

    fun getTopicFile(topicId: String): Single<Message> = invoke<MessageAPI>().getTopicFile(topicId)

    fun getTopicLink(topicId: String): Single<Message> = invoke<MessageAPI>().getTopicLink(topicId)

    fun getTopicTodoList(topicId: String): Single<TodoList> = invoke<MessageAPI>().getTopicTodoList(topicId)

    fun getUserInfo(): Single<AuthorizationModel> = invoke<Authentication>().getUserInfo()

    fun addPin(pinType:String,pinId:String):Completable = invoke<PinAPI>().addPin(pinType,pinId)

    fun deletePin(pinType:String,pinId:String):Completable = invoke<PinAPI>().deletePin(pinType,pinId)

    fun searchTopic(topicName:String): Observable<Topic> = invoke<MessageAPI>().searchTopic(topicName)

    fun getFileFromServer(url:String) : Single<ResponseBody> = invoke<MessageAPI>().getFileFromServer(url)

    //PUT: Update data to server
    fun updateFcmToken(token: String?): Completable = invoke<Authentication>().updateFcmToken(token)

    fun updateJoinedState(
        callId: String,
        privacyMode: String,
        topicId: String,
    ): Completable = invoke<CallAPI>().updateJoinedState(callId, privacyMode, topicId)

    fun updateLeftState(callId: String): Completable = invoke<CallAPI>().updateLeftState(callId)

    fun responseAcceptFriend(senderId: String): Completable =
        invoke<FriendsAPI>().responseAcceptFriend(senderId)

    fun responseDeclineFriend(senderId: String): Completable =
        invoke<FriendsAPI>().responseDeclineFriend(senderId)

    fun cancelFriendRequest(customerId: String): Completable =
        invoke<FriendsAPI>().cancelFriendRequest(customerId)

    fun editTopicName(topicId: String, newTopicName: String) =
        invoke<MessageAPI>().editTopicName(topicId, newTopicName)

    fun deleteFriend(customerID: ArrayList<String>) = invoke<FriendsAPI>().deleteFriend(customerID)

    fun deleteMessage(messageId:Int) :Completable = invoke<MessageAPI>().deleteMessage(messageId)

    fun editMessage(messageId: Int,messageContent: String): Completable =
        invoke<MessageAPI>().editMessage(messageId, messageContent)
}
