package com.intas.metrolog.ui.login

import android.app.Application
import android.util.Log
import android.view.View
import androidx.databinding.ObservableBoolean
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

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    var login: String? = null
    var password: String? = null
    var appVersion: String? = null
    var rememberCheckBoxValue: ObservableBoolean? = null

    var onSuccess: ((AuthUser) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onFailure: ((String) -> Unit)? = null

    private val compositeDisposable = CompositeDisposable()

    private val db = AppDatabase.getInstance(application)

    fun onTargetFocusChanged(view: View, hasFocus: Boolean) {}

    /**
     * Удаление всех данных кроме справочников
     */
    fun deleteAllData() {
        viewModelScope.launch {
            db.userLocationDao().deleteAllUserLocations()
        }
    }

    /**
     * Асинхронная функция добавления данных о пользователе в БД
     */
    private fun insertUser(authUser: AuthUser) {

        Journal.insertJournal("LoginViewModel->insertUser", journalText = authUser)
        Log.d("MO_INSERT_USER", authUser.toString())

        viewModelScope.launch {
            db.authUserDao().deleteAll()
            db.authUserDao().insertAuthUser(authUser)
        }
    }

    /**
     * Функция обработки нажатия пользователем кнопки авторизации
     */
    fun onLoginClickButton(view: View) {

        Journal.insertJournal("LoginViewModel->onLoginClickButton", "")

        if (login.isNullOrEmpty() || password.isNullOrEmpty()) {
            onFailure?.invoke("Введите логин и пароль")
            return
        }

        Util.serverIpAddress =
            PreferenceManager.getDefaultSharedPreferences(getApplication())
                .getString(
                    "server_ip_address",
                    getApplication<Application>().applicationContext.getString(R.string.settings_activity_ip_address_default_value)
                ).toString()

        Journal.insertJournal("LoginViewModel->onLoginClickButton->serverIpAddress", Util.serverIpAddress)

        val authDateTime = AppPreferences.userLoginDateTime
        val authUser = AppPreferences.authUser
        val authLogin = AppPreferences.login
        val authPassword = AppPreferences.password
        val deltaTime = DateTimeUtil.getUnixDateTimeNow() - authDateTime

        //если сохраненнный пользователь существует и с момента сохранения прошло меньше суток
        if (authUser != null && deltaTime < 86400 && authLogin.equals(login) && authPassword.equals(
                password
            )
        ) {
            Journal.insertJournal("LoginViewModel->onLoginClickButton", journalText = authUser)
            onSuccess?.invoke(authUser)
            AppPreferences.userLoginDateTime = DateTimeUtil.getUnixDateTimeNow()
            return
        }

        val disposable = ApiFactory.apiService.authUser(login = login!!, password = password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.data == null) {
                    response.requestError?.message?.let {
                        onFailure?.invoke(it)
                        Log.d("MO_AUTH_USER", it)
                        Journal.insertJournal(
                            "LoginViewModel->authUser->requestError",
                            journalText = it
                        )
                    }
                } else {
                    insertUser(response.data)
                    Util.authUser = response.data
                    onSuccess?.invoke(response.data)

                    AppPreferences.userLoginDateTime = DateTimeUtil.getUnixDateTimeNow()

                    Log.d("MO_AUTH_USER", response.data.toString())
                    Journal.insertJournal("LoginViewModel->authUser", journalText = response.data)
                }
            }, {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MO_AUTH_USER", it.message.toString())
                onError?.invoke("Отсутствует соединение")
                Journal.insertJournal("LoginViewModel->authUser", journalText = it.localizedMessage)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}