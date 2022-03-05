package com.intas.metrolog.ui.events

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.util.DateTimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventsViewModel(application: Application) : AndroidViewModel(application) {

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

        return db.eventDao().getEventList(startDate, endDate).map {
            it.map {
                it.operationListSize = db.eventOperationDao().getOperationListSize(it.opId)
                it.equip = db.equipDao().getEquipItemById(it.equipId ?: 0)
            }
            it
        }
    }

    fun getEventListWeek(): LiveData<List<EventItem>> {

        val startDate = DateTimeUtil.getUnixMonday()
        val endDate = DateTimeUtil.getUnixSunday()

        Log.d("MO_GET_EVENT_WEEK", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate).map {
            it.map {
                it.operationListSize = db.eventOperationDao().getOperationListSize(it.opId)
                it.equip = db.equipDao().getEquipItemById(it.equipId ?: 0)
            }
            it
        }
    }

    fun getEventListMonth(): LiveData<List<EventItem>> {

        val startDate = DateTimeUtil.getFirstDayOfMonth()
        val endDate = DateTimeUtil.getLastDayOfMonth()

        Log.d("MO_GET_EVENT_MONTH", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate).map {
            it.map {
                it.operationListSize = db.eventOperationDao().getOperationListSize(it.opId)
                it.equip = db.equipDao().getEquipItemById(it.equipId ?: 0)
            }
            it
        }
    }

    fun getEventListCompleted(): LiveData<List<EventItem>> {

        val startDate = DateTimeUtil.getFirstDayOfMonth()
        val endDate = DateTimeUtil.getLastDayOfMonth()

        Log.d("MO_GET_EVENT_COMPLETED", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate, EventStatus.COMPLETED).map {
            it.map {
                it.operationListSize = db.eventOperationDao().getOperationListSize(it.opId)
                it.equip = db.equipDao().getEquipItemById(it.equipId ?: 0)
            }
            it
        }
    }

    fun getEventListCanceled(): LiveData<List<EventItem>> {

        val startDate = DateTimeUtil.getFirstDayOfMonth()
        val endDate = DateTimeUtil.getLastDayOfMonth()

        Log.d("MO_GET_EVENT_CANCELED", "startDate: $startDate, endDate: $endDate")

        return db.eventDao().getEventList(startDate, endDate, EventStatus.CANCELED).map {
            it.map {
                it.operationListSize = db.eventOperationDao().getOperationListSize(it.opId)
                it.equip = db.equipDao().getEquipItemById(it.equipId ?: 0)
            }
            it
        }
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