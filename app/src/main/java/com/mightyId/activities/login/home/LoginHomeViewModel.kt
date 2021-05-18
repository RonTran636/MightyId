package com.mightyId.activities.login.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.mightyId.R
import com.mightyId.utils.Common
import com.mightyId.apiCentral.ServiceCentral
import com.mightyId.models.Account
import com.mightyId.models.AuthorizationModel
import com.mightyId.models.server.ServerUserModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.io.IOException
import javax.net.ssl.SSLException

class LoginHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    private val myService = ServiceCentral()
    private val disposable = CompositeDisposable()
    private val _loginSuccessDetails = MutableLiveData<Boolean>()
    val loginSuccessDetails: LiveData<Boolean> = _loginSuccessDetails
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private fun registerUserToDatabase(account: Account) {
        disposable.add(
            myService.registerUserToDatabase(account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ServerUserModel>() {
                    override fun onSuccess(t: ServerUserModel) {
                        Timber.tag("LoginHomeViewModel").d("onSuccess:")
                        if (t.message == "The customer email has already been taken.") {
                            _loginSuccessDetails.value = false
                            _errorMessage.postValue("User with same email already exists. Please try with another email address.")
                        } else {
                            Common.currentAccount = t.result
                            _loginSuccessDetails.value = true
                        }
                    }

                    override fun onError(e: Throwable) {
                        when (e) {
                            is IOException -> {
                                _errorMessage.postValue(_application.getString(R.string.IOException))
                            }
                            is SSLException -> {
                                _errorMessage.postValue(_application.getString(R.string.IOException))
                            }
                            else -> {
                                _errorMessage.postValue(_application.getString(R.string.UnknownException))
                            }
                        }
                        Timber.tag("LoginHomeViewModel").e("onError registerUserToDatabase: $e")
                    }
                })
        )
    }

    fun loginWithEmailAndPassword(email:String,password:String){
        disposable.add(
            myService.loginWithEmailAndPassword(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<AuthorizationModel>() {
                    override fun onSuccess(t: AuthorizationModel) {
                        Timber.tag("LoginHomeViewModel")
                            .d("onSuccess: data received : ${t.currentUser}")
                        if (t.success) {
                            Common.currentAccount = t.currentUser
                            _loginSuccessDetails.value = true
                        } else {
                            _errorMessage.value = t.message
                            _loginSuccessDetails.value = false
                        }
                    }

                    override fun onError(e: Throwable?) {
                        when (e) {
                            is IOException -> {
                                _errorMessage.postValue(_application.getString(R.string.IOException))
                            }
                            is SSLException -> {
                                _errorMessage.postValue(_application.getString(R.string.IOException))
                            }
                            else->{
                                _errorMessage.postValue(_application.getString(R.string.UnknownException))
                            }
                        }
                        Timber.tag("LoginHomeViewModel").e("onError loginWithEmailAndPassword: $e")
                        _loginSuccessDetails.value = false
                    }
                })
        )
    }

    fun createNewAccount(email: String, password: String, username: String?) {
        val user = Account()
        user.customerEmail = email
        user.customerName = username
        user.password = password
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            user.fcmToken = it
            registerUserToDatabase(user)
        }
    }

    fun sendPasswordRecoveryEmail(email: String) {
        disposable.add(
            myService.sendPasswordRecoveryEmail(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableCompletableObserver() {
                    override fun onComplete() {
                        Timber.tag("LoginHomeViewModel").d("onComplete: Success")
                    }

                    override fun onError(e: Throwable?) {
                        Timber.tag("LoginHomeViewModel").e("onError: ")
                    }
                })
        )
    }

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }
}