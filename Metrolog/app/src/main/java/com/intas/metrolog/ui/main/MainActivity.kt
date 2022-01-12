package com.intas.metrolog.ui.main

import android.os.Bundle
import android.widget.Toast
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

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: MainViewModel

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
     * Запуск определения местоположения пользователя
     */
    private fun initDeviceLocationObserver() {
        val deviceLocation = DeviceLocation(this)
        deviceLocation.setLocationUpdatesActive(true)
        deviceLocation
            .currentLocation.observe(this) {
                Toast.makeText(this, it.latitude.toString(), Toast.LENGTH_LONG).show()

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
}