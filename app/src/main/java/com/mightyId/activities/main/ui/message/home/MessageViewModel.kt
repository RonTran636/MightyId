package com.mightyId.activities.main.ui.message.home

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay3.PublishRelay
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.RecentMessage
import com.mightyId.models.TopicItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    val myService = ServiceCentral()
    val disposable = CompositeDisposable()
    private val _application = application
    private val _listMessage = MutableLiveData<RecentMessage>()
    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable : LiveData<Boolean> = _isNetworkAvailable
    val listMessage: LiveData<RecentMessage> = _listMessage
    val isMessageSent = MutableLiveData<Boolean>()
    val isTopicPinned = MutableLiveData<Boolean>()

    fun getRecentMessage(pages: Int) {
        disposable.add(
            myService.getRecentMessage(pages)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<RecentMessage>() {
                    override fun onSuccess(t: RecentMessage) {
                        Timber.tag("CreateNewTopicViewModel").d("onSuccess: data received: $t")
                        _listMessage.value = t
                    }

                    override fun onError(e: Throwable?) {
                        _isNetworkAvailable.value =false
                        Timber.tag("MessageViewModel").e("getListMessage onError: $e")
                    }
                })
        )
    }

    fun addPin(pinType: String, pinId: String) {
        disposable.add(
            myService.addPin(pinType, pinId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        isTopicPinned.value = true
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("MessageViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun deletePin(pinType: String, pinId: String) {
        disposable.add(
            myService.deletePin(pinType, pinId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        isTopicPinned.value = false
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("MessageViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun sendMessage(topicId: String, messageContent: String) {
        disposable.add(
            myService.sendMessage(topicId, messageContent, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        isMessageSent.value = true
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ChatRoomViewModel").e("sendMessage onError: $e")
                    }
                })

        )
    }

    fun archiveTopic(listTopicArchive: ArrayList<String>){
        disposable.add(
            myService.archiveTopic(listTopicArchive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Toast.makeText(_application, "Topic archived", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("MessageViewModel").e("onError archiveTopic: $e")
                    }
                })
        )
    }

    /**
     * Search topic
     */
    private val _searchResult = MutableLiveData<MutableList<TopicItem>>()
    var searchResult: LiveData<MutableList<TopicItem>> = _searchResult
    private val autoCompletePublishSubject = PublishRelay.create<String>()
    //Called on every character change made to the search `EditText`
    fun onInputStateChanged(query: String) {
        autoCompletePublishSubject.accept(query.trim())
    }

    /**
     * Called only once when the `ViewModel` is being created
     * Initialises the autocomplete publish subject
     */
    fun configureAutoComplete() {
        disposable.add(
            autoCompletePublishSubject
                .debounce(1000, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .switchMap { myService.searchTopic(it) }
                .subscribe({
                    _searchResult.postValue(it.result)
                    Timber.tag("MessageViewModel").d("configureAutoComplete: result : ${it.result}")
                }, {
                    Timber.tag("SearchViewModel").e("configureAutoComplete: ${it.message}")
                })
        )
    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}