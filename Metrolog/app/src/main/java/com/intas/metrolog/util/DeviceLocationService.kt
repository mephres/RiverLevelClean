package com.intas.metrolog.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.intas.metrolog.R
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.metrolog.ui.main.MainActivity
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.util.Util.SERVICE_NOTIFICATION_CHANNEL_ID
import com.intas.metrolog.util.Util.START_FOREGROUND_ACTION
import com.intas.metrolog.util.Util.STOP_FOREGROUND_ACTION

class DeviceLocationService : LifecycleService() {

    private val viewModel by lazy {
        ViewModelProvider(
            ViewModelStore(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]
    }
    lateinit var notificationChannel: NotificationChannel

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.getAction().equals(START_FOREGROUND_ACTION)) {
            Journal.insertJournal("DeviceLocationService->onStartCommand()", "START_FOREGROUND_ACTION")
            configureServiceNotification()
            init()
            viewModel.notSendedUserLocationList.observe(this) {
                for (userLocation in it) {
                    viewModel.sendUserLocation(userLocation)
                }
            }
        } else if (intent?.getAction().equals(STOP_FOREGROUND_ACTION)) {
            Journal.insertJournal("DeviceLocationService->onStartCommand()", "STOP_FOREGROUND_ACTION")
            stopForeground(true)
            stopSelfResult(startId)
        }

        return START_STICKY
    }

    private fun configureServiceNotification() {

        Journal.insertJournal("DeviceLocationService->configureServiceNotification()", "")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, SERVICE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_navigation_24dp)
            .setContentIntent(pendingIntent)
            .setContentTitle("Координаты по GPS")
            .setContentText("Мобильный Метролог")
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
        startForeground(99999, notification)
    }
    /**
     * Создание канала уведомлений
     * @param _context контекст
     */
    fun createNotificationChannel() {

        Journal.insertJournal("DeviceLocationService->createNotificationChannel()", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = Util.SERVICE_NOTIFICATION_CHANNEL_NAME
            val descriptionText = Util.SERVICE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_MIN

            notificationChannel = NotificationChannel(SERVICE_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    override fun onBind(p0: Intent): IBinder? {
        super.onBind(p0)
        return null
    }

    private fun init() {
        initDeviceLocationObserver()
    }

    private fun initDeviceLocationObserver() {

        Journal.insertJournal("DeviceLocationService->initDeviceLocationObserver()", "")

        val deviceLocation = DeviceLocation(this)
        deviceLocation.setLocationUpdatesActive(true)
        deviceLocation.currentLocation.observe(this) {

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