package com.intas.metrolog.ui.main

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityMainBinding
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.metrolog.util.DeviceLocation
import com.intas.metrolog.util.Util

class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Util.serverIpAddress = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(
                "server_ip_address",
                getString(R.string.settings_activity_ip_address_default_value)
            ).toString()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initBottomNavigation()
        initDeviceLocationObserver()
        initNotSendedUserLocationObserver()
        initNotSendedEquipObserver()
        initNotSendedEquipDocumentObserver()
        initNotSendedEventObserver()
        initNotSendedEventOperationObserver()
        initNotSendedEventOperationControlObserver()
        initNotSendedEventPhotoObserver()
        initNotSendedRequestObserver()
        initNotSendedEquipInfoObserver()
        initNotSendedRequestPhotoObserver()
    }

    /**
     * Инициализация нижнего меню
     */
    private fun initBottomNavigation() {
        val navigationController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.bottomNavigationView.setupWithNavController(navigationController)
    }

    /**
     * Получение и отправка на сервер неотправленных записей с местоположением пользователя
     */
    private fun initNotSendedUserLocationObserver() {
        viewModel.notSendedUserLocationList.observe(this, {
            for (userLocation in it) {
                viewModel.sendUserLocation(userLocation)
            }
        })
    }

    /**
     * Получение и отправка на сервер списка оборудования с проставленными RFID-метками
     */
    private fun initNotSendedEquipObserver() {
        viewModel.notSendedEquipList.observe(this, {
            for (equip in it) {
                    if (equip.isSendRFID == 0 && !Util.equipRfidQueue.contains(equip.equipId)) {
                        viewModel.sendEquipRFID(equip)
                    }
                    if (equip.isSendGeo == 0 && !Util.equipLocationQueue.contains(equip.equipId)) {
                        viewModel.sendEquipLocation(equip)
                    }
            }
        })
    }

    /**
     * Запуск определения местоположения пользователя
     */
    private fun initDeviceLocationObserver() {
        val deviceLocation = DeviceLocation(this)
        deviceLocation.setLocationUpdatesActive(true)
        deviceLocation
            .currentLocation.observe(this) {
                //Toast.makeText(this, it.latitude.toString(), Toast.LENGTH_LONG).show()

                val userLocation = UserLocation()

                userLocation.accuracy = it.accuracy
                userLocation.altitude = it.altitude
                userLocation.bearing = it.bearing
                userLocation.elapsedRealtimeNanos = it.elapsedRealtimeNanos
                userLocation.latitude = it.latitude
                userLocation.longitude = it.longitude
                userLocation.provider = it.provider
                userLocation.speed = it.speed
                userLocation.time = (it.time / 1000).toLong()
                userLocation.userId = Util.authUser?.userId ?: 0

                viewModel.insertUserLocation(userLocation)
            }
    }

    /**
     * Получение и отправка на сервер неотправленных документов оборудования
     */
    private fun initNotSendedEquipDocumentObserver() {
        viewModel.notSendedEquipDocumentList.observe(this, {
            for (equipDocument in it) {
                viewModel.sendEquipDocument(equipDocument)
            }
        })
    }

    /**
     * Получение и отправка на сервер неотправленных мероприятий
     */
    private fun initNotSendedEventObserver() {
        viewModel.notSendedEventList.observe(this, {
            for (event in it) {
                if (!Util.eventQueue.contains(event.opId)) {
                    viewModel.sendEvent(event)
                }
            }
        })
    }

    /**
     * Получение и отправка на сервер неотправленных операций мероприятия
     */
    private fun initNotSendedEventOperationObserver() {
        viewModel.notSendedEventOperationList.observe(this, {
            for (eventOperation in it) {
                if (!Util.eventOperationQueue.contains(eventOperation.subId)) {
                    if (eventOperation.equipId > 0) {
                        viewModel.sendComplexEventOperation(eventOperation)
                    } else {
                        viewModel.sendEventOperation(eventOperation)
                    }
                }
            }
        })
    }

    /**
     * Получение и отправка на сервер неотправленного операционного контроля
     */
    private fun initNotSendedEventOperationControlObserver() {
        viewModel.getNotSendedEventOperationControlList.observe(this, {
            for (eventOperationControl in it) {
                if (!Util.eventOperationControlQueue.contains(eventOperationControl.id)) {
                    viewModel.sendEventOperationControl(eventOperationControl)
                }
            }
        })
    }

    /**
     * Получение и отправка на сервер неотправленного операционного контроля
     */
    private fun initNotSendedEventPhotoObserver() {
        viewModel.getNotSendedEventPhotoList.observe(this, {
            for (eventPhoto in it) {
                if (!Util.eventPhotoQueue.contains(eventPhoto.id)) {
                    viewModel.sendEventPhoto(eventPhoto)
                }
            }
        })
    }

    /**
     * Получение и отправка на сервер неотправленных заявок
     */
    private fun initNotSendedRequestObserver() {
        viewModel.getNotSendedRequestList.observe(this, {
            for (request in it) {
                if (!Util.requestQueue.contains(request.id)) {
                    viewModel.sendRequest(request)
                }
            }
        })
    }

    /**
     * Получение и отправка на сервер комментария к оборудованию
     */
    private fun initNotSendedEquipInfoObserver() {
        viewModel.getNotSendedEquipInfoList.observe(this, {
            for (equipInfo in it) {
                if (!Util.equipInfoQueue.contains(equipInfo.id)) {
                    viewModel.sendEquipInfo(equipInfo)
                }
            }
        })
    }

    /**
     * Получение и отправка фото к отправленной заявке
     */
    private fun initNotSendedRequestPhotoObserver() {
        viewModel.getNotSendedRequestPhotoList.observe(this, {
            for (requestPhoto in it) {
                if (!Util.requestPhoto.contains(requestPhoto.id)) {
                    viewModel.sendRequestPhoto(requestPhoto)
                }
            }
        })
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        //получаем текущий фрагмент
        val backStackEntryCount = navHostFragment?.childFragmentManager?.backStackEntryCount

        if (backStackEntryCount == 0) {
            if (doubleBackToExitPressedOnce) {
                finishAndRemoveTask()
                return
            }
            this.doubleBackToExitPressedOnce = true
            showToast(getString(R.string.exit_message))

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        } else {
            super.onBackPressed()
            return
        }
    }
}