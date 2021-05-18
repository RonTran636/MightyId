package com.mightyId.activities.call

import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class MeetingViewModel:ViewModel() {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()

    fun updateLeftState(callId:String) {
        disposable.add(
            myService.updateLeftState(callId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("updateLeftState").d("onComplete")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("updateLeftState").e("onError: $e")
                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}