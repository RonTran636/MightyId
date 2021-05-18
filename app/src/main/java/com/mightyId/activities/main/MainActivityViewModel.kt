package com.mightyId.activities.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.AuthorizationModel
import com.mightyId.models.server.ServerNotify
import com.mightyId.utils.Common
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class MainActivityViewModel: ViewModel() {
    val myService = ServiceCentral()
    val disposable = CompositeDisposable()
    val notifyInfo = MutableLiveData<ServerNotify>()
    fun updateFcmToken(token: String) {
        disposable.add(
            myService.updateFcmToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("updateFcmToken").d("onComplete: Token updated")
                        Common.currentAccount?.fcmToken = token
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("updateFcmToken").e("onError: $e")
                    }
                })
        )
    }

    fun getUserInfo(){
        disposable.add(
            myService.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<AuthorizationModel>() {
                    override fun onSuccess(t: AuthorizationModel) {
                        Common.currentAccount!!.strangerCall = t.currentUser.strangerCall
                        Common.currentAccount!!.strangeInviteTopic = t.currentUser.strangeInviteTopic
                        Common.currentAccount!!.strangerMessage = t.currentUser.strangerMessage
                        Timber.tag("MainActivityViewModel").d("onSuccess: account: ${Common.currentAccount!!}")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("MainActivityViewModel").e("onError getUserInfo: $e")
                    }
                })
        )
    }

    fun getNotifyInfo(){
        disposable.add(
            myService.getNotifyInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ServerNotify>() {
                    override fun onSuccess(t: ServerNotify) {
                        notifyInfo.value = t
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("MainActivityViewModel").e("getNotifyInfo onError: $e")
                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}