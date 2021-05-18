package com.mightyId.activities.main.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.CallHistory
import com.mightyId.models.CallHistoryItems
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class HistoryViewModel : ViewModel() {

    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _listHistory = MutableLiveData<MutableList<CallHistoryItems>>()
    var listHistory : LiveData<MutableList<CallHistoryItems>> = _listHistory
    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable : LiveData<Boolean> = _isNetworkAvailable
    fun getCallHistory(){
        disposable.add(
            myService.getCallHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<CallHistory>() {
                    override fun onSuccess(t: CallHistory) {
                        _listHistory.value = t.result.asReversed()
                        Timber.tag("HistoryViewModel").d("onSuccess: data: ${t.result}")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("HistoryViewModel").e("onError: $e")
                        _isNetworkAvailable.value = false
                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}