package com.mightyId.activities.main.ui.message.chatRoom.options.storedMedia.nestedRv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mightyId.models.Message
import com.mightyId.models.MessageItem
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.TodoList
import com.mightyId.models.TodoListItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

class ViewPagerViewModel(application: Application) : AndroidViewModel(application) {
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _listMedia = MutableLiveData<MutableList<MessageItem>>()
    val listMedia: LiveData<MutableList<MessageItem>> = _listMedia
    private val _listLink = MutableLiveData<MutableList<MessageItem>>()
    val listLink: LiveData<MutableList<MessageItem>> = _listLink
    private val _listFile = MutableLiveData<MutableList<MessageItem>>()
    val listFile: LiveData<MutableList<MessageItem>> = _listFile
    private val _listTodo = MutableLiveData<MutableList<TodoListItem>>()
    val listTodo: LiveData<MutableList<TodoListItem>> = _listTodo

    fun getTopicTodoList(topicId: String) {
        disposable.add(
            myService.getTopicTodoList(topicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<TodoList>() {
                    override fun onSuccess(t: TodoList) {
                        Timber.tag("ViewPagerViewModel")
                            .d("onSuccess getTopicTodoList: data receive :${t.result}")
                        _listTodo.value = t.result
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("StoredMediaViewModel").e("onError: $e")
                    }
                })
        )
    }


    fun getTopicPhotoAndVideo(topicId: String) {
        disposable.add(
            myService.getPhotoAndVideo(topicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Message>() {
                    override fun onSuccess(t: Message) {
                        Timber.tag("ViewPagerViewModel")
                            .d("onSuccess getTopicPhotoAndVideo: ${t.result}")
                        _listMedia.value = t.result
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("StoredMediaViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun getTopicLinks(topicId: String) {
        disposable.add(
            myService.getTopicLink(topicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Message>() {
                    override fun onSuccess(t: Message) {
                        Timber.tag("ViewPagerViewModel").d("onSuccess getTopicLinks: ${t.result}")
                        _listLink.value = t.result
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("StoredMediaViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun getTopicFiles(topicId: String) {
        disposable.add(
            myService.getTopicFile(topicId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Message>() {
                    override fun onSuccess(t: Message) {
                        Timber.tag("ViewPagerViewModel").d("onSuccess getTopicFiles: ${t.result}")
                        _listFile.value = t.result
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ViewPagerViewModel").e("onError: $e")
                    }
                })
        )
    }

    fun updateStatus(todoId: Int, status: String) {
        disposable.add(
            myService.updateStatus(todoId, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("ViewPagerViewModel").d("updateStatus onComplete: success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("ViewPagerViewModel").e("updateStatus onError: $e")
                    }
                })

        )
    }
}