package com.intas.metrolog.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intas.metrolog.pojo.authuser.AuthUser
import java.lang.reflect.Type

/**
 * Синглтон для работы с SharedPreferences
 */
object AppPreferences {

    private lateinit var preferences: SharedPreferences
    private const val MODE = Context.MODE_PRIVATE
    private const val FILE_NAME = "preferences"
    private const val PREF_LOGIN = "login"
    private const val PREF_PASS = "pass"
    private const val PREF_SAVE_LOGIN_PASS = "saveLoginPass"
    private const val PREF_PIN_IS_SAVE = "pinIsSave"
    private const val PREF_SAVED_PIN = "savedPin"
    private const val PREF_AUTH_USER = "authUser"
    private const val PREF_USER_LOGIN_DATETIME = "userLoginDateTime"
    private const val PREF_BIOMETRIC_SUPPORT = "biometricSupport"
    private const val PREF_SAVE_FINGERPRINT = "saveFingerprint"

    private const val TASK_FILTER_FIELD_LIST = "taskFilterFieldList"
    private const val TASK_FILTER_STATUS_LIST = "taskFilterStatusList"
    private const val OBJECT_FILTER_STATUS_LIST = "objectFilterStatusList"

    private const val VERSION_KEY = "versionKey"
    private const val LAST_DATE_JOB = "lastDateJob"


    fun init(context: Context) {
        preferences = context.getSharedPreferences(FILE_NAME, MODE)
    }

    /*
     * Функция расширения [SharedPreferences], нет необходимости вызывать edit() и apply() для каждой операции
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    fun clear() {
        login = null
        password = null
        saveLoginPass = false
        userLoginDateTime = DateTimeUtil.getUnixDateTimeNow()
        pinCodeIsSave = false
        savedPinCode = null
        biometricSupport = false
        fingerPrintIsSave = false
    }

    /**
     * Дата-время последнего запуска процесса
     */
    var lastDateJob: Long
        get() = preferences.getLong(LAST_DATE_JOB, 0)
        set(value) = preferences.edit { it.putLong(LAST_DATE_JOB, value) }

    /**
     * Номер версии приложения
     */
    var versionNumber: Int
        get() = preferences.getInt(VERSION_KEY, 0)
        set(value) = preferences.edit { it.putInt(VERSION_KEY, value) }

    /**
     * Имя пользователя для авторизации
     */
    var login: String?
        get() = preferences.getString(PREF_LOGIN, "")
        set(value) = preferences.edit { it.putString(PREF_LOGIN, value) }

    /**
     * Пароль пользователя
     */
    var password: String?
        get() = preferences.getString(PREF_PASS, "")
        set(value) = preferences.edit { it.putString(PREF_PASS, value) }

    /**
     * Признак сохранения имени пользователя и пароля
     */
    var saveLoginPass: Boolean
        get() = preferences.getBoolean(PREF_SAVE_LOGIN_PASS, false)
        set(value) = preferences.edit { it.putBoolean(PREF_SAVE_LOGIN_PASS, value) }

    /**
     * Признак сохранения пин-кода
     */
    var pinCodeIsSave: Boolean
        get() = preferences.getBoolean(PREF_PIN_IS_SAVE, false)
        set(value) = preferences.edit { it.putBoolean(PREF_PIN_IS_SAVE, value) }

    /**
     * Значение сохраненного пин-кода
     */
    var savedPinCode: String?
        get() = preferences.getString(PREF_SAVED_PIN, null)
        set(value) = preferences.edit { it.putString(PREF_SAVED_PIN, value) }

    /**
     * Дата-время последней успешной авторизации пользователя
     */
    var userLoginDateTime: Long
        get() = preferences.getLong(PREF_USER_LOGIN_DATETIME, DateTimeUtil.getUnixDateTimeNow())
        set(value) = preferences.edit { it.putLong(PREF_USER_LOGIN_DATETIME, value) }

    /**
     * Признак сохранения отпечатка пальца пользователя
     */
    var fingerPrintIsSave: Boolean
        get() = preferences.getBoolean(PREF_SAVE_FINGERPRINT, false)
        set(value) = preferences.edit { it.putBoolean(PREF_SAVE_FINGERPRINT, value) }

    /**
     * Признак поддержки работы с дактилоскопическим датчиком
     */
    var biometricSupport: Boolean
        get() = preferences.getBoolean(PREF_BIOMETRIC_SUPPORT, false)
        set(value) = preferences.edit { it.putBoolean(PREF_BIOMETRIC_SUPPORT, value) }

    /**
     * Авторизованный пользователь
     */
    var authUser: AuthUser?
        get() {
            val gson = Gson()
            val json: String? = preferences.getString(PREF_AUTH_USER, "")
            val type: Type = object : TypeToken<AuthUser?>() {}.getType()
            return gson.fromJson(json, type)
        }
        set(value) {
            val user: AuthUser? = value
            val userText = Gson().toJson(user)
            preferences.edit {
                it.putString(PREF_AUTH_USER, userText)
            }
        }

    /**
     * Фильтр для отображения задач по выбранным месторождениям
     */
    var taskFilterFieldList: ArrayList<Int>
        get() {
            val gson = Gson()
            val json: String? = preferences.getString(TASK_FILTER_FIELD_LIST, "")
            val type: Type = object : TypeToken<java.util.ArrayList<Int?>?>() {}.getType()
            return gson.fromJson(json, type)
        }
        set(value) {
            val textList: List<Int> = ArrayList(value)
            val jsonText = Gson().toJson(textList)
            preferences.edit {
                it.putString(TASK_FILTER_FIELD_LIST, jsonText)
            }
        }

    /**
     * Фильтр для отображения задач по выбранным статусам
     */
    var taskFilterStatusList: ArrayList<Int>
        get() {
            val gson = Gson()
            val json: String? = preferences.getString(TASK_FILTER_STATUS_LIST, "")
            val type: Type = object : TypeToken<java.util.ArrayList<Int?>?>() {}.getType()
            return gson.fromJson(json, type)
        }
        set(value) {
            val textList: List<Int> = ArrayList(value)
            val jsonText = Gson().toJson(textList)
            preferences.edit {
                it.putString(TASK_FILTER_STATUS_LIST, jsonText)
            }
        }

    /**
     * Фильтр для отображения объектов по выбранным статусам
     */
    var objectFilterStatusList: ArrayList<Int>
        get() {
            val gson = Gson()
            val json: String? = preferences.getString(OBJECT_FILTER_STATUS_LIST, "")
            val type: Type = object : TypeToken<java.util.ArrayList<Int?>?>() {}.getType()
            return gson.fromJson(json, type)
        }
        set(value) {
            val textList: List<Int> = ArrayList(value)
            val jsonText = Gson().toJson(textList)
            preferences.edit {
                it.putString(OBJECT_FILTER_STATUS_LIST, jsonText)
            }
        }
}