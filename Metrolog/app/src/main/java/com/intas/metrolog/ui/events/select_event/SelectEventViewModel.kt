package com.intas.metrolog.ui.events.select_event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_priority.EventPriority
import com.intas.metrolog.util.DateTimeUtil

class SelectEventViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val startDate = DateTimeUtil.getBeginToday()
    private val endDate = DateTimeUtil.getEndToday()

    fun getEventByRfid(rfid: String): List<EventItem> {
        return db.eventDao().getEventListByRfid(rfid)
    }

    /**
     * Получение из БД высокоприоритетных [EventPriority.SERIOUS],[EventPriority.ACCIDENT] мероприятий, ожидающих выполнения
     * @return - список мероприятий
     */
    fun getHighPriorityEventList(): List<EventItem> {
        return db.eventDao().getHighPriorityEventList(startDate, endDate)
    }

    /**
     * Получение из БД запущенного высокоприоритетного мероприятия
     * @return - список мероприятий
     */
    fun getLaunchedHighPriorityEvent(): List<EventItem> {
        return db.eventDao().getLaunchedHighPriorityEvent(startDate, endDate)
    }
}