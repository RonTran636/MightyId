package com.mightyId.activities.main.ui.message.chatRoom.listMember

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.Account
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class ChatRoomRemoveUserViewModel(application: Application) : AndroidViewModel(application) {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _listMember = MutableLiveData<ArrayList<Account>>()
    private val _application = application
    val listMember : LiveData<ArrayList<Account>> = _listMember

    fun removeUser(topicId: String,listMember: ArrayList<String>){
        disposable.add(
            myService.removeMember(topicId, listMember)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Toast.makeText(_application,"Member are removed successfully",Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomRemoveUserViewModel").e("removeUser onError: $e")
                    }
                })
        )
    }

    fun deleteTopic(topicId: String){
        disposable.add(
            myService.deleteTopic(topicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                       //TODO: Move to Message home
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomRemoveUserViewModel").e("deleteTopic onError: $e")
                    }
                })
        )
    }
}