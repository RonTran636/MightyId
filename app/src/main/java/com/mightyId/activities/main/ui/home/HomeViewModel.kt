package com.mightyId.activities.main.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.Account
import com.mightyId.models.Contact
import com.mightyId.utils.Common
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import timber.log.Timber

class HomeViewModel : ViewModel() {

    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _listSuggestContact = MutableLiveData<ArrayList<Account>>()
    var listSuggestContact : LiveData<ArrayList<Account>> = _listSuggestContact
    private val _isTokenUpdated = MutableLiveData<Boolean>()
    val isTokenUpdated : LiveData<Boolean> = _isTokenUpdated
    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable : LiveData<Boolean> = _isNetworkAvailable

    //Loading all data from Server
    fun loadRecommendContact(customerId: String) {
        disposable.add(
            myService.getRecommendContact(customerId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Contact>() {
                    override fun onSuccess(t: Contact) {
                        _listSuggestContact.value = t.result
                    }

                    override fun onError(e: Throwable) {
                        Timber.tag("HomeViewModel").e("loadRecommendContact onError: $e")
                        _isNetworkAvailable.value = false
                    }
                })
        )
    }
    //End of loading all data from Firebase Database

    fun updateServerToken(){
        disposable.add(
            myService.updateServerToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<JSONObject>() {
                    override fun onSuccess(t: JSONObject) {
                        val temp = t.getString("token")
                        Common.currentAccount!!.serverToken = temp
                        _isTokenUpdated.value = true
                        Timber.tag("MainActivityViewModel").d("onSuccess: current token is: $temp")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("MainActivityViewModel").e("updateServerToken onError: $e")
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}