package com.intas.metrolog.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.ByteArrayOutputStream

object ImageUtil {

    /**
     * Получение растрового изображения по гкш файла
     * @param uri uri файла
     * @return растровое изображение, объект класса [Bitmap]
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            return image
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return null
    }

    /**
     * Получение кодированной картинки в виде строки
     * @return кодированная картинка
     */
    @Throws(Exception::class)
    fun Bitmap.getScreen(context: Context): String {
        synchronized(ImageUtil) {

            val beginTime = DateTimeUtil.getUnixDateTimeNow()

            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val imageSize = preferences.getString("image_size", "2")?.toInt() ?: 1
            val formatType = preferences.getString("format_type", "JPEG")
            val imageQuality = preferences.getInt("image_quality", 100)

            // Вычисляем ширину и высоту изображения
            val bitmapWidth = width
            val bitmapHeight = height

            // Половинки
            val halfWidth: Int = bitmapWidth / imageSize
            val halfHeight: Int = bitmapHeight / imageSize
            val bitmapHalf = Bitmap.createScaledBitmap(
                this, halfWidth,
                halfHeight, false
            )
            val byteArrayOutputStream = ByteArrayOutputStream()
            var imageFormat: CompressFormat? = null
            var prefixString = ""
            when (formatType) {
                "JPEG" -> {
                    imageFormat = CompressFormat.JPEG
                    prefixString = "data:image/jpeg;base64,"
                }
                "PNG" -> {
                    imageFormat = CompressFormat.PNG
                    prefixString = "data:image/png;base64,"
                }
                "WEBP" -> {
                    imageFormat = CompressFormat.WEBP
                    prefixString = "data:image/webp;base64,"
                }
                else -> {}
            }
            bitmapHalf.compress(
                imageFormat,
                imageQuality,
                byteArrayOutputStream
            )

            // Получаем изображение из потока в виде байтов
            val bytes = byteArrayOutputStream.toByteArray()

            // Кодируем байты в строку Base64 и возвращаем
            val screen = prefixString + Base64.encodeToString(bytes, Base64.DEFAULT)
            val endTime = DateTimeUtil.getUnixDateTimeNow()
            val diff = endTime - beginTime
            Log.d("BITMAP_GET_SCREEN", "decodeTime: $diff" )
            return screen
        }
    }

    /**
     * Получение кодированной картинки в виде строки
     * @param uriString строка, содержащая uri изображения
     * @return кодированная картинка
     */
    fun getBase64ScreenFromUri(context: Context, uriString: String?): String? {
        try {
            val beginTime = DateTimeUtil.getUnixDateTimeNow()
            val uri = Uri.parse(uriString)
            val bitmap = getBitmapFromUri(context, uri)
            var screen: String? = null
            bitmap?.let {
                screen = bitmap.getScreen(context)
            }
            val endTime = DateTimeUtil.getUnixDateTimeNow()
            val diff = endTime - beginTime
            Log.d("GET_BASE64_SCREEN", "decodeTime: $diff" )
            return screen
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return null
    }
}