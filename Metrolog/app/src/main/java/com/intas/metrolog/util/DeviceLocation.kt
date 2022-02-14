package com.intas.metrolog.util

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

class DeviceLocation(val activity: Context) {

    private var isLocationUpdatesActive: Boolean = false
    private var errorText: String = ""

    private var _currentLocation = MutableLiveData<Location>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback

    val currentLocation: LiveData<Location>
        get() = _currentLocation

    companion object {
        const val CHECK_SETTINGS_CODE = 111;
    }

    /**
     * Конструктор
     */
    init {
        initLocationSettings()
    }

    /**
     * Общая инициализация
     */
    private fun initLocationSettings() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        settingsClient = LocationServices.getSettingsClient(activity)
        buildLocationRequest()
        buildLocationCallback()
        buildLocationSettingsRequest()
    }

    /**
     * Обновление текущего местоположения, сообщение всем подписчикам об изменении местоположения
     * @param currentLocation текущее местоположение, объект класса MutableLiveData<[android.location.Location]>
     */
    private fun setCurrentLocation(currentLocation: MutableLiveData<Location>) {
        this._currentLocation = currentLocation
    }

    /**
     * Запуск или остановка алгоритма отслеживания текущего местоположения
     * @param locationUpdatesActive если true - отслеживание запущено, false - отслеживание остановлено
     */
    fun setLocationUpdatesActive(locationUpdatesActive: Boolean) {
        isLocationUpdatesActive = locationUpdatesActive
        if (isLocationUpdatesActive) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    /**
     * Остановка отслеживания изменения местоположения
     */
    private fun stopLocationUpdates() {
        if (!isLocationUpdatesActive) {
            return
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
            .addOnCompleteListener { task ->
                isLocationUpdatesActive = false
            }
    }

    /**
     * Запуск отслеживания изменения местоположения
     */
    private fun startLocationUpdates() {
        isLocationUpdatesActive = true
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener { locationSettingsResponse ->
                Looper.myLooper()?.let {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback, it
                    )
                }
                updateLocationUi()
            }.addOnFailureListener { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    /*LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                     //Нужно разрешение пользователя
                        try {
                            val resolvableApiException = e as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                activity,
                                CHECK_SETTINGS_CODE
                            )
                        } catch (sie: SendIntentException) {
                            sie.printStackTrace()
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Невозможно изменить настройки, т.к. пользователь должен вручную
                        // установить настройки определения местоположения
                        //String message = "Adjust location settings on your device";
                        //Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show();
                        //Log.d("Location", message);
                        errorText = "Установите настройки определения местоположения"
                        isLocationUpdatesActive = false
                    }
                    else -> {
                    }*/
                }
                updateLocationUi()
            }
    }

    /**
     * Создание запроса настроек местоположения
     */
    private fun buildLocationSettingsRequest() {

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }

    /**
     * Создание обратного вызова для получения местоположения
     */
    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                _currentLocation.value = locationResult.lastLocation
                updateLocationUi()
            }
        }
    }

    /**
     * Передача информации о местоположении в активити
     */
    private fun updateLocationUi() = setCurrentLocation(_currentLocation)

    /**
     * Настройка параметров
     */
    private fun buildLocationRequest() {

        locationRequest = LocationRequest.create()

        with(locationRequest) {
            this.let {
                it.interval = 60000 * 1
                it.fastestInterval = 20000
                it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
        }

    }
}