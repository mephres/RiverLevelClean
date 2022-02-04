package com.intas.metrolog.ui.login.pin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.R
import com.intas.metrolog.api.ApiFactory
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.authuser.AuthUser
import com.intas.metrolog.util.AppPreferences
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class PinCodeViewModel(application: Application) : AndroidViewModel(application) {

    var onSuccess: ((AuthUser) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onFailure: ((String) -> Unit)? = null

    private val compositeDisposable = CompositeDisposable()

    private val db = AppDatabase.getInstance(application)

    /**
     * Асинхронная функция добавления данных о пользователе в БД
     */
    private fun insertUser(authUser: AuthUser) {

        Journal.insertJournal("PinCodeViewModel->insertUser", journalText = authUser)
        Log.d("MO_INSERT_USER", authUser.toString())

        viewModelScope.launch {
            db.authUserDao().insertAuthUser(authUser)
        }
    }

    /**
     * Функция обработки нажатия пользователем кнопки авторизации
     */
    fun authUser() {

        val login = AppPreferences.login ?: ""
        val password = AppPreferences.password ?: ""
        Log.d("MO_AUTH_USER", "Login: $login Pass: $password")

        Util.serverIpAddress =
            PreferenceManager.getDefaultSharedPreferences(getApplication())
                .getString("server_ip_address", getApplication<Application>().applicationContext.getString(
                    R.string.settings_activity_ip_address_default_value)).toString()

        val authDateTime = AppPreferences.userLoginDateTime
        val authUser = AppPreferences.authUser
        val deltaTime = DateTimeUtil.getUnixDateTimeNow() - authDateTime

        //если сохраненнный пользователь существует и с момента сохранения прошло меньше суток
        if (authUser != null && deltaTime < 86400) {
            Journal.insertJournal("PinCodeViewModel->authUser", journalText = authUser)
            onSuccess?.invoke(authUser)
            AppPreferences.userLoginDateTime = DateTimeUtil.getUnixDateTimeNow()
            Util.authUser = authUser
            return
        }

        val disposable = ApiFactory.apiService.authUser(login, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.data == null) {
                    response.requestError?.message?.let {
                        onFailure?.invoke(it)
                        Log.d("MO_AUTH_USER", it)
                        Journal.insertJournal("PinCodeViewModel->authUser->requestError", journalText = it)
                    }
                } else {
                    insertUser(response.data)
                    onSuccess?.invoke(response.data)
                    Util.authUser = response.data

                    AppPreferences.userLoginDateTime = DateTimeUtil.getUnixDateTimeNow()

                    Log.d("MO_AUTH_USER", response.data.toString())
                    Journal.insertJournal("PinCodeViewModel->authUser", journalText = response.data)
                }
            }, {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MO_AUTH_USER", it.message.toString())
                onError?.invoke(it.message.toString())
                Journal.insertJournal("PinCodeViewModel->authUser->throwable", journalText = it.localizedMessage)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}