package com.mightyId.activities.main.ui.message.createNewTopic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.TopicItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class CreateNewTopicViewModel : ViewModel() {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val gson = Gson()
    private val _topicId = MutableLiveData<String>()
    val topicId: LiveData<String> = _topicId

    fun createTopic(
        topicName: String,
        listMember: ArrayList<String>
    ) {
        disposable.add(
            myService.createTopic(topicName, listMember)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<JsonObject>() {
                    override fun onSuccess(t: JsonObject) {
                        Timber.tag("CreateNewTopicViewModel").d("onSuccess: data received: $t")
                        val temp = gson.fromJson(t.getAsJsonObject("result"), TopicItem::class.java)
                        _topicId.value = temp.topicId!!
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("CreateNewTopicViewModel").e("onError: $e")
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
    }
}