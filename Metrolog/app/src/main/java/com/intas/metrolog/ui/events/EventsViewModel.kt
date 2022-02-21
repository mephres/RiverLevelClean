package com.intas.metrolog.ui.events

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.util.DateTimeUtil
import kotlinx.coroutines.launch

class EventsViewModel(application: Application) : AndroidViewModel(application)  {

    private val db = AppDatabase.getInstance(application)
    val eventList = db.eventDao().getEventList()

    private val _scroll = MutableLiveData<Int>()
    val scroll: LiveData<Int> get() = _scroll

    private val _searchText = MutableLiveData<String>()
    val searchText: LiveData<String> get() = _searchText

    fun onScrolled(dy: Int) {
        _scroll.value = dy
    }

    fun setSearchText(text: String) {
        _searchText.value = text
    }

    fun getEventListToday(): LiveData<List<EventItem>> {

        val startDate = DateTimeUtil.getBeginToday()
        val endDate = DateTimeUtil.getEndToday()

        Log.d("MO_GET_EVENT_TODAY", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate)
    }

    fun getEventListWeek(): LiveData<List<EventItem>> {

        val startDate = DateTimeUtil.getUnixMonday()
        val endDate = DateTimeUtil.getUnixSunday()

        Log.d("MO_GET_EVENT_WEEK", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate)
    }

    fun getEventListMonth(): LiveData<List<EventItem>> {

        val startDate= DateTimeUtil.getFirstDayOfMonth()
        val endDate = DateTimeUtil.getLastDayOfMonth()

        Log.d("MO_GET_EVENT_MONTH", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate)
    }

    fun getEventListCompleted(): LiveData<List<EventItem>> {

        val startDate= DateTimeUtil.getFirstDayOfMonth()
        val endDate = DateTimeUtil.getLastDayOfMonth()

        Log.d("MO_GET_EVENT_COMPLETED", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate, EventStatus.COMPLETED)
    }

    fun getEventListCanceled(): LiveData<List<EventItem>> {

        val startDate= DateTimeUtil.getFirstDayOfMonth()
        val endDate = DateTimeUtil.getLastDayOfMonth()

        Log.d("MO_GET_EVENT_CANCELED", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate, EventStatus.CANCELED)
    }

    fun deleteEventById(eventId: Long) {
        viewModelScope.launch {
            db.eventDao().deleteEventByEventId(eventId)
            db.eventOperationDao().deleteEventOperationByEventId(eventId)
            db.eventPhotoDao().deleteEventPhotoByEventId(eventId)
            db.operControlDao().deleteOperControlByEventId(eventId)
        }
    }
}