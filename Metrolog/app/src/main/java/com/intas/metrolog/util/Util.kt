package com.intas.metrolog.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import com.intas.metrolog.pojo.authuser.AuthUser
import java.util.*

object Util {
    var deviceUniqueIdArray = arrayOf("c9840a92e310f689", "06b0e0e504e68b85")

    var eventLoggingEnabled = false

    var userLocationQueue: LinkedList<Long> = LinkedList()
    var equipLocationQueue: LinkedList<Long> = LinkedList()
    var equipRfidQueue: LinkedList<Long> = LinkedList()
    var eventQueue: LinkedList<Long> = LinkedList()
    var eventOperationQueue: LinkedList<Long> = LinkedList()
    var eventOperationControlQueue: LinkedList<Int> = LinkedList()
    var eventPhotoQueue: LinkedList<Long> = LinkedList()
    var requestQueue: LinkedList<Long> = LinkedList()
    var equipInfoQueue: LinkedList<Int> = LinkedList()
    var requestPhoto: LinkedList<Long> = LinkedList()
    var chatMessageQueue: LinkedList<Int> = LinkedList()

    val appPermissionArray = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.MANAGE_DOCUMENTS
    )
    var YYYYMMDD_HHMMSS = "yyyyMMdd_HHmmss"

    /**
     * Количество попыток ввода пин кода
     */
    const val PIN_COUNT_OF_ATTEMPTS = 3

    /**
     * Количество цифр в пинкоде
     */
    const val PIN_COUNT_OF_DIGITS = 4

    val ALARM_REQUEST_CODE = 999

    val GALLERY_REQUEST = 3000
    val CAMERA_CAPTURE = 3001
    val DOCUMENT_REQUEST = 3002
    val PERMISSION_REQUEST_CODE = 3673

    val NOTIFICATION_CHANNEL_ID = "101"
    val NOTIFICATION_CHANNEL_NAME = "Канал уведомлений Мобильный Оператор"
    val NOTIFICATION_CHANNEL_DESCRIPTION = "Уведомления о системных событиях"

    val BOTTOM_DIALOG_SHEET_FRAGMENT_TAG = "BOTTOM_DIALOG_SHEET_FRAGMENT_TAG"

    var authUser: AuthUser? = null
        get() = AppPreferences.authUser

    lateinit var serverIpAddress: String

    var unreadMessageCount = 0
    var unreadSystemEventCount = 0

    private val hexArray = "0123456789ABCDEF".toCharArray()

    fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
        p1: T1?,
        p2: T2?,
        p3: T3?,
        block: (T1, T2, T3) -> R?
    ): R? {
        return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
        p1: T1?,
        p2: T2?,
        p3: T3?,
        p4: T4?,
        block: (T1, T2, T3, T4) -> R?
    ): R? {
        return if (p1 != null && p2 != null && p3 != null && p4 != null) block(
            p1,
            p2,
            p3,
            p4
        ) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(
        p1: T1?,
        p2: T2?,
        p3: T3?,
        p4: T4?,
        p5: T5?,
        block: (T1, T2, T3, T4, T5) -> R?
    ): R? {
        return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(
            p1,
            p2,
            p3,
            p4,
            p5
        ) else null
    }

    /**
     * Получение id ресурса по наименованию
     */
    fun getResId(resName: String, c: Class<*>): Int {
        return try {
            val idField = c.getDeclaredField(resName)
            idField.getInt(idField)
        } catch (e: Exception) {
            -1
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    @SuppressLint("HardwareIds")
    fun getDeviceUniqueID(activity: Activity): String {
        return Settings.Secure.getString(
            activity.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}