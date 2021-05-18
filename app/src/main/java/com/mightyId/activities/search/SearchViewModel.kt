package com.mightyId.activities.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.PublishRelay
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.Account
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchViewModel : ViewModel() {

    private val autoCompletePublishSubject = PublishRelay.create<String>()
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _searchResult = MutableLiveData<ArrayList<Account>>()
    var searchResult: LiveData<ArrayList<Account>> = _searchResult
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
                .switchMap { myService.searchUserByEmailOrUid(it) }
                .subscribe({
                    _searchResult.postValue(it.result)
                    Timber.tag("SearchViewModel")
                        .d("configureAutoComplete: autoCompletePublishSubject : ${it.result}")
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