package com.intas.metrolog.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtil {

    companion object {

        val timeZone = TimeZone.getDefault()

        const val DAY_IN_MILLIS = 86399999
        const val DAY_IN_SECONDS = 86399

        /**
         * dd - день
         * MM - месяц
         * YYYY - год
         */
        fun getDateNowByPattern(pattern: String, timeInMili: Long = 0): Int {
            val simpleDateFormat = SimpleDateFormat(pattern)
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            if (timeInMili > 0) {
                calendar.timeInMillis = timeInMili * 1000L
            }
            val a = simpleDateFormat.format(calendar.time)
            return simpleDateFormat.format(calendar.time).toInt()
        }
        /**
         * Преобразование даты-времени в милисекундах
         * @param timeInMili время в милисекундах
         * @param chatType тип чата: 0 - список активных чатов, 1 - чат с собеседником
         * @return дата-время в формате HH:mm для текущего дня, строка "Вчера" для вчерашнего дня, dd.MM.yyyy для остальных дней
         */
        fun getChatMessageDateTime(timeInMili: Long, chatType: Int): String {
            var result = ""
            val timeNow = getUnixDateTimeNow()
            if (timeNow - timeInMili <= 86400) {
                when(chatType) {
                    0 -> result = getShortTimeFromMili(timeInMili)
                    1 -> result = "Сегодня"
                }
            } else if (timeNow - timeInMili > 86400 && timeNow - timeInMili <= 172800) {
                result = "Вчера"
            } else if (timeNow - timeInMili > 172800) {
                result = getShortDataFromMili(timeInMili)
            }
            return result
        }

        /**
         * Получение времени в милисекундах из строки вида yyyy-MM-dd HH:mm:ss
         * @param dateTime дата-время вида yyyy-MM-dd HH:mm:ss
         * @return время в милисекундах
         */
        fun getUnixDateTime(dateTime: String?): Long {
            val calendar = Calendar.getInstance()
            calendar.timeZone = timeZone
            try {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime)
                calendar.time = date
            } catch (e: ParseException) {
            }
            return calendar.timeInMillis / 1000L
        }

        /**
         * Определение времени прошедшего понедельника
         * @return дата-время в милисекундах
         */
        fun getUnixMonday(): Long {
            val calendar = Calendar.getInstance()
            calendar.timeZone = timeZone
            while (calendar[Calendar.DAY_OF_WEEK] != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            return calendar.timeInMillis / 1000L
        }

        /**
         * Определение времени следующего воскресенья
         * @return дата-время в милисекундах
         */
        fun getUnixSunday(): Long {
            val calendar = Calendar.getInstance()
            calendar.timeZone = timeZone
            while (calendar[Calendar.DAY_OF_WEEK] != Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            return calendar.timeInMillis / 1000L
        }

        /**
         * Преобразование даты-времени в милисекундах в строку HH:mm
         * @param timeInMili время в милисекундах
         * @return дата-время в формате HH:mm
         */
        fun getShortTimeFromMili(timeInMili: Long): String {
            val simpleDateFormat = SimpleDateFormat("HH:mm")
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            calendar.timeInMillis = timeInMili * 1000L
            return simpleDateFormat.format(calendar.time)
        }

        /**
         * Преобразование даты-времени в милисекундах в строку dd.MM.yyyy
         * @param timeInMili время в милисекундах
         * @return дата-время в формате dd.MM.yyyy
         */
        fun getShortDataFromMili(timeInMili: Long): String {
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            calendar.timeInMillis = timeInMili * 1000L
            return simpleDateFormat.format(calendar.time)
        }

        /**
         * Преобразование даты-времени в милисекундах в строку dd.MM
         * @param timeInMili время в милисекундах
         * @return дата-время в формате dd.MM
         */
        fun getDayMonthFromMili(timeInMili: Long): String {
            val simpleDateFormat = SimpleDateFormat("dd.MM")
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            calendar.timeInMillis = timeInMili * 1000L
            return simpleDateFormat.format(calendar.time)
        }

        /**
         * Преобразование даты-времени в милисекундах в строку yyyy
         * @param timeInMili время в милисекундах
         * @return дата-время в формате yyyy
         */
        fun getYearFromMili(timeInMili: Long): String {
            val simpleDateFormat = SimpleDateFormat("yyyy")
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            calendar.timeInMillis = timeInMili * 1000L
            return simpleDateFormat.format(calendar.time)
        }

        /**
         * Получение текущей даты-времени в милисекундах
         * @return дата-время в милисекундах
         */
        fun getUnixDateTimeNow(): Long {
            val calendar = GregorianCalendar()
            calendar.timeZone = timeZone
            return calendar.timeInMillis / 1000L
        }

        fun getLongDateFromMili(timeInMili: Long): String {
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            calendar.timeInMillis = timeInMili * 1000L
            return simpleDateFormat.format(calendar.time)
        }

        fun getLongDateFromMiliForDbBackup(timeInMili: Long): String {
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy-hh:mm")
            simpleDateFormat.timeZone = timeZone
            val calendar = GregorianCalendar(timeZone)
            calendar.timeInMillis = timeInMili * 1000L
            return simpleDateFormat.format(calendar.time)
        }
    }
}