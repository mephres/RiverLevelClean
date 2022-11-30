package me.kdv.riverlevel.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {

    val timeZone = TimeZone.getDefault()

    fun getUnixDateTimeNow(): Long {
        val calendar: Calendar = GregorianCalendar()
        calendar.timeZone = timeZone
        return calendar.timeInMillis / 1000L
    }

    @SuppressLint("SimpleDateFormat")
    fun getLongDateFromMilli(timeInMilli: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        simpleDateFormat.timeZone = timeZone
        val calendar = GregorianCalendar(timeZone)
        calendar.timeInMillis = timeInMilli * 1000L
        return simpleDateFormat.format(calendar.time)
    }
}