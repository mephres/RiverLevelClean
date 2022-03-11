package me.kdv.riverlevel.utils

import java.util.*

class DateTimeUtil {
    companion object {
        val timeZone = TimeZone.getDefault()

        fun getUnixDateTimeNow(): Long {
            val calendar: Calendar = GregorianCalendar()
            calendar.timeZone = timeZone
            return calendar.timeInMillis / 1000L
        }
    }
}