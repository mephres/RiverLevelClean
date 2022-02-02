package com.intas.metrolog.ui.login.pin

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.intas.metrolog.BuildConfig
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityPinCodeBinding
import com.intas.metrolog.ui.bottom_dialog.BottomDialogSheet
import com.intas.metrolog.ui.login.LoginActivity
import com.intas.metrolog.ui.main.MainActivity
import com.intas.metrolog.util.*
import com.intas.metrolog.util.Util.BOTTOM_DIALOG_SHEET_FRAGMENT_TAG
import com.intas.metrolog.util.Util.PIN_COUNT_OF_ATTEMPTS
import com.intas.metrolog.util.Util.PIN_COUNT_OF_DIGITS
import io.github.tonnyl.whatsnew.WhatsNew
import io.github.tonnyl.whatsnew.util.PresentationOption

class PinCodeActivity : AppCompatActivity() {

    private lateinit var viewModel: PinCodeViewModel

    // введенный ПИН для первого раза
    private var inputPin1: String? = null

    // введенный ПИН для второго раза
    private var inputPin2: String? = null
    private var doubleBackToExitPressedOnce = false

    // Флаг,
    // true - первый раз (вводим первоначальный пин)
    // false - уже ввели первый раз (авторизуемся)
    private var firstTimePin = true

    //количество попыток ввода
    private var countAttempt: Int = PIN_COUNT_OF_ATTEMPTS

    private var digitCollectionList = mutableListOf<String>()
    private lateinit var circleList: List<ImageView>

    private val enterTransition = MaterialFadeThrough()
    private val exitTransition = MaterialFadeThrough()

    private val binding by lazy {
        ActivityPinCodeBinding.inflate(layoutInflater)
    }

    // api биометрии
    private lateinit var biometricPrompt: BiometricPrompt

    // лаунчер для перехода в настройки биометрии
    private var callActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                when (BiometricManager.from(this).canAuthenticate()) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        showBiometricPromptForDecryption()
                        BiometricPromptUtils.onAuthenticationSucceeded = {
                            AppPreferences.fingerPrintIsSave = true
                            AppPreferences.biometricSupport = true
                            userInfoUpdate()
                            viewModel.authUser()
                            Journal.insertJournal("PinCodeActivity->callActivityResultLauncher", BiometricManager.BIOMETRIC_SUCCESS )
                        }
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        AppPreferences.fingerPrintIsSave = false
                        AppPreferences.biometricSupport = true
                        userInfoUpdate()
                        viewModel.authUser()
                        Journal.insertJournal("PinCodeActivity->callActivityResultLauncher", BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED )
                    }
                    else -> return@registerForActivityResult
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Синглтон для работы c SharedPreferences для сохранения логина и пароля пользователя
        //Инициализация SharedPreferences
        AppPreferences.init(this)

        binding.infoTextView.setText(getString(R.string.pin_activity_input_pin_code))

        //ViewModel
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[PinCodeViewModel::class.java]

        checkAppVersion()

        setUi()

        checkBiometricDataIsSave()

        createOnClickListener()

        viewModel.onSuccess = {
            binding.pinProgressIndicator.visibility = View.GONE
            showToast("Вы вошли как ${it.fio}")

            for (i in 0..circleList.lastIndex) {
                circleList[i].setImageResource(R.drawable.circle_orange_filled)
            }

            this.finish()
            startActivity(Intent(this@PinCodeActivity, MainActivity::class.java))
        }

        viewModel.onError = {
            setUi()
            showToast(it)
        }

        viewModel.onFailure = {
            setUi()
            showToast(it)
        }

        // callback сканера отпечатков
        BiometricPromptUtils.onAuthenticationSucceeded = {
            AppPreferences.fingerPrintIsSave = true
            AppPreferences.biometricSupport = true
            userInfoUpdate()
            viewModel.authUser()
        }
    }

    /**
     * Проверка версии приложения
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
     * Отображение журнала внесенных изменений, если версия приложения обновилась
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
            whatsNew.buttonTextColor = ContextCompat.getColor(this, R.color.md_white_1000)
            whatsNew.itemTitleColor = ContextCompat.getColor(this, R.color.colorAccent)
            whatsNew.itemContentColor = Color.parseColor("#808080")

            whatsNew.presentAutomatically(this@PinCodeActivity)
        }
    }

    /**
     * Проверка при запуске активити на возможность входа по биометрии
     */
    private fun checkBiometricDataIsSave() {
        // если сохранен признак входа в приложение по отпечатку пальца
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (AppPreferences.fingerPrintIsSave && canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.fingerPrintImageView.visibility = View.VISIBLE
            showBiometricPromptForDecryption()
        } else {
            AppPreferences.fingerPrintIsSave = false
        }
    }

    /**
     * Диалог для добавления возможности авторизации по биометрии
     */
    private fun addFingerPrintAlertDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("Вход по отпечатку пальца")
            .setMessage("Добавьте, чтобы не вводить код")
            .setCancelable(false)
            .setIcon(R.drawable.ic_fingerprint_24)
            .setPositiveButton("Добавить", DialogInterface.OnClickListener { _, _ ->
                showBiometricPromptForDecryption()
            })
            .setNegativeButton("Отмена") { _, _ ->
                AppPreferences.fingerPrintIsSave = false
                AppPreferences.biometricSupport = true
                viewModel.authUser()
            }
            .show()
    }

    /**
     * Диалог для перехода в настройки биометрии
     */
    private fun addFingerPrintInSettingsAlertDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("Вход по отпечатку пальца")
            .setMessage("Включите в настройках устройства")
            .setCancelable(false)
            .setIcon(R.drawable.ic_fingerprint_24)
            .setPositiveButton("Настроить", DialogInterface.OnClickListener { _, _ ->
                callActivityResultLauncher.launch(Intent(Settings.ACTION_SECURITY_SETTINGS))
            })
            .setNegativeButton("Отмена") { _, _ ->
                AppPreferences.fingerPrintIsSave = false
                AppPreferences.biometricSupport = true
                viewModel.authUser()
            }
            .show()
    }

    /**
     * Диалог сканера отпечатков
     */
    private fun showBiometricPromptForDecryption() {
        biometricPrompt = BiometricPromptUtils.createBiometricPrompt(this)
        val promptInfo = BiometricPromptUtils.createPromptInfo(this)
        biometricPrompt.authenticate(promptInfo)
    }

    private fun setUi() {

        TransitionManager.beginDelayedTransition(binding.root, enterTransition)

        digitCollectionList.clear()

        with(binding) {

            pinProgressIndicator.visibility = View.GONE

            circle1ImageView.setImageResource(R.drawable.circle_inactive_gray)
            circle2ImageView.setImageResource(R.drawable.circle_inactive_gray)
            circle3ImageView.setImageResource(R.drawable.circle_inactive_gray)
            circle4ImageView.setImageResource(R.drawable.circle_inactive_gray)

            circleList = listOf(
                circle1ImageView,
                circle2ImageView,
                circle3ImageView,
                circle4ImageView
            )

            appVersionTextView.setText(
                String.format(
                    getString(R.string.app_version_title),
                    BuildConfig.VERSION_NAME
                )
            )

            val userName = AppPreferences.authUser?.name ?: getString(R.string.no_data)
            pinHelloTextView.text = "Здравствуйте, $userName"
        }


        val pinCode = AppPreferences.savedPinCode

        firstTimePin = true
        binding.infoTextView.setText(getString(R.string.pin_activity_input_pin_code))

        pinCode?.let {
            if (it.toInt() > 0) {
                firstTimePin = false
            }
        }
    }

    /**
     * Обработка введенных цифр
     */
    private fun collectDigits(digit: String) {

        if (digit.equals("fingerprint", ignoreCase = true)) {
            showBiometricPromptForDecryption()
            return
        }

        if (digit.equals("resetPIN", ignoreCase = true)) {
            resetPIN()
            return
        }

        // Стираем последнюю введенную цифру
        if (digit.equals("-1", ignoreCase = true)) {

            if (digitCollectionList.size > 0) {

                circleList[digitCollectionList.lastIndex].setImageResource(R.drawable.circle_inactive_gray)
                digitCollectionList.removeLast()
            }

            return
        }

        digitCollectionList.add(digit)

        pinCircleInvalidate()

        if (digitCollectionList.size < PIN_COUNT_OF_DIGITS) {
            return
        }
        //собрали цифры
        var inputPinCode = ""
        digitCollectionList.forEach({
            inputPinCode = inputPinCode + it
        })

        //если первый раз
        if (firstTimePin) {
            if (inputPin1 == null) {
                // ввели первый раз
                inputPin1 = inputPinCode
                // отправляемся на второй круг
                setUi()
                binding.infoTextView.setText(getString(R.string.pin_activity_input_pin_code_again))
                return
            } else if (inputPin1 != null && inputPin2 == null) {
                // ввели второй раз
                // сравниваем
                inputPin2 = inputPinCode

                if (inputPin1.equals(inputPin2, ignoreCase = true)) {

                    //если первый раз, и пины совпадают, то записывем пин код в хранилище
                    AppPreferences.savedPinCode = inputPinCode
                    AppPreferences.pinCodeIsSave = true

                } else {

                    // если не совпадают, тогда сбрасываем пины и начинаем заново
                    inputPin1 = null
                    inputPin2 = null
                    firstTimePin = true

                    setUi()

                    showToast(getString(R.string.pin_activity_pins_not_valid))

                    return
                }
            }
        }

        // записываем данные о пользователе User в хранилище
        userInfoUpdate()
        val pinCodeString = AppPreferences.savedPinCode

        // если введенный ПИН совпадает с ПИН, который хранится в хранилище,
        // то запускаем MainActivity
        if (inputPinCode.equals(pinCodeString, ignoreCase = true)) {

            binding.pinProgressIndicator.visibility = View.VISIBLE

            // проверка поддержки сканирования отпечатков
            val canAuthenticate =
                BiometricManager.from(applicationContext).canAuthenticate()

            //если в приложение не добавлена возможность авторизации по биометрии, имеется аппаратная возможность и сохранен отпечаток в телефоне
            if (!AppPreferences.biometricSupport && canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                addFingerPrintAlertDialog()

                //если в приложение не добавлена возможность авторизации по биометрии, имеется аппаратная возможность и НЕ сохранен отпечаток в телефоне
            } else if (!AppPreferences.biometricSupport && canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                addFingerPrintInSettingsAlertDialog()
            } else {
                viewModel.authUser()
            }


        } else {
            // если не совпадает, уменьшаем кол-во попыток
            countAttempt--

            if (countAttempt > 0) {

                when (countAttempt) {
                    1 -> showToast(getString(R.string.pin_activity_pin_not_valid_last_try))
                    2, 3, 4 -> showToast(
                        String.format(
                            applicationContext.getString(R.string.pin_activity_pin_not_valid_attempts_left),
                            countAttempt
                        )
                    )
                }
                setUi()
            } else {
                // если кол-во попыток превысило допустимое, то сбрасываем информацию о
                // пользователе и открываем загрузчик.
                eraseInfoUser()

                showToast(getString(R.string.pin_activity_pin_reset_log_in_again))

                startActivity(Intent(this@PinCodeActivity, LoginActivity::class.java))
                finish()
            }
        }
    }


    // записываем в хранилище информацию из класса User
    // это сделано для последующего восстановления этого пользователя в LoadActivity, т.к.
    // в LoadActivity нет данных о пользователях
    private fun userInfoUpdate() {

        Util.authUser = AppPreferences.authUser
    }

    // перекрашиваем круги взависимости от количества введенных цифр
    private fun pinCircleInvalidate() {

        if (digitCollectionList.size > PIN_COUNT_OF_DIGITS) {
            return
        }

        try {
            for (i in 0 until digitCollectionList.size) {
                circleList[i].setImageResource(R.drawable.circle_orange_filled)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createOnClickListener() {

        val listener = View.OnClickListener { view ->

            TransitionManager.beginDelayedTransition(binding.root, enterTransition)

            ViewUtil.runAnimationButton(applicationContext, view)
            collectDigits(view.tag.toString())
        }

        with(binding) {

            digit0ImageView.setOnClickListener(listener)
            digit1ImageView.setOnClickListener(listener)
            digit2ImageView.setOnClickListener(listener)
            digit3ImageView.setOnClickListener(listener)
            digit4ImageView.setOnClickListener(listener)
            digit5ImageView.setOnClickListener(listener)
            digit6ImageView.setOnClickListener(listener)
            digit7ImageView.setOnClickListener(listener)
            digit8ImageView.setOnClickListener(listener)
            digit9ImageView.setOnClickListener(listener)
            resetPINTextView.setOnClickListener(listener)
            arrowLeftImageView.setOnClickListener(listener)
            fingerPrintImageView.setOnClickListener(listener)
        }
    }

    /**
     * сбрасываем пин и открываем загрузчик
     */
    private fun resetPIN() {

        val dialogSheet = BottomDialogSheet.newInstance(
            getString(R.string.pin_activity_dialog_title),
            getString(R.string.pin_activity_dialog_text),
            getString(R.string.pin_activity_dialog_positive_button),
            getString(R.string.pin_activity_dialog_negative_button)
        )
        dialogSheet.show(supportFragmentManager, BOTTOM_DIALOG_SHEET_FRAGMENT_TAG)
        dialogSheet.onPositiveClickListener = {
            eraseInfoUser()
            val intent = Intent(this@PinCodeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
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

    // удаляем всю информацию о пользователе из храилища
    private fun eraseInfoUser() {

        AppPreferences.pinCodeIsSave = false
        AppPreferences.savedPinCode = null
        AppPreferences.biometricSupport = false
        AppPreferences.fingerPrintIsSave = false
        AppPreferences.authUser = null
    }

    private fun showToast(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}