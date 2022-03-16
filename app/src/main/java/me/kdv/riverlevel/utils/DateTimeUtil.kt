package me.kdv.riverlevel.utils

import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtil {
    companion object {
        val timeZone = TimeZone.getDefault()

        fun getUnixDateTimeNow(): Long {
            val calendar: Calendar = GregorianCalendar()
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
    }
}