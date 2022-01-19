package com.intas.metrolog.util

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

class FileUtil {

    companion object {
        private var contentUri: Uri? = null

        private lateinit var context: Context

        fun setContext(context: Context) {
            this.context = context
        }

        /**
         * Получение месторасположения файла по его uri
         * @param uri - uri файла
         * @return месторасположение файла
         */
        fun getPath(uri: Uri): String? {
            // check here to KITKAT or new version
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            // DocumentProvider
            if (isKitKat) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    val fullPath = getPathFromExtSD(split)
                    return if (fullPath !== "") {
                        fullPath
                    } else {
                        null
                    }
                }

                // DownloadsProvider
                if (isDownloadsDocument(uri)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val id: String
                        var cursor: Cursor? = null
                        try {
                            cursor = context.getContentResolver().query(
                                uri,
                                arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                                null,
                                null,
                                null
                            )
                            if (cursor != null && cursor.moveToFirst()) {
                                val fileName: String = cursor.getString(0)
                                val path = Environment.getExternalStorageDirectory()
                                    .toString() + "/Download/" + fileName
                                if (!TextUtils.isEmpty(path)) {
                                    return path
                                }
                            }
                        } finally {
                            if (cursor != null) cursor.close()
                        }
                        id = DocumentsContract.getDocumentId(uri)
                        if (!TextUtils.isEmpty(id)) {
                            if (id.startsWith("raw:")) {
                                return id.replaceFirst("raw:".toRegex(), "")
                            }
                            val contentUriPrefixesToTry = arrayOf(
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                            )
                            for (contentUriPrefix in contentUriPrefixesToTry) {
                                return try {
                                    val contentUri: Uri = ContentUris.withAppendedId(
                                        Uri.parse(contentUriPrefix),
                                        java.lang.Long.valueOf(id)
                                    )
                                    getDataColumn(
                                        context,
                                        contentUri,
                                        null,
                                        null
                                    )
                                } catch (e: NumberFormatException) {
                                    //In Android 8 and Android P the id is not a number
                                    uri.getPath()?.replaceFirst("^/document/raw:", "")
                                        ?.replaceFirst("^raw:", "")
                                }
                            }
                        }
                    } else {
                        val id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:".toRegex(), "")
                        }
                        try {
                            contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                java.lang.Long.valueOf(id)
                            )
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                        if (contentUri != null) {
                            return getDataColumn(
                                context,
                                contentUri!!,
                                null,
                                null
                            )
                        }
                    }
                }


                // MediaProvider
                if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                    if (contentUri != null) {
                        return getDataColumn(
                            context, contentUri, selection,
                            selectionArgs
                        )
                    }
                }
                if (isGoogleDriveUri(uri) || isOneDriveUri(uri)) {
                    return getDriveFilePath(uri)
                }
                if (isWhatsAppFile(uri)) {
                    return getFilePathForWhatsApp(uri)
                }
                if ("content".equals(uri.getScheme(), ignoreCase = true)) {
                    if (isGooglePhotosUri(uri)) {
                        return uri.getLastPathSegment()
                    }
                    if (isGoogleDriveUri(uri)) {
                        return getDriveFilePath(uri)
                    }
                    if ("content://${BuildConfig.APPLICATION_ID}.provider" in uri.toString()) {
                        return getDriveFilePath(uri)
                    }
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        // return getFilePathFromURI(context,uri);
                        copyFileToInternalStorage(uri, "userfiles")
                        // return getRealPathFromURI(context,uri);
                    } else {
                        getDataColumn(context, uri, null, null)
                    }
                }
                if ("file".equals(uri.getScheme(), ignoreCase = true)) {
                    return uri.getPath()
                }
            } else {
                if (isWhatsAppFile(uri)) {
                    return getFilePathForWhatsApp(uri)
                }
                if ("content".equals(uri.getScheme(), ignoreCase = true)) {
                    val projection = arrayOf(
                        MediaStore.Images.Media.DATA
                    )
                    var cursor: Cursor? = null
                    try {
                        cursor = context.getContentResolver()
                            .query(uri, projection, selection, selectionArgs, null)
                        cursor?.let {
                            val column_index: Int =
                                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            if (cursor.moveToFirst()) {
                                return cursor.getString(column_index)
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return null
        }

        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(pathData: Array<String>): String {
            val type = pathData[0]
            val relativePath = "/" + pathData[1]
            var fullPath = ""

            // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
            // something like "71F8-2C0A", some kind of unique id per storage
            // don't know any API that can get the root path of that storage based on its id.
            //
            // so no "primary" type, but let the check here for other devices
            if ("primary".equals(type, ignoreCase = true)) {
                fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
                if (fileExists(fullPath)) {
                    return fullPath
                }
            }

            // Environment.isExternalStorageRemovable() is `true` for external and internal storage
            // so we cannot relay on it.
            //
            // instead, for each possible path, check if file exists
            // we'll start with secondary storage as this could be our (physically) removable sd card
            fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
            fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
            return if (fileExists(fullPath)) {
                fullPath
            } else fullPath
        }

        /**
         * Получение пути файла по его uri
         * @param uri - расположение файла
         * @return путь файла
         */
        private fun getDriveFilePath(uri: Uri): String? {
            val returnUri: Uri = uri
            val contentResolver = context.contentResolver
            val returnCursor = contentResolver
                .query(returnUri, null, null, null, null)

            if (returnCursor == null) {
                return null
            }
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
            val file = File(context.getCacheDir(), name)
            try {
                val inputStream =
                    context.getContentResolver().openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable: Int = inputStream?.available() ?: 0

                //int bufferSize = 1024;
                val bufferSize = Math.min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream?.read(buffers).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                Log.e("File Size", "Size " + file.length())
                inputStream?.close()
                outputStream.close()
                Log.e("File Path", "Path " + file.getPath())
                Log.e("File Size", "Size " + file.length())
            } catch (e: Exception) {
                Log.e("Exception", e.localizedMessage)
            }
            return file.getPath()
        }

        /**
         * Used for Android Q+
         * @param uri
         * @param newDirName if you want to create a directory, you can set this variable
         * @return
         */
        private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String? {
            val returnUri: Uri = uri
            val returnCursor = context.getContentResolver().query(
                returnUri, arrayOf(
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
                ), null, null, null
            )


            /*
            * Get the column indexes of the data in the Cursor,
            * move to the first row in the Cursor, get the data,
            * and display it.
            */
            if (returnCursor == null) {
                return null
            }
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
            val output: File
            if (newDirName != "") {
                val dir =
                    File(context.getFilesDir().toString() + "/" + newDirName)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                output = File(
                    context.getFilesDir()
                        .toString() + "/" + newDirName + "/" + name
                )
            } else {
                output = File(context.getFilesDir().toString() + "/" + name)
            }
            try {
                val inputStream =
                    context.getContentResolver().openInputStream(uri)
                val outputStream = FileOutputStream(output)
                var read = 0
                val bufferSize = 1024
                val buffers = ByteArray(bufferSize)
                while (inputStream?.read(buffers).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                Log.e("Exception", e.localizedMessage)
            }
            return output.getPath()
        }

        private fun getFilePathForWhatsApp(uri: Uri): String? {
            return copyFileToInternalStorage(uri, "whatsapp")
        }

        private fun getDataColumn(
            context: Context,
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.getContentResolver().query(
                    uri, projection,
                    selection, selectionArgs, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index: Int = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                if (cursor != null) cursor.close()
            }
            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.getAuthority()
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.getAuthority()
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.getAuthority()
        }

        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.getAuthority()
        }

        private fun isWhatsAppFile(uri: Uri): Boolean {
            return "com.whatsapp.provider.media" == uri.getAuthority()
        }

        private fun isGoogleDriveUri(uri: Uri): Boolean {
            return "com.google.android.apps.docs.storage" == uri.getAuthority() || "com.google.android.apps.docs.storage.legacy" == uri.getAuthority()
        }

        private fun isOneDriveUri(uri: Uri): Boolean {
            val a = uri.authority
            return "com.microsoft.skydrive.content.StorageAccessProvider" == uri.getAuthority() || "com.microsoft.skydrive.content.external" == uri.getAuthority()
        }

        /**
         * Получение MIME типа файла по указанному расположению
         * @param uri - расположение файла
         * @param context - контекст
         * @return MIME тип
         */
        fun getMimeType(uri: Uri, context: Context): String {
            val extension: String

            //Check uri format to avoid null
            extension = if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) ({
                //If scheme is a content
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.getContentResolver().getType(uri))
            }).toString() else {
                //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            }

            return extension
        }

        /**
         * Получение MIME типа файла по указанному пути
         * @param path - путь к файлу
         * @return MIME тип
         */
        fun getMimeType(path: String): String? {
            val extension = path.substring(path.lastIndexOf("."))
            val mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap)
        }

        /**
         * Получение размера файла в виде строки XXX Kb, XXX Mb
         */
        fun File.getStringSizeLengthFile(): String {
            val size = length()
            val df = DecimalFormat("0.00")
            val sizeKb = 1024.0f
            val sizeMb = sizeKb * sizeKb
            val sizeGb = sizeMb * sizeKb
            val sizeTerra = sizeGb * sizeKb
            if (length() < sizeMb) return df.format(length() / sizeKb)
                .toString() + " Kb" else if (length() < sizeGb) return df.format(length() / sizeMb)
                .toString() + " Mb" else if (length() < sizeTerra) return df.format(length() / sizeGb)
                .toString() + " Gb"
            return "0"
        }

        /**
         * Проверка на существование файла по его имени
         * @param filename - имя файла
         * @return true - файл существует, false - файл не существует
         */
        fun isFileExists(filename: String): Boolean {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, filename)
            return file.exists()
        }

        /**
         * Удаление файла по его имени
         * @param filename - имя файла
         * @return true - удаление успешно, false - удаление не удалось
         */
        fun deleteFile(filename: String): Boolean {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(path, filename)
            return file.delete()
        }

        /**
         * Открытие файла
         * @param fileUri - uri расположения файла
         * @param typeString - MIME тип файла
         */
        fun openFileWithIntent(fileUri: Uri, typeString: String) {

            try {
                context.grantUriPermission(
                    context.getPackageName(),
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
            } catch (e: IllegalArgumentException) {
                // on Kitkat api only 0x3 is allowed (FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION)
                context.grantUriPermission(
                    context.getPackageName(),
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.e("MO_OPEN_FILE", e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
            }

            // запускаем активность для просмотра файла
            val target = Intent(Intent.ACTION_VIEW)
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            target.setDataAndType(fileUri, typeString)

            try {
                context.startActivity(Intent.createChooser(target, "Открыть с помощью"))
            } catch (e: ActivityNotFoundException) {
                Log.d("MO_OPEN_FILE", e.message.toString())
                if (BuildConfig.DEBUG) e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }
}