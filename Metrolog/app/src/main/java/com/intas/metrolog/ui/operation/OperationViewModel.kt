package com.intas.metrolog.ui.operation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.PAUSED
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import kotlinx.coroutines.launch
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

    val eventItem = db.eventDao().getEventLD(eventId)

    private var _timerDuration = MutableLiveData<Long>()
    val timerDuration: LiveData<Long> = _timerDuration

    init {
        initDefaultValues()
    }

    fun getEquipById(equipId: Long): LiveData<EquipItem> {
        return db.equipDao().getEquipItemByIdLD(equipId)
    }

    fun setTimerValue(duration: Long) {
        _timerDuration.value = duration
    }

    fun startTimer() {
        timer?.cancel()
        timer?.purge()
        timer = Timer()
        timer?.schedule(timerTask {
            timerDuration.value?.let {
                _timerDuration.postValue(it.plus(1))
            }
        }, 0, 1000)
    }

    fun stopTimer() {
        timer?.cancel()
        timer?.purge()
    }

    fun changeControlButtonVisibleValue() {
        controlButtonClicked.value?.let {
            _controlButtonClicked.value = !it
        }
    }

    fun getOperationList(): LiveData<List<EventOperationItem>> {
        return if (eventItem.value?.status == EventStatus.NEW || eventItem.value?.status == EventStatus.IN_WORK) {
            return db.eventOperationDao().getNotCompletedOperationList(eventId)
        } else {
            db.eventOperationDao().getOperationList(eventId)
        }
    }

    fun setOperationComplete(eventOperationItem: EventOperationItem) {
        viewModelScope.launch {
            eventOperationItem.completed = 1
            eventOperationItem.dateEnd = DateTimeUtil.getUnixDateTimeNow()
            eventOperationItem.completedUserId = (Util.authUser?.userId ?: 0).toLong()
            eventOperationItem.isSended = 0

            db.eventOperationDao().updateEventOperation(eventOperationItem)
        }
    }

    private fun initDefaultValues() {
        _controlButtonClicked.value = false
        _timerDuration.value = 0
        timer = Timer()
    }

    /**
     * Установка даты-времени начала выполнения мероприятия. Подсчет продолжительности выполнения мероприятия
     * @param isStopTimer флаг остановки таймера (true - таймер остановлен, false - таймер запущен)
     */
    fun setDateTimeTimer(isStopTimer: Boolean) {

        viewModelScope.launch {

            val dateTime = DateTimeUtil.getUnixDateTimeNow() // получаем текущее время
            val duration =
                eventItem.value?.durationTimer
                    ?: 0 // получаем продолжительность выполнения мероприятия

            if (eventItem.value?.dateTimeStartTimer ?: 0 > 0) { // если таймер уже запущен
                val delta = dateTime - (eventItem.value?.dateTimeStartTimer
                    ?: 0) // разница между текущим временем и временем начала выполнения мероприятия
                val currentDuration =
                    duration.plus(delta) // увеличиваем продолжительность выполнения мероприятия
                if (!isStopTimer) { // если таймер не остановлен
                    eventItem.value?.durationTimer =
                        currentDuration // записываем продолжительность выполнения мероприятия
                }
            }

            eventItem.value?.dateTimeStartTimer =
                dateTime // записываем дату-время начала работы таймера

            eventItem.value?.let {
                db.eventDao().updateEvent(it)
            }
        }
    }

    /**
     * Установка статуса для текущего открытого мероприятия
     * @param status статус мероприятия (0 - новое мероприятие, 1 - выполняется, 2 - остановлено, 3 - выполнено, 4 - отказ от выполнения)
     */
    fun setEventStatus(status: Int, comment: String? = null) {

        viewModelScope.launch {
            eventItem.value?.let {
                comment?.let { comment ->
                    it.comment = comment
                }
                it.status = status

                if (status >= PAUSED) { // мероприятие остановлено, выполнено или отказано
                    it.factDate = it.dateTimeStartTimer.toString() // установка текущей даты-время
                    it.userId =
                        (Util.authUser?.userId
                            ?: 0).toString()  // установка идентификатора пользователя
                    it.otv = Util.authUser?.fio // установка фио ответственного
                    it.eventDone = true
                    if (status != PAUSED) {
                        it.isSended = 0
                    }
                }
                Journal.insertJournal("OperationViewModel->setEventStatus->Event", it.toString())
                db.eventDao().updateEvent(it)
            }
        }
    }

    override fun onCleared() {
        timer?.cancel()
        timer?.purge()
        super.onCleared()
    }
}