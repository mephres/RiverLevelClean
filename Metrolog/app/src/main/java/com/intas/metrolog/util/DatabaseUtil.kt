package com.intas.metrolog.util

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.intas.metrolog.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * Класс-синглтон для копирования базы данных
 */
object DatabaseUtil {
    private var FILE_NAME = ""
    private var ZIP_NAME = ""
    private const val DIR_SD = "MetrologBackup"
    var onBackupComplete: ((File) -> Unit)? = null
    var onBackupProcess: ((String) -> Unit)? = null
    var onBackupError: ((String) -> Unit)? = null
    private lateinit var saveFile: File
    private lateinit var zipFile: File

    /**
     * Функция копирования БД и записи в архив в локальном хранилище
     */
    fun backupDatabase(context: Context) {
        // проверяем доступность хранилища
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val job = async(Dispatchers.IO) {
                FILE_NAME = ""
                ZIP_NAME = ""
                try {
                    // получаем экземпляр БД
                    val appDatabase: AppDatabase = AppDatabase.getInstance(context)
                    // устанавливаем конечную точку копирования БД
                    appDatabase.userDao()
                        .checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
                    // получаем файл с записями БД
                    val dbFile = File(appDatabase.openHelper.writableDatabase.path)
                    // формируем именования для файлов
                    FILE_NAME =
                        "${DateTimeUtil.getShortDataFromMili(DateTimeUtil.getUnixDateTimeNow())}.db"
                    ZIP_NAME =
                        "${DateTimeUtil.getShortDataFromMili(DateTimeUtil.getUnixDateTimeNow())}.zip"
                    // получаем файл с записями БД
                    val sdPath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + DIR_SD)

                    if (!sdPath.exists()) {
                        sdPath.mkdirs()
                    }

                    saveFile = File(sdPath.path, FILE_NAME)
                    zipFile = File(sdPath.path, ZIP_NAME)

                    if (saveFile.exists()) {
                        saveFile.delete()
                    }
                    if (zipFile.exists()) {
                        zipFile.delete()
                    }

                    val buff = ByteArray(1024 * 8)
                    val output = FileOutputStream(saveFile, true)
                    val inDb: InputStream = FileInputStream(dbFile)
                    // пишем данные
                    while (true) {
                        val readed = inDb.read(buff)
                        if (readed == -1) {
                            break
                        }
                        output.write(buff, 0, readed ?: 0)
                    }
                    output.flush()
                    output.close()
                    // архивируем файл
                    zip(saveFile, zipFile)
                    // возвращаем архированный файл
                    zipFile
                } catch (fnf: FileNotFoundException) {
                    fnf
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("DatabaseUtil", e.message.toString())
                }
            }
            onBackupProcess?.invoke("Запущен процесс копирования базы данных")
            // ожидание выполнения сохранения в архив
            job.await()
            // если архив с данными сохранен, запускаем ACTION_SEND в SettingsActivity
            if (zipFile.exists()) {
                onBackupComplete?.invoke(zipFile)
            }
            // если job возвращает исключение FileNotFoundException, запускаем ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION в SettingsActivity
            if (job.await() is FileNotFoundException) {
                onBackupError?.invoke(
                    "Предоставьте права доступа"
                )
            }
            // закрываем поток сопрограммы
            job.cancel()
        }
    }

    /**
     * Запись файла с данными локальной базы в архив
     * @param file - файл с записями из БД
     * @param zipFile - архив, куда добавляется file
     */
    @Throws(IOException::class)
    fun zip(file: File?, zipFile: File?) {
        var origin: BufferedInputStream? = null
        val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile?.path)))
        try {
            val data = ByteArray(8 * 1024)

            val fi = FileInputStream(file)
            origin = BufferedInputStream(fi, 8 * 1024)
            try {
                // записываем файл в архив
                val entry = ZipEntry(file?.path?.substring(file.path.lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, 8 * 1024).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                // если файл архивирован, удаляем файл
                file?.delete()
            } finally {
                origin.close()
            }

        } finally {
            out.flush()
            out.close()
        }
    }
}