package com.mightyId.activities.main.ui.setting

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mightyId.utils.Common
import com.mightyId.utils.Key
import com.mightyId.utils.getFileName
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.services.UploadRequestBody
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingViewModel(application: Application) : AndroidViewModel(application),
    UploadRequestBody.UploadCallback {
    private val _application = application
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _text =
        MutableLiveData<String>().apply { value = Common.currentAccount?.customerName }
    val text: LiveData<String> = _text
    private val _loggedOut = MutableLiveData<Boolean>().apply { value = false }
    val loggedOut: LiveData<Boolean> = _loggedOut

    fun changeAvatar(selectedImage: Uri) {
        val parcelFileDescriptor =
            _application.contentResolver.openFileDescriptor(selectedImage, "r", null) ?: return
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file =
            File(_application.cacheDir, _application.contentResolver.getFileName(selectedImage))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        val body = UploadRequestBody(file, "file", this)
        val apiKey = RequestBody.create(MediaType.parse("text/plain"), Key.KEY)
        val headers: MutableMap<String, String> = HashMap()
        headers["Authorization"] = "Bearer " + Common.currentAccount?.serverToken

        disposable.add(
            myService.changeAvatar(
                MultipartBody.Part.createFormData("avatar", file.name, body),
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

    fun logout(fcmToken: String) {
        disposable.add(
            myService.logout(fcmToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        _loggedOut.value = true
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("SettingViewModel").e("onError: $e")
                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    override fun onProgressUpdate(percentage: Int) {
    }
}
