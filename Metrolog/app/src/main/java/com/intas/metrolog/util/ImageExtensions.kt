package com.intas.metrolog.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.ByteArrayOutputStream


/**
 * Получение кодированной картинки в виде строки
 * @return кодированная картинка
 */
fun getEncodedScreen(uri: Uri, context: Context): String {

    return try {
        val bitmap = uri.getBitmap(context)
        val screen: String = bitmap?.getBase64() ?: ""
        screen
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        ""
    }
}

/**
 * Получение кодированной картинки в виде строки
 *
 * @param bitmap растровое изображение, объект класса [Bitmap]
 * @return кодированная картинка
 */
@Throws(Exception::class)
fun Bitmap.getBase64(): String {

    synchronized(this) {

        // Вычисляем ширину и высоту изображения
        val bitmapWidth = this.width
        val bitmapHeight = this.height

        // Половинки
        val halfWidth: Int = bitmapWidth / 1
        val halfHeight: Int = bitmapHeight / 1
        val bitmapHalf = Bitmap.createScaledBitmap(
            this, halfWidth,
            halfHeight, false
        )
        val byteArrayOutputStream = ByteArrayOutputStream()
        var imageFormat: Bitmap.CompressFormat? = null
        var prefixString = ""
        when ("JPEG") {
            "JPEG" -> {
                imageFormat = Bitmap.CompressFormat.JPEG
                prefixString = "data:image/jpeg;base64,"
            }
            "PNG" -> {
                imageFormat = Bitmap.CompressFormat.PNG
                prefixString = "data:image/png;base64,"
            }
            "WEBP" -> {
                imageFormat = Bitmap.CompressFormat.WEBP
                prefixString = "data:image/webp;base64,"
            }
        }
        bitmapHalf.compress(
            imageFormat,
            50,
            byteArrayOutputStream
        )

        // Получаем изображение из потока в виде байтов
        val bytes = byteArrayOutputStream.toByteArray()

        // Кодируем байты в строку Base64 и возвращаем
        return prefixString + Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}

/**
 * Получение растрового изображения по uri файла
 *
 * @param uri uri файла
 * @return растровое изображение, объект класса [Bitmap]
 * @throws IOException возвращаемая ошибка для последующей обработки
 */
private fun Uri.getBitmap(context: Context): Bitmap? {
    return try {
        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(this, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close();
        image
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        null
    }
}

fun ImageView.loadImage(url: String) {
    Glide.with(context)
        .load(url)
        .into(this)
}
