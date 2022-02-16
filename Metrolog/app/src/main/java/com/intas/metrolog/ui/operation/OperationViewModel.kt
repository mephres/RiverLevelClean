package com.intas.metrolog.ui.operation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem
import com.intas.metrolog.pojo.event.event_priority.EventPriority
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
    val eventPhotoList = db.eventPhotoDao().getEventPhotoListById(eventId)

    private var _timerDuration = MutableLiveData<Long>()
    val timerDuration: LiveData<Long> = _timerDuration

    private val startDate = DateTimeUtil.getBeginToday()
    private val endDate = DateTimeUtil.getEndToday()

    init {
        initDefaultValues()
    }

    /**
     * Проверка наличия важных [EventPriority.SERIOUS] мероприятий, ожидающих выполнения
     * @return - true/false
     */
    fun isSeriousPriorityEventsExists(): Boolean {
        return db.eventDao().isSeriousPriorityEventsExists(startDate, endDate, EventPriority.SERIOUS.ordinal)
    }

    /**
     * Проверка наличия аварийных [EventPriority.ACCIDENT] мероприятий, ожидающих выполнения
     * @return - true/false
     */
    fun isAccidentPriorityEventsExists(): Boolean {
        return db.eventDao().isAccidentPriorityEventsExists(startDate, endDate, EventPriority.ACCIDENT.ordinal)
    }

    /**
     * Проверка наличия запущенного мероприятия в высшим приоритетом
     * @return - true/false
     */
    fun isHighPriorityEventsLaunched(): Boolean {
        return db.eventDao().isHighPriorityEventsLaunched(accident = EventPriority.ACCIDENT.ordinal,
            serious = EventPriority.SERIOUS.ordinal)
    }

    /**
     * Проверка наличия в БД не просмотренной приоритеной информации по оборудованию
     * @param - equipId идентификатор оборудования
     * @return - true/false
     */
    fun isNotCheckedEquipInfoExists(equipId: Long): Boolean {
        return db.equipInfoDao().isNotCheckedEquipInfoExists(equipId)
    }

    fun getEquipById(equipId: Long): LiveData<EquipItem> {
        return db.equipDao().getEquipItemByIdLD(equipId)
    }

    fun setTimerValue(duration: Long) {
        _timerDuration.value = duration
    }

    /**
     * Запуск таймера
     */
    fun startTimer() {
        Journal.insertJournal("OperationViewModel->startTimer", "")
        timer?.cancel()
        timer?.purge()
        timer = Timer()
        timer?.schedule(timerTask {
            timerDuration.value?.let {
                _timerDuration.postValue(it.plus(1))
            }
        }, 0, 1000)
    }

    /**
     * Остановка таймера
     */
    fun stopTimer() {
        timer?.cancel()
        timer?.purge()
        Journal.insertJournal("OperationViewModel->stopTimer", "")
    }

    fun changeControlButtonVisibleValue() {
        controlButtonClicked.value?.let {
            _controlButtonClicked.value = !it
        }
    }

    /**
     * Получение списка операций мероприятия
     */
    fun getOperationList(): LiveData<List<EventOperationItem>> {
        return if (eventItem.value?.status == EventStatus.NEW || eventItem.value?.status == EventStatus.IN_WORK) {
            //если мероприятие в статусе НОВОЕ или ВЫПОЛНЯЕТСЯ, то выводим список всех невыполненных операций
            db.eventOperationDao().getNotCompletedOperationList(eventId)
        } else {
            // иначе - список всех операций
            db.eventOperationDao().getOperationList(eventId)
        }
    }

    /**
     * Выполнение операции
     */
    fun setOperationComplete(eventOperationItem: EventOperationItem) {
        viewModelScope.launch {
            Journal.insertJournal("OperationViewModel->setOperationComplete->eventOperationItem", eventOperationItem)
            eventOperationItem.completed = 1
            eventOperationItem.dateEnd = DateTimeUtil.getUnixDateTimeNow()
            eventOperationItem.completedUserId = (Util.authUser?.userId ?: 0).toLong()
            eventOperationItem.isSended = 0

            db.eventOperationDao().updateEventOperation(eventOperationItem)
            Journal.insertJournal("OperationViewModel->setOperationComplete->updatedEvent", eventOperationItem)
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

        Journal.insertJournal("OperationViewModel->setDateTimeTimer->isStopTimer", isStopTimer)
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
                Journal.insertJournal("OperationViewModel->setDateTimeTimer->event", it)
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
            Journal.insertJournal("OperationViewModel->setEventStatus", "status: $status, comment: $comment")
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
                Journal.insertJournal("OperationViewModel->setEventStatus->Event", it)
                db.eventDao().updateEvent(it)
            }
        }
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}