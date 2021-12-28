package com.intas.metrolog.util

import android.content.pm.PackageManager
import android.os.CancellationSignal
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.intas.metrolog.R

/**
 * Класс работы со сканером пальца
 */
object BiometricPromptUtils {
    private const val TAG = "BiometricPromptUtils"
    var onAuthenticationSucceeded: (() -> Unit)? = null
    var onAuthenticationFailed: (() -> Unit)? = null
    var onAuthenticationError: ((Int) -> Unit)? = null

    fun createBiometricPrompt(activity: AppCompatActivity): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                onAuthenticationError?.invoke(errCode)
                Log.d(TAG, "errCode is $errCode and errString is: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "User biometric rejected.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthenticationSucceeded?.invoke()
                Log.d(TAG, "Authentication was successful")
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Диалог со сканером
     */
    fun createPromptInfo(activity: AppCompatActivity): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.prompt_info_title))
            setDescription(activity.getString(R.string.prompt_info_description))
            setConfirmationRequired(false)
            setNegativeButtonText(activity.getString(R.string.pin_activity_dialog_negative_button))
        }.build()

    /**
     * обработка нажатия кнопки "Отмена"
     */
    fun getCancellationSignal(): CancellationSignal {
        val cancellationSignal = CancellationSignal()
        cancellationSignal.setOnCancelListener {

        }
        return cancellationSignal
    }

    /**
     * Проверка устройства на возможность авторизации по биометрии
     */
    fun checkBiometricSupport(activity: AppCompatActivity): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                android.Manifest.permission.USE_BIOMETRIC
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Fingerprint has not been enabled in settings.")
            return false
        }
        return if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            true
        } else true
    }
}