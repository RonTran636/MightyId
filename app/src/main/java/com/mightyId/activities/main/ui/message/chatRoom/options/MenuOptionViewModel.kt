package com.mightyId.activities.main.ui.message.chatRoom.options

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import com.mightyId.utils.getFileName
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.Account
import com.mightyId.models.Contact
import com.mightyId.services.UploadRequestBody
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MenuOptionViewModel(application: Application) : AndroidViewModel(application),
    UploadRequestBody.UploadCallback {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _application = application
    private val _listMember = MutableLiveData<ArrayList<Account>>()
    val listMember: LiveData<ArrayList<Account>> = _listMember
    fun editTopicName(topicId: String, newTopicName: String) {
        disposable.add(
            myService.editTopicName(topicId, newTopicName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Toast.makeText(_application, "Topic name changed", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("editTopicName onError: $e`")
                    }
                })
        )
    }

    fun getMember(topicId: String) {
        disposable.add(
            myService.getMember(topicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Contact>() {
                    override fun onSuccess(t: Contact) {
                        _listMember.value = t.result
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomRemoveUserViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun deleteFriend(customerId: ArrayList<String>) {
        disposable.add(
            myService.deleteFriend(customerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {

                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("UserDetailViewModel").e("deleteFriend onError: $e")
                    }
                })
        )
    }

    fun changeTopicImage(topicId: String, selectedImage: Uri) {
        val parcelFileDescriptor =
            _application.contentResolver.openFileDescriptor(selectedImage, "r", null) ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file =
            File(_application.cacheDir, _application.contentResolver.getFileName(selectedImage))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        val topic = RequestBody.create(MediaType.parse("text/plain"), topicId)
        val body = UploadRequestBody(file, "file", this)
        val apiKey = RequestBody.create(MediaType.parse("text/plain"), Key.KEY)
        val headers: MutableMap<String, String> = HashMap()
        headers["Authorization"] = "Bearer " + Common.currentAccount?.serverToken

        disposable.add(
            myService.changeTopicImage(
                topic,
                MultipartBody.Part.createFormData("image", file.name, body),
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
    }
}