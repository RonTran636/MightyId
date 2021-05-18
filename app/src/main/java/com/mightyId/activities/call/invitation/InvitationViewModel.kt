package com.mightyId.activities.call.invitation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.ResponseModel
import com.mightyId.models.TopicItem
import com.mightyId.models.server.ServerCallModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class InvitationViewModel : ViewModel() {

    private val _meetingInfo = MutableLiveData<ResponseModel>()
    val meetingInfo: LiveData<ResponseModel> = _meetingInfo

    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()

    fun sendRequestCall(serverCallModel: ServerCallModel) {
        Timber.tag("InvitationViewModel").d("sendRequestCall: receive request call: $serverCallModel")
        disposable.add(
            myService.sendRequestCall(serverCallModel)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ResponseModel>() {
                    override fun onSuccess(t: ResponseModel) {
                        _meetingInfo.value = t
                    }
                    override fun onError(error: Throwable) {
                        Timber.tag("InvitationViewModel").e("sendRequestCall onError: $error")

                    }
                })
        )
    }

    fun joinExistingTopic(topicItem: TopicItem, meetingType: String) {
        Timber.tag("InvitationViewModel").d("joinExistingTopic: Called")
        val serverCallModel = ServerCallModel(
            meetingType = meetingType,
            topicId = topicItem.topicId,
            privacy = "public",
            callId = topicItem.callId
        )
        sendRequestCall(serverCallModel)
    }

    fun sendResponseRequestCall(
        callId: String,
        responseAction: String,
        topicId: String,
    ) {
        disposable.add(
            myService.sendResponseRequestCall(callId, responseAction, topicId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("sendResponseRequestCall").d("onComplete")
                    }

                    override fun onError(error: Throwable) {
                        Timber.tag("sendResponseRequestCall").e(error)
                    }
                })
        )
    }

    fun updateJoinedState(callId: String, privacyMode:String, topicId:String) {
        disposable.add(
            myService.updateJoinedState(callId,privacyMode, topicId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("updateJoinedState").d("onComplete")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("updateJoinedState").e("onError: $e")
                    }
                })
        )
    }

    fun requestJoinExistingMeeting(customerId:String,callId: String,topicId: String){
        disposable.add(
            myService.requestJoinExistingMeeting(customerId,callId,topicId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("requestJoinExistingMeeting").d("onComplete: success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("requestJoinExistingMeeting").e("requestJoinExistingMeeting onError: $e")
                    }
                })
        )
    }

    fun cancelJoinExistingMeeting(customerID:String){
        disposable.add(
            myService.cancelJoinExistingMeeting(customerID)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("InvitationViewModel").d("onComplete: success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("InvitationViewModel").e("cancelJoinExistingMeeting onError: $e")
                    }
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}