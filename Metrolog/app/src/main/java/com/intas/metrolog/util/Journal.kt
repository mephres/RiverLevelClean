package com.intas.metrolog.util

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.preference.PreferenceManager
import com.intas.metrolog.BuildConfig
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.JournalItem
import kotlinx.coroutines.*
import java.io.*
import java.util.*

/**
 * Класс-синглтон для журналирования событий
 */
object Journal {

    private lateinit var db: AppDatabase
    private const val DIR_SD = "MobOpLogs"
    private var FILENAME_SD = ""
    private lateinit var sdFile: File

    var onJournalExportComplete: ((File) -> Unit)? = null
    var onJournalExportFailure: ((String) -> Unit)? = null
    var onJournalExportError: ((String) -> Unit)? = null
    var onJournalExportProcess: ((String) -> Unit)? = null

    /**
     * Инициализация
     */
    fun init(context: Context) {
        Util.eventLoggingEnabled = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("event_log_switch", false)
        db = AppDatabase.getInstance(context)
    }

    @Deprecated(message = "В данной версиии не используется")
    /**
     * Генерация текстового файла журналов
     */
    private fun setFileName() {
        val calendar = Calendar.getInstance()
        calendar.timeZone = DateTimeUtil.timeZone

        val day = calendar[Calendar.DAY_OF_MONTH]
        val month = calendar[Calendar.MONTH] + 1
        val year = calendar[Calendar.YEAR]
        var sDay = day.toString()
        var sMonth = month.toString()
        if (day < 10) {
            sDay = "0$sDay"
        }
        if (month < 10) {
            sMonth = "0$sMonth"
        }
        FILENAME_SD = "$year.$sMonth.$sDay.txt"
    }

    /**
     * Сохранение в локальную БД событий
     * @param comment - комментарий
     * @param journalText - текст события
     * @param journalType - тип события
     */
    fun insertJournal(comment: String, journalText: Any, journalType: Int = 0) {
        if (Util.eventLoggingEnabled) {
            CoroutineScope(Dispatchers.IO).launch {
                db.journalDao().insertJournal(
                    JournalItem(
                        comment = comment,
                        journalText = journalText.toString(),
                        journalType = journalType
                    )
                )
            }
        }
    }

    /**
     * Сохранение в локальную БД событий
     * @param comment - комментарий
     * @param list - список событий
     * @param journalType - тип события
     */
    fun insertJournal(comment: String, list: List<Any>?, journalType: Int = 0) {
        if (Util.eventLoggingEnabled) {
            CoroutineScope(Dispatchers.IO).launch {
                list?.forEach {
                    insertJournal(
                        comment = comment,
                        journalText = it.toString(),
                        journalType = journalType
                    )
                }
            }
        }
    }

    /**
     * Экспортирование событий из БД в файл
     * @param startTime - начало периода записи
     * @param endTime - конец периода записи
     */
    fun exportJournalFromDb(startTime: Long, endTime: Long) {
        // проверяем доступность хранилища
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val job = async(Dispatchers.IO) {
                FILENAME_SD = ""
                try {
                    // получем из БД список событий по выбранному периоду
                    val journal =
                        db.journalDao().getJournalByDateRange(
                            startTime = startTime.div(1000),
                            endTime = (endTime.plus(86399999)).div(1000)
                        )

                    // создаем директорию для хранения файлов
                    val sdPath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + DIR_SD)
                    if (!sdPath.exists()) {
                        sdPath.mkdir()
                    }

                    if (!journal.isNullOrEmpty()) {
                        // присваеваем имя для файла в виде выбранного временного среза
                        FILENAME_SD = "${DateTimeUtil.getShortDataFromMili(startTime.div(1000))}-${
                            DateTimeUtil.getShortDataFromMili(endTime.div(1000))
                        }.txt"

                        sdFile = File(sdPath.path, FILENAME_SD)

                        if (sdFile.exists()) {
                            sdFile.delete()
                        }

                        // пишем информацию о пользователе и девайсе
                        val log = StringBuilder(
                            "userId: ${Util.authUser?.userId} \n" +
                                    "appVersion: ${BuildConfig.VERSION_NAME} \n" +
                                    "deviceBrand: ${Build.BRAND} \n" +
                                    "deviceModel: ${Build.MODEL} \n" +
                                    "version: ${Build.VERSION.SDK_INT} \n" +
                                    "versionRelease: ${Build.VERSION.RELEASE} \n\n"
                        )
                        // сохраняем в строковом виде данные по событиям
                        journal.forEach {
                            log.append(it.dateTimeString + "\t" + it.comment + "\t" + it.journalText + "\t" + it.journalType + "\n\n")
                        }


                        // пишем данные в файл
                        val fileWriter = FileWriter(sdFile, true)
                        val bufferedWriter = BufferedWriter(fileWriter)

                        bufferedWriter.newLine()
                        bufferedWriter.write(log.toString())
                        bufferedWriter.close()
                        fileWriter.close()
                    }
                    sdFile
                } catch (f: FileNotFoundException) {
                    f
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            // ждем выполнения сопрограммы
            onJournalExportProcess?.invoke("Запущен процесс экспорта")
            job.await()
            Log.d("dfsdfsdf", job.await().toString())
            // выводим результат работы сопрограммы
            if (FILENAME_SD.isNotEmpty() && sdFile.exists()) {
                onJournalExportComplete?.invoke(
                    sdFile
                )
            } else {
                onJournalExportFailure?.invoke(
                    "За выбранный период журналы отсутствуют"
                )
            }

            if (job.await() is FileNotFoundException) {
                onJournalExportError?.invoke(
                    "Предоставьте права доступа"
                )
            }

            // закрываем поток сопрограммы
            job.cancel()
        }
    }

    /**
     * Удаление записей из БД и файлов, хранящихся более "day" дней
     * @param day - количество дней хранения записей и файлов
     */
    fun deleteOldJournal(day: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            // очищаем журналы из базы, хранящиеся более day дней
            val twoWeakLater = DateTimeUtil.getUnixDateTimeNow() - (day * 86400).toLong()
            db.journalDao().deleteJournalByDate(twoWeakLater)

            // проверяем доступность хранилища
            if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                return@launch
            }
            // получаем путь к директории
            val sdPath: File = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + File.separator + DIR_SD
            )

            // очищаем файлы логов в хранилище, хранящиеся более 2 недель
            if (sdPath.exists()) {
                val datetimeNow = DateTimeUtil.getUnixDateTimeNow()

                sdPath.listFiles()?.let { fileList ->
                    for (file in fileList) {
                        val fileDateTime = file.lastModified() / 1000
                        val interval = (day * 86400).toLong()
                        val diff = datetimeNow - fileDateTime
                        if (diff > interval) {
                            try {
                                if (!file.delete()) {
                                    file.canonicalFile.delete()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Удаление всех журналов работы в базе данных
     */
    fun deleteAllJournal() {
        CoroutineScope(Dispatchers.IO).launch {
            db.journalDao().deleteAllJournal()
        }
    }
}