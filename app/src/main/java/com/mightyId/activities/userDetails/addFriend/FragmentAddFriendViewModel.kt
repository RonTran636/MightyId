package com.mightyId.activities.userDetails.addFriend

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.RequestAddFriendModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class FragmentAddFriendViewModel(application: Application) : AndroidViewModel(application) {
    private val _application = application
    val myService = ServiceCentral()
    val disposable = CompositeDisposable()
    fun sendFriendRequest(remoteMsg: RequestAddFriendModel) {
        Timber.tag("FragmentAddFriendViewModel").d("sendFriendRequest: data sending $remoteMsg")
        disposable.add(
            myService.sendFriendRequestToServer(remoteMsg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("FragmentAddFriendViewModel").d("onComplete: Success")
                        Toast.makeText(_application, "Friend request sent", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("UserDetailViewModel").e("onError: $e")
                    }
                })
        )
    }
}