package com.intas.metrolog.ui.login

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.intas.metrolog.BuildConfig
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityLoginBinding
import com.intas.metrolog.ui.login.pin.PinCodeActivity
import com.intas.metrolog.ui.main.MainActivity
import com.intas.metrolog.ui.settings.SettingsActivity
import com.intas.metrolog.util.AppPreferences
import com.intas.metrolog.util.ChangeLogXmlParser
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import com.intas.metrolog.util.Util.PERMISSION_REQUEST_CODE
import io.github.tonnyl.whatsnew.WhatsNew
import io.github.tonnyl.whatsnew.util.PresentationOption
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Util.serverIpAddress = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(
                    "server_ip_address",
                    getString(R.string.settings_activity_ip_address_default_value)
                ).toString()

        //Если приложение запущено не на планшете
        //Блокируется landscape-режим для текущей активити
        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        //Data Binding
        val binding: ActivityLoginBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_login)

        //ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[LoginViewModel::class.java]

        //Установка ViewModel для Data Binding
        binding.loginViewModel = viewModel

        //Синглтон для работы c SharedPreferences для сохранения логина и пароля пользователя
        //Инициализация SharedPreferences
        AppPreferences.init(this)
        Journal.init(this)

        // переключатель "Запомнить меня" - по умолчанию true
        viewModel.rememberCheckBoxValue = ObservableBoolean(true)

        // Проверка входа по пин-коду
        // Если пин-код сохранен, то вызывается активность входа по пин
        if (AppPreferences.pinCodeIsSave) {
            val intent = Intent(this, PinCodeActivity::class.java)
            startActivity(intent)
            this.finish()
        } else {
            //показ изменений/исправлений - после обновления версии приложения и если вход производится без пин-кода
            checkAppVersion()
        }

        //Присваиваем полям ввода логина и пароля значения, если сохранены в SharedPreferences
        if (AppPreferences.saveLoginPass) {
            viewModel.login = AppPreferences.login
            viewModel.password = AppPreferences.password
            viewModel.rememberCheckBoxValue = ObservableBoolean(true)
        }

        //TODO: УБРАТЬ!!!!
       /*Util.authUser = AuthUser(
            userId = 44632,
            fio = "Достоевский Ф.М.",
            surname = "Достоевский",
            name = "Федор",
            middleName = "Михайлович",
            position = "Оператор 1 категории",
            avatarUrl = "https://cdn2.thecatapi.com/images/2j0.jpg",
            role = 1
        )
        startActivity(Intent(this, MainActivity::class.java))
        finish()*/

        //Получаем ответ, в случае успешной аутентификации на сервере
        viewModel.onSuccess = {

            //При первой авторизации, сохраняем логин и пароль,
            //если пользователь выбрал галочку "Запомнить меня"
            if (viewModel.rememberCheckBoxValue?.get() == true) {
                AppPreferences.login = viewModel.login
                AppPreferences.password = viewModel.password
                AppPreferences.saveLoginPass = true
            } else {
                AppPreferences.clear()
            }

            // если предыдущий сохраненный пользователь не равен авторизованному,
            // тогда удаляем данные
            if (AppPreferences.authUser?.userId != it.userId) {
                viewModel.deleteAllData()
            }
            AppPreferences.authUser = it

            // если пин кода нет, то открываем активити пин кода
            if (!AppPreferences.pinCodeIsSave && viewModel.rememberCheckBoxValue?.get() == true) {
                startActivity(Intent(this, PinCodeActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            this.finish()
        }

        //В случае отсутствия сети
        viewModel.onError = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        //В случае проблем с авторизацией
        viewModel.onFailure = {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.appVersion =
            String.format(getString(R.string.app_version_title), BuildConfig.VERSION_NAME)

        binding.settingsButton.setOnClickListener {
            Journal.insertJournal("LoginActivity->settingsButton", journalText = "Переход в настройки")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        checkAndRequestPermissions()
        hideAppVersionWhenKeyboardIsShown()
    }

    override fun onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            finishAndRemoveTask()
            return
        }
        this.doubleBackToExitPressedOnce = true
        showToast(getString(R.string.exit_message))

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                doubleBackToExitPressedOnce = false
            }
        }, 2000)
    }

    /**
     * Отображение журнала внесенных изменений, если версия приложения обновилась
     */
    private fun checkAppVersion() {
        val savedVersionNumber = AppPreferences.versionNumber
        var currentVersionNumber = 0

        currentVersionNumber = try {
            this.packageManager.getPackageInfo(this.packageName, 0).versionCode
        } catch (e: Exception) {
            0
        }
        if (currentVersionNumber > savedVersionNumber) {
            initWhatsNewLog()
            AppPreferences.versionNumber = currentVersionNumber
        }

    }

    /**
     * Проверка при запуске активити на возможность входа по биометрии
     */
    private fun initWhatsNewLog() {
        val stream = assets.open("change_log.xml")

        val changeLogList = ChangeLogXmlParser.parse(stream)
        if (!changeLogList.isNullOrEmpty()) {
            val whatsNew = WhatsNew.newInstance(changeLogList)

            whatsNew.presentationOption = PresentationOption.DEBUG
            whatsNew.titleColor = ContextCompat.getColor(this, R.color.colorAccent)
            whatsNew.titleText = "Что нового в ${BuildConfig.VERSION_NAME}?"
            whatsNew.buttonText = "Понятно"
            whatsNew.buttonBackground = ContextCompat.getColor(this, R.color.colorPrimaryDark)
            whatsNew.buttonTextColor = ContextCompat.getColor(this, R.color.md_white)
            whatsNew.itemTitleColor = ContextCompat.getColor(this, R.color.colorAccent)
            whatsNew.itemContentColor = Color.parseColor("#808080")

            whatsNew.presentAutomatically(this@LoginActivity)
        }
    }

    /**
     * Скрытие appVersionTextView, при открытой клавиатуре
     * Вычисление разницы в размерах между root view активности и размером окна
     */
    private fun hideAppVersionWhenKeyboardIsShown() {
        val activityRootView: ConstraintLayout = findViewById(R.id.loginConstraintLayout)
        val appVersionTextView: TextView = findViewById(R.id.appVersionTextView)
        val metrics: DisplayMetrics = resources.displayMetrics
        val dpToPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, metrics)

        activityRootView.viewTreeObserver
            .addOnGlobalLayoutListener {
                val heightDiff: Int =
                    activityRootView.rootView.height - activityRootView.height
                if (heightDiff > dpToPx) {
                    appVersionTextView.isVisible = false
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        appVersionTextView.isVisible = true
                    }, 100)
                }
            }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionResults = HashMap<String, Int>()
            var deniedCount = 0
            for (i in grantResults.indices) {
                if (grantResults[i] == PERMISSION_REQUEST_CODE) {
                    permissionResults[permissions[i]] = grantResults[i]
                    deniedCount++
                }
            }
            if (deniedCount != 0) {
                for ((permissionName, permissionResult) in permissionResults) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)) {
                        showDialog(
                            "",
                            "Для корректной работы данного приложения необходимо разрешение на использование определения местоположения, " +
                                    "использование камеры устройства, а также работа с внутренней памятью устройства",
                            "Да, разрешить " +
                                    "использование",
                            DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                                Journal.insertJournal("LoginActivity->onRequestPermissionsResult->PositiveButton", journalText = "Разрешения получены")
                                dialogInterface.dismiss()
                                checkAndRequestPermissions()
                            },
                            "Нет, выйти из приложения",
                            DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                                Journal.insertJournal("LoginActivity->onRequestPermissionsResult->NegativeButton", journalText = "Выход из приложения")
                                dialogInterface.dismiss()
                                finish()
                            },
                            false
                        )
                    } else {
                        showDialog(
                            "",
                            "Несколько разрешений не получено. Необходимо зайти в настройки",
                            "Настройки",
                            DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                                dialogInterface.dismiss()
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts(
                                        "package", packageName,
                                        null
                                    )
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            },
                            "Выйти из приложения",
                            DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                                dialogInterface.dismiss()
                                finish()
                            },
                            false
                        )
                        break
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showDialog(
        title: String,
        message: String,
        positiveLabel: String,
        positiveOnClick: DialogInterface.OnClickListener,
        negativeLabel: String,
        negativeOnClick: DialogInterface.OnClickListener,
        isCancelable: Boolean
    ): AlertDialog? {

        val builder = AlertDialog.Builder(this)

        with(builder) {

            setTitle(title)
            setCancelable(isCancelable)
            setMessage(message)
            setPositiveButton(positiveLabel, positiveOnClick)
            setNegativeButton(negativeLabel, negativeOnClick)
        }

        val alertDialog = builder.create()
        alertDialog.show()

        return alertDialog
    }

    /**
     * Проверка разрешений для приложения
     */
    fun checkAndRequestPermissions(): Boolean {

        val needPermissionList = ArrayList<String>()

        for (permission in Util.appPermissionArray) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                needPermissionList.add(permission)
            }
        }
        if (!needPermissionList.isEmpty()) {
            val permStringArray = arrayOfNulls<String>(needPermissionList.size)
            ActivityCompat.requestPermissions(
                this,
                needPermissionList.toArray(permStringArray),
                PERMISSION_REQUEST_CODE
            )
            Journal.insertJournal("LoginActivity->checkAndRequestPermissions", journalText = false)
            return false
        }
        Journal.insertJournal("LoginActivity->checkAndRequestPermissions", journalText = true)
        return true
    }
}