package com.mightyId.activities.main.ui.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.Account
import com.mightyId.models.Contact
import com.mightyId.models.RequestAddFriendModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class ContactViewModel : ViewModel() {

    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _listFriend = MutableLiveData<ArrayList<Account>>()
    var listFriend : LiveData<ArrayList<Account>> = _listFriend
    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable : LiveData<Boolean> = _isNetworkAvailable

    fun getCurrentUserFriendList(){
        disposable.add(
            myService.getCurrentUserFriendList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Contact>() {
                    override fun onSuccess(t: Contact) {
                        _listFriend.value = t.result
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("getCurrentUserFriendList").e("onError: $e")
                        _isNetworkAvailable.value = false

                    }
                })
        )
    }

    fun sendResponseAccept(userData: RequestAddFriendModel){
        disposable.add(
            myService.responseAcceptFriend(userData.senderId!!)
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

    fun sendResponseDecline(userData: RequestAddFriendModel){
        disposable.add(
            myService.responseDeclineFriend(userData.senderId!!)
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

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}