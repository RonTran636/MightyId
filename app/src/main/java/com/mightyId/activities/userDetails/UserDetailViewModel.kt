package com.mightyId.activities.userDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.CallHistory
import com.mightyId.models.CallHistoryItems
import com.mightyId.models.FriendStatus
import com.mightyId.models.RequestAddFriendModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class UserDetailViewModel(application: Application) : AndroidViewModel(application) {

    val myService = ServiceCentral()
    val disposable = CompositeDisposable()
    private val _listHistory = MutableLiveData<MutableList<CallHistoryItems>>()
    val listHistory : LiveData<MutableList<CallHistoryItems>> = _listHistory
    private val _application = application
    private val _friendStatus = MutableLiveData<Int>()
    val friendStatus :LiveData<Int> = _friendStatus
    val isFriendDeleted = MutableLiveData<Boolean>()

    fun getCallHistoryOf(customerId:String){
        disposable.add(
            myService.getCallHistoryOf(customerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallHistory>() {
                    override fun onSuccess(t: CallHistory) {
                        _listHistory.value = t.result
                        Timber.tag("UserDetailViewModel").d("onSuccess: data :${t.result} ")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("UserDetailViewModel").e("getCallHistoryOf onError: $e")
                    }
                })
        )
    }

    fun getFriendStatusOf(customerId: String){
        disposable.add(
            myService.getFriendStatusOf(customerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<FriendStatus>() {
                    override fun onSuccess(t: FriendStatus) {
                        _friendStatus.value = t.status
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("UserDetailViewModel").e("getFriendStatusOf onError: $e")
                    }
                })
        )
    }

    fun sendResponseAccept(customerId: String){
        disposable.add(
            myService.responseAcceptFriend(customerId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("sendResponseAccept").d("onComplete")
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("onError").e(e)
                    }
                })
        )
    }

    fun sendResponseDecline(customerId: String){
        disposable.add(
            myService.responseDeclineFriend(customerId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("sendResponseDecline").d("onComplete")
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("onError").e(e)
                    }
                })
        )
    }

    fun sendFriendRequest(remoteMsg: RequestAddFriendModel) {
        disposable.add(
            myService.sendFriendRequestToServer(remoteMsg)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("UserDetailViewModel").d("onComplete: Send friend request to server")
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("UserDetailViewModel").e("onError: ")
                    }
                })
        )
    }

    fun deleteFriend(customerId: ArrayList<String>){
        disposable.add(
            myService.deleteFriend(customerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        isFriendDeleted.value = true
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("UserDetailViewModel").e("deleteFriend onError: $e")
                    }
                })
        )
    }
}