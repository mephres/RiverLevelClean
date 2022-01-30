package com.intas.metrolog.ui.operation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import java.util.*
import kotlin.concurrent.timerTask

class OperationViewModel(
    private val application: Application,
    private val eventId: Long
) : ViewModel() {

    private val db = AppDatabase.getInstance(application)
    private var timer: Timer? = null

    private val _controlButtonClicked = MutableLiveData<Boolean>()
    val controlButtonClicked: LiveData<Boolean> = _controlButtonClicked

    private val _eventItem = MutableLiveData<EventItem>()
    val eventItem: LiveData<EventItem> = _eventItem

    private var _timerDuration = MutableLiveData<Long>()
    val timerDuration: LiveData<Long> = _timerDuration

    init {
        initDefaultValues()
        getEvent()
    }

    fun startTimer() {
        timer?.cancel()
        timer?.purge()
        timer = Timer()
        timer?.schedule(timerTask{
            timerDuration.value?.let {
                _timerDuration.postValue(it.plus(1))
            }
        }, 0, 1000)
    }

    fun stopTimer() {
        timer?.cancel()
        timer?.purge()
    }

    fun updateEventStatus(newStatus: Int) {
        _eventItem.value  = eventItem.value?.apply {
            status = newStatus
        }
    }

    fun changeControlButtonVisibleValue() {
        controlButtonClicked.value?.let {
            _controlButtonClicked.value = !it
        }
    }

    fun getCheckList(): LiveData<List<EventOperationItem>> {
        return db.eventOperationDao().getCheckList(eventId)
    }

    private fun getEquip(equipId: Long): EquipItem? {
        return db.equipDao().getEquipItemById(equipId)
    }

    private fun getEvent() {
        val item = db.eventDao().getEvent(eventId)
        item?.let {
            it.equip = getEquip(it.equipId ?: 0)
            _eventItem.value = it
        }
    }

    private fun initDefaultValues() {
        _controlButtonClicked.value = false
        _timerDuration.value = 0
        timer = Timer()
    }

    override fun onCleared() {
        timer?.cancel()
        timer?.purge()
        super.onCleared()
    }
}