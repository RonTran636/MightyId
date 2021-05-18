package com.mightyId.activities.main.ui.message.chatRoom.addUser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class ChatRoomAddUserViewModel : ViewModel() {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _isAddMemberSuccess = MutableLiveData<Boolean>()
    val isAddMemberSuccess: LiveData<Boolean> = _isAddMemberSuccess

    fun addMember(topicId:String,listMember: ArrayList<String>){
        disposable.add(
            myService.addMember(topicId, listMember)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        _isAddMemberSuccess.value = true
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomAddUserViewModel").e("addMember onError: $e")
                    }
                })
        )
    }
}