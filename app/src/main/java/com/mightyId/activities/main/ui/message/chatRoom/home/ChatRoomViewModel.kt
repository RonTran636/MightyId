package com.mightyId.activities.main.ui.message.chatRoom.home

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mightyId.R
import com.mightyId.models.Message
import com.mightyId.models.MessageItem
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import com.mightyId.utils.getFileName
import com.mightyId.workManager.DownloadFileWorker
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.PersonalChatInfo
import com.mightyId.models.TopicItem
import com.mightyId.services.UploadRequestBody
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import timber.log.Timber
import java.io.*

class ChatRoomViewModel(application: Application) : AndroidViewModel(application),
    UploadRequestBody.UploadCallback {

    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val gson = Gson()
    private val _application = application
    val chatList = MutableLiveData<Message>()
    private val _messageInfo = MutableLiveData<PersonalChatInfo>()
    val messageInfo: LiveData<PersonalChatInfo> = _messageInfo
    private val _pinMessage = MutableLiveData<ArrayList<MessageItem>>()
    val pinMessage: LiveData<ArrayList<MessageItem>> = _pinMessage
    private val _topicInfo = MutableLiveData<TopicItem>()
    val topicInfo: LiveData<TopicItem> = _topicInfo
    private val _taskAssigned = MutableLiveData<Boolean>()
    val taskAssigned: LiveData<Boolean> = _taskAssigned
    private val _exception = MutableLiveData<String>()


    fun getMessage(topicId: String, lastMessageId: Int?) {
        disposable.add(
            myService.getMessage(topicId, lastMessageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Message>() {
                    override fun onSuccess(t: Message) {
                        Timber.tag("ChatRoomViewModel").d("onSuccess: data recive $t")
                        chatList.value = t
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("getMessage onError: $e")
                    }
                })
        )
    }

    fun sendMessage(topicId: String, messageContent: String, parentId: Int?) {
        disposable.add(
            myService.sendMessage(topicId, messageContent, parentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("ChatRoomViewModel").d("onComplete: Success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("sendMessage onError: $e")
                    }
                })

        )
    }

    fun getPrivateMessageInfo(customerID: String) {
        disposable.add(
            myService.getTopicId(customerID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<JsonObject>() {
                    override fun onError(e: Throwable?) {
                        Timber.tag("TopicViewModel").e("getTopicId onError: $e")
                    }

                    override fun onSuccess(t: JsonObject) {
                        Timber.tag("ChatRoomViewModel")
                            .d("onSuccess getPrivateMessageInfo: data: $t")
                        _messageInfo.value = gson.fromJson(
                            t.getAsJsonObject("result"),
                            PersonalChatInfo::class.java
                        )
                    }
                })
        )
    }

    fun createTodo(
        title: String,
        topicId: String,
        content: String,
        assignee: ArrayList<String>,
        deadline: String
    ) {
        disposable.add(
            myService.createTodo(title, topicId, content, assignee, deadline)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<JsonObject>() {
                    override fun onSuccess(t: JsonObject) {
                        _taskAssigned.value = true
                        Timber.tag("ChatRoomViewModel").d("onSuccess: todo created: $t")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError createTodo: $e")
                    }
                })
        )
    }

    fun buildImageBodyPart(context: Context, name: String, bitmap: Bitmap): MultipartBody.Part {
        val file = File(context.cacheDir, name)
        val bos = ByteArrayOutputStream()
        val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
        disposable.add(
            Completable.fromCallable {
                file.createNewFile()
                //Convert bitmap to byte array
                bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos)
                val bitMapData = bos.toByteArray()
                //write the bytes in file
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(file)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                try {
                    fos?.write(bitMapData)
                    fos?.flush()
                    fos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        )
        return MultipartBody.Part.createFormData(name, file.name, reqFile)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun uploadImage(topicId: String, selectedImage: Uri) {
        val parcelFileDescriptor =
            _application.contentResolver.openFileDescriptor(selectedImage, "r", null) ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file =
            File(_application.cacheDir, _application.contentResolver.getFileName(selectedImage))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        val body = UploadRequestBody(file, "file", this)
        val topicIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), topicId)
        val apiKey = RequestBody.create(MediaType.parse("text/plain"), Key.KEY)
        val headers: MutableMap<String, String> = HashMap()
        headers["Authorization"] = "Bearer " + Common.currentAccount?.serverToken

        disposable.add(
            myService.sendFile(
                topicIdRequestBody,
                MultipartBody.Part.createFormData("file", file.name, body),
                apiKey,
                headers
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("ChatRoomViewModel").d("onComplete: Upload file success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError uploadImage: $e")
                    }
                })
        )
    }

    override fun onProgressUpdate(percentage: Int) {
        //TODO: upload progress bar
//        progress_bar.progress = percentage
    }

    fun getFileFromServer(fileName: String, url: String) {
        disposable.add(
            myService.getFileFromServer(url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                    override fun onSuccess(result: ResponseBody) {
                        Timber.tag("ChatRoomViewModel")
                            .d("onSuccess getFileFromServer: success, $result")
                        downloadFile(fileName, result)
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError downloadFile: $e")
                    }
                })
        )
    }

    fun downloadFile(fileName: String, file: ResponseBody) {
        Timber.tag("ChatRoomViewModel").d("downloadFile: called")
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val inputData = Data.Builder()
            .putString("fileDownload", gson.toJson(file))
            .putString("fileName", fileName)
            .build()
        val workRequest = OneTimeWorkRequest.Builder(DownloadFileWorker::class.java)
//            .setInitialDelay(2, TimeUnit.SECONDS)
//            .setConstraints(constraint)
            .setInputData(inputData)
            .addTag("fileDownload")
            .build()
        WorkManager.getInstance(_application).enqueue(workRequest)
    }

    fun getTopicInfo(topicId: String) {
        disposable.add(
            myService.getTopicInfo(topicId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<JsonObject>() {
                    override fun onSuccess(t: JsonObject) {
                        val temp =
                            Gson().fromJson(t.getAsJsonObject("result"), TopicItem::class.java)
                        _topicInfo.value = temp
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun deleteMessage(messageId: Int) {
        disposable.add(
            myService.deleteMessage(messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Toast.makeText(
                            _application, _application.getString(R.string.message_deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("DialogMessageOptionVM").e("onError deleteMessage: $e")
                    }
                })
        )
    }

    fun editMessage(messageId: Int, messageContent: String) {
        disposable.add(
            myService.editMessage(messageId, messageContent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("ChatRoomViewModel").d("onComplete: Success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError editMessage: $e")
                    }
                })
        )
    }

    fun pinMessage(topicId: String, messageId: Int) {
        disposable.add(
            myService.pinMessage(topicId, messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Toast.makeText(_application, "Message pinned", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError pinMessage: $e")
                    }
                })
        )
    }

    fun getPinMessage(topicId: String) {
        disposable.add(
            myService.getPinMessage(topicId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<Message>() {
                    override fun onSuccess(t: Message) {
                        _pinMessage.value = t.result
                        Timber.tag("ChatRoomViewModel")
                            .d("getPinMessage onSuccess: data: ${t.result}")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("getPinMessage onError: $e")
                    }
                })
        )
    }

    fun unpinMessage(topicId: String, messageId: Int) {
        disposable.add(
            myService.unpinMessage(topicId, messageId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Toast.makeText(_application, "Unpinned message", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("onError: $e")
                    }
                })
        )
    }
}