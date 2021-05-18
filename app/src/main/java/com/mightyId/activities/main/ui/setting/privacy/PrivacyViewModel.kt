package com.mightyId.activities.main.ui.setting.privacy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mightyId.apiCentral.ServiceCentral
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {
    private val _application = application
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()

    fun blockStrangerCall(status: Int) {
        disposable.add(
            myService.blockStrangerCall(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("PrivacyViewModel").d("onComplete blockStrangerCall: success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("PrivacyViewModel").e("onError blockStrangerCall: $e")
                    }
                }
                ))
    }

    fun blockStrangerInviteTopic(status: Int) {
        disposable.add(
            myService.blockStrangerInviteTopic(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("PrivacyViewModel")
                            .d("onComplete blockStrangerInviteTopic: success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("PrivacyViewModel").e("onError blockStrangerInviteTopic: $e")
                    }
                }
                ))
    }

    fun blockStrangerSendMessage(status: Int) {
        disposable.add(
            myService.blockStrangerSendMessage(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("PrivacyViewModel")
                            .d("onComplete blockStrangerSendMessage: success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("PrivacyViewModel").e("onError blockStrangerSendMessage: $e")
                    }
                }
                ))
    }
}