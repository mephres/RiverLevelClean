package com.intas.metrolog.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.api.ApiFactory
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ACCURACY
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ALTITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_BEARING
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ELAPSED_REALTIME_NANOS
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LATITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LONGITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_PROVIDER
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_SPEED
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_TIME
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_USER_ID
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.metrolog.util.Util
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    private var sendUserLocationDisposable: Disposable? = null

    val notSendedUserLocationList = db.userLocationDao().getNotSendedUserLocationList()

    init {

    }

    /**
     * Сохранение геопозиции устройства в БД
     * @param userLocation - геопозиция устройства
     */
    fun insertUserLocation(userLocation: UserLocation) {

        Log.d("MM_INSERT_USER_LOCATION", userLocation.toString())

        viewModelScope.launch {
            db.userLocationDao().insertUserLocation(userLocation)
        }
    }

    /**
     * Отправка данных геопозиции на сервер
     * @param userLocation - геопозиция устройства
     */
    fun sendUserLocation(userLocation: UserLocation) {

        sendUserLocationDisposable?.let {
            compositeDisposable.remove(it)
        }

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_ID] = userLocation.id.toString()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_ACCURACY] = userLocation.accuracy.toString()
        map[QUERY_PARAM_ALTITUDE] = userLocation.altitude.toString()
        map[QUERY_PARAM_BEARING] = userLocation.bearing.toString()
        map[QUERY_PARAM_ELAPSED_REALTIME_NANOS] = userLocation.elapsedRealtimeNanos.toString()
        map[QUERY_PARAM_LATITUDE] = userLocation.latitude.toString()
        map[QUERY_PARAM_LONGITUDE] = userLocation.longitude.toString()
        map[QUERY_PARAM_PROVIDER] = userLocation.provider.toString()
        map[QUERY_PARAM_SPEED] = userLocation.speed.toString()
        map[QUERY_PARAM_TIME] = userLocation.time.toString()

        sendUserLocationDisposable = ApiFactory.apiService.addLocation(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_USER_LOCATION", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setUserLocationSendedById(it)
                    }
                }
                Log.d("MM_SEND_USER_LOCATION", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_USER_LOCATION", it.message.toString())
                })
        sendUserLocationDisposable?.let {
            compositeDisposable.add(it)
        }
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setUserLocationSendedById(id: Long) {

        Log.d("MM_SET_LOCATION_SEND", id.toString())

        viewModelScope.launch {
            db.userLocationDao().setUserLocationSendedById(id)
        }
    }
}