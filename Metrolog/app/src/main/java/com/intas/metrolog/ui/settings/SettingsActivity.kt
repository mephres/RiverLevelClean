package com.intas.metrolog.ui.settings

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.preference.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.intas.metrolog.BuildConfig
import com.intas.metrolog.R
import com.intas.metrolog.databinding.SettingsActivityBinding
import com.intas.metrolog.ui.login.LoginActivity
import com.intas.metrolog.util.*
import io.github.tonnyl.whatsnew.WhatsNew
import io.github.tonnyl.whatsnew.util.PresentationOption
import java.io.File
import java.net.URLConnection


class SettingsActivity : AppCompatActivity() {

    private val binding by lazy {
        SettingsActivityBinding.inflate(layoutInflater)
    }

    private var toastToShow: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.settings_activity_title)

        binding.settingsProgressIndicator.visibility = View.GONE
        binding.shadowView.isVisible = false
        // если экспорт файла журнала завершен
        Journal.onJournalExportComplete = {
            //openFile(it)
            toastToShow?.cancel()
            toastToShow = Toast.makeText(this, "Экспорт завершен", Toast.LENGTH_SHORT)
            toastToShow?.show()
            binding.settingsProgressIndicator.visibility = View.GONE
            binding.shadowView.isVisible = false
            // открываем диалог, чтобы отправить файл
            shareFile(it)
        }
        // если записи в БД отсутствуют и файл не создан
        Journal.onJournalExportFailure = {
            binding.settingsProgressIndicator.visibility = View.GONE
            binding.shadowView.isVisible = false
            toastToShow?.cancel()
            toastToShow = Toast.makeText(this, it, Toast.LENGTH_SHORT)
            toastToShow?.show()
        }
        // индикация процесса создания файла журнала
        Journal.onJournalExportProcess = {
            binding.settingsProgressIndicator.visibility = View.VISIBLE
            binding.shadowView.isVisible = true
            toastToShow?.cancel()
            toastToShow = Toast.makeText(this, it, Toast.LENGTH_SHORT)
            toastToShow?.show()
        }
        // если потребуется предоставить особые права доступа MANAGE_EXTERNAL_STORAGE
        Journal.onJournalExportError = {
            toastToShow?.cancel()
            toastToShow =
                Toast.makeText(this, it, Toast.LENGTH_SHORT)
            toastToShow?.show()
            binding.settingsProgressIndicator.visibility = View.GONE
            binding.shadowView.isVisible = false
            val permissionIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(permissionIntent)
        }
        // если данные из локальной базы успешно архивированы
        DatabaseUtil.onBackupComplete = {
            toastToShow?.cancel()
            toastToShow =
                Toast.makeText(this, "Копирование базы данных завершено", Toast.LENGTH_SHORT)
            toastToShow?.show()
            binding.settingsProgressIndicator.visibility = View.GONE
            binding.shadowView.isVisible = false
            shareFile(it)
        }
        // индикация процесса архивирования данных из локальной базы
        DatabaseUtil.onBackupProcess = {
            binding.settingsProgressIndicator.visibility = View.VISIBLE
            binding.shadowView.isVisible = true
            toastToShow?.cancel()
            toastToShow = Toast.makeText(this, it, Toast.LENGTH_SHORT)
            toastToShow?.show()
        }
        // если потребуется предоставить особые права доступа MANAGE_EXTERNAL_STORAGE
        DatabaseUtil.onBackupError = {
            toastToShow?.cancel()
            toastToShow =
                Toast.makeText(this, it, Toast.LENGTH_SHORT)
            toastToShow?.show()
            binding.settingsProgressIndicator.visibility = View.GONE
            binding.shadowView.isVisible = false
            val permissionIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(permissionIntent)
        }
    }

    private fun openFile(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)

        intent.setDataAndType(
            uri,
            URLConnection.guessContentTypeFromName(file.getName())
        )
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(intent)
    }

    private fun shareFile(file: File) {

        val intent = Intent(Intent.ACTION_SEND)
        val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)

        intent.setDataAndType(
            uri,
            URLConnection.guessContentTypeFromName(file.getName())
        )
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

        intent.putExtra(
            Intent.EXTRA_STREAM,
            uri
        )
        startActivity(Intent.createChooser(intent, "Переслать файл..."))
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var biometricPrompt: BiometricPrompt
        private lateinit var authMode: SwitchPreference
        private lateinit var serverIpAddress: EditTextPreference

        private var callActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_CANCELED) {

                    when (BiometricManager.from(requireContext()).canAuthenticate()) {
                        BiometricManager.BIOMETRIC_SUCCESS -> {
                            showBiometricPromptForDecryption()
                        }
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                            authMode.isChecked = AppPreferences.fingerPrintIsSave
                        }
                        else -> return@registerForActivityResult
                    }
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val button = findPreference<Preference>("change_user_button")
            val changeLogButton = findPreference<Preference>("change_log_button")
            val saveEventLog = findPreference<Preference>("save_event_log")
            val eventLogSwitch = findPreference<SwitchPreference>("event_log_switch")
            val backupDatabaseButton = findPreference<Preference>("backup_database_button")

            backupDatabaseButton?.setOnPreferenceClickListener {
                DatabaseUtil.backupDatabase(requireContext())
                true
            }

            eventLogSwitch?.setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    true -> {
                        Util.eventLoggingEnabled = true
                    }
                    false -> {
                        Util.eventLoggingEnabled = false
                    }
                }
                return@setOnPreferenceChangeListener true
            }

            saveEventLog?.setOnPreferenceClickListener {
                val dateRangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Выберите период")
                        .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                        .setSelection(
                            Pair(
                                MaterialDatePicker.todayInUtcMilliseconds().minus(86399999),
                                MaterialDatePicker.todayInUtcMilliseconds()
                            )
                        ).build()

                dateRangePicker.addOnPositiveButtonClickListener {
                    Journal.exportJournalFromDb(startTime = it.first, endTime = it.second)
                }
                dateRangePicker.show(
                    (requireContext() as AppCompatActivity).supportFragmentManager,
                    dateRangePicker.toString()
                )
                true
            }


            button?.setOnPreferenceClickListener {
                val dialog = MaterialAlertDialogBuilder(requireContext() as AppCompatActivity)
                dialog.setTitle(getString(R.string.settings_activity_change_user_title))
                    .setMessage("При выходе из профиля произойдет удаление сохраненных данных учетной записи и пин кода")
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_baseline_people_black_36dp)
                    .setPositiveButton("Выйти из профиля", { _, _ ->
                        AppPreferences.clear()
                        startActivity(Intent(context, LoginActivity::class.java))
                        getActivity()?.onBackPressed()
                    })
                    .setNegativeButton("Отмена") { _, _ ->
                    }
                    .show()
                true
            }

            changeLogButton?.setOnPreferenceClickListener {
                val stream = requireActivity().assets.open("change_log.xml")

                val changeLogList = ChangeLogXmlParser.parse(stream)
                if (!changeLogList.isNullOrEmpty()) {
                    val whatsNew = WhatsNew.newInstance(changeLogList)

                    whatsNew.presentationOption = PresentationOption.DEBUG
                    whatsNew.titleColor =
                        ContextCompat.getColor(requireActivity(), R.color.colorAccent)
                    whatsNew.titleText = "Что нового в ${BuildConfig.VERSION_NAME}?"
                    whatsNew.buttonText = "Понятно"
                    whatsNew.buttonBackground =
                        ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark)
                    whatsNew.buttonTextColor =
                        ContextCompat.getColor(requireActivity(), R.color.md_white)
                    whatsNew.itemTitleColor =
                        ContextCompat.getColor(requireActivity(), R.color.colorAccent)
                    whatsNew.itemContentColor = Color.parseColor("#808080")

                    whatsNew.presentAutomatically(requireActivity() as AppCompatActivity)
                } else {
                    val dialog = MaterialAlertDialogBuilder(requireContext() as AppCompatActivity)
                    dialog.setMessage("Версия приложения ${BuildConfig.VERSION_NAME}")
                        .setPositiveButton("Ok") { _, _ ->

                        }
                        .show()
                }
                true
            }

            val authCategory = findPreference<PreferenceCategory>("auth_tools_category")
            authMode = findPreference("auth_mode") ?: return

            when (BiometricManager.from(requireContext()).canAuthenticate()) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    authCategory?.isVisible = false
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    authCategory?.isVisible = AppPreferences.biometricSupport
                    authMode.isChecked = AppPreferences.fingerPrintIsSave
                }
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    authCategory?.isVisible = AppPreferences.biometricSupport
                    authMode.isChecked = AppPreferences.fingerPrintIsSave
                }
                else -> authCategory?.isVisible = false
            }

            authMode.setOnPreferenceChangeListener { _, newValue ->

                when (newValue) {
                    false -> {
                        AppPreferences.fingerPrintIsSave = false
                    }

                    true -> {
                        when (BiometricManager.from(requireContext()).canAuthenticate()) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                showBiometricPromptForDecryption()
                            }
                            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                                addFingerPrintInSettingsAlertDialog()
                            }
                            else -> return@setOnPreferenceChangeListener false
                        }
                    }
                }
                return@setOnPreferenceChangeListener true
            }

            BiometricPromptUtils.onAuthenticationSucceeded = {
                AppPreferences.fingerPrintIsSave = true
            }

            BiometricPromptUtils.onAuthenticationError = {
                authMode.isChecked = AppPreferences.fingerPrintIsSave
            }

            serverIpAddress = findPreference("server_ip_address") ?: return
            serverIpAddress.setOnPreferenceChangeListener { _, newValue ->
                Util.serverIpAddress = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        override fun onResume() {
            super.onResume()
            when (BiometricManager.from(requireContext()).canAuthenticate()) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    AppPreferences.fingerPrintIsSave = false
                    authMode.isChecked = false
                }
                else -> return
            }
        }

        // показать диалог сканера отпечатка пальца
        private fun showBiometricPromptForDecryption() {
            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(requireContext() as AppCompatActivity)
            val promptInfo =
                BiometricPromptUtils.createPromptInfo(requireContext() as AppCompatActivity)
            Handler(Looper.getMainLooper()).post {
                biometricPrompt.authenticate(promptInfo)
            }
        }

        // диалог для включения идентификации по биометрии
        private fun addFingerPrintInSettingsAlertDialog() {
            val dialog = MaterialAlertDialogBuilder(requireContext() as AppCompatActivity)
            dialog.setTitle("Вход по отпечатку пальца")
                .setMessage("Включите в настройках устройства")
                .setCancelable(false)
                .setIcon(R.drawable.ic_fingerprint_24)
                .setPositiveButton("Настроить", DialogInterface.OnClickListener { _, _ ->
                    callActivityResultLauncher.launch(Intent(Settings.ACTION_SECURITY_SETTINGS))
                })
                .setNegativeButton("Отмена") { _, _ ->
                    authMode.isChecked = AppPreferences.fingerPrintIsSave
                }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}