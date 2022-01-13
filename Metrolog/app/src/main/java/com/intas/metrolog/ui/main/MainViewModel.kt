package com.intas.metrolog.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.api.ApiFactory
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ACCURACY
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ALTITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_BEARING
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ELAPSED_REALTIME_NANOS
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LATITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LONGITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_PROVIDER
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_SPEED
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_TIME
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_USER_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_EQUIP_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_EQUIP_RFID
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.event_priority.EventPriority
import com.intas.metrolog.pojo.event_status.EventStatus
import com.intas.metrolog.pojo.operation.EventOperationItem
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.metrolog.util.SingleLiveEvent
import com.intas.metrolog.util.Util
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    private var sendUserLocationDisposable: Disposable? = null
    private var sendEquipLocationDisposable: Disposable? = null
    private var sendEquipRFIDDisposable: Disposable? = null

    val notSendedUserLocationList = db.userLocationDao().getNotSendedUserLocationList()

    val onErrorMessage = SingleLiveEvent<String>()

    init {
        getEquip()
        getRequestStatus()
        getDiscipline()
        getEventOperation()
        getDocumentType()
        getEquipInfoPriority()
        getEventStatus()
        getEventPriority()
        getEventComment()
    }

    /**
     * Сохранение списка оборудования в БД
     * @param equipList - список оборудования
     */
    private fun insertEquipList(equipList: List<EquipItem>) {

        Log.d("MM_INSERT_EQUIP", equipList.toString())

        viewModelScope.launch {
            db.equipDao().insertEquipList(equipList)
        }
    }

    /**
     * Сохранение информации по оборудованию в БД
     * @param equipInfoList - список информации по оборудованию
     */
    private fun insertEquipInfoList(equipInfoList: List<EquipInfo>) {

        Log.d("MM_INSERT_EQUIP_INFO", equipInfoList.toString())

        viewModelScope.launch {
            db.equipDao().insertEquipInfoList(equipInfoList)
        }
    }

    /**
     * Получение списка оборудования
     */
    fun getEquip() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEquip(it)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { equip ->
                        insertEquipList(equip)

                        equip.forEach {
                            if(it.equipInfoList.isNotEmpty()) insertEquipInfoList(it.equipInfoList)
                        }
                    }
                }, {
                    onErrorMessage.postValue("При получении списка оборудования с сервера произошла ошибка - " +
                            "${it.message}")
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка комментариев для мероприятия в БД
     * @param eventCommentList - список комментариев для мероприятия
     */
    private fun insertEventCommentList(eventCommentList: List<EventComment>) {

        Log.d("MM_INSERT_EVENT_COMMENT", eventCommentList.toString())

        viewModelScope.launch {
            db.eventCommentDao().insertEventCommentList(eventCommentList)
        }
    }

    /**
     * Получение списка комментариев для мероприятия
     */
    private fun getEventComment() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEventCommentList(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { eventComment ->
                        insertEventCommentList(eventComment)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка приоритетов для мероприятия в БД
     * @param eventPriorityList - список приоритетов для мероприятия
     */
    private fun insertEventPriorityList(eventPriorityList: List<EventPriority>) {

        Log.d("MM_INSERT_EVENT_PRIOR", eventPriorityList.toString())

        viewModelScope.launch {
            db.eventPriorityDao().insertEventPriorityList(eventPriorityList)
        }
    }

    /**
     * Получение списка приоритетов для мероприятия
     */
    private fun getEventPriority() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEventPriority(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { eventPriority ->
                        insertEventPriorityList(eventPriority)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка статусов мероприятий в БД
     * @param eventStatusList - список статусов мероприятий
     */
    private fun insertEventStatusList(eventStatusList: List<EventStatus>) {

        Log.d("MM_INSERT_EVENT_STATUS", eventStatusList.toString())

        viewModelScope.launch {
            db.eventStatusDao().insertEventStatusList(eventStatusList)
        }
    }

    /**
     * Получение списка статусов мероприятий
     */
    private fun getEventStatus() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEventStatus(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { eventStatus ->
                        insertEventStatusList(eventStatus)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка приоритетов для комментариев по оборудованию в БД
     * @param equipInfoPriorityList - список приоритетов для комментариев по оборудованию
     */
    private fun insertEquipInfoPriorityList(equipInfoPriorityList: List<EquipInfoPriority>) {

        Log.d("MM_INSERT_EQ_PRIORITY", equipInfoPriorityList.toString())

        viewModelScope.launch {
            db.equipInfoPriorityDao().insertEquipInfoPriorityList(equipInfoPriorityList)
        }
    }

    /**
     * Получение списка приоритетов для комментариев по оборудованию
     */
    private fun getEquipInfoPriority() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEquipInfoPriorityList(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { equipPriority ->
                        insertEquipInfoPriorityList(equipPriority)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка типов документов в БД
     * @param documentTypeList - список типов документов
     */
    private fun insertDocumentTypeList(documentTypeList: List<DocumentType>) {

        Log.d("MM_INSERT_DOCUMENT_TYPE", documentTypeList.toString())

        viewModelScope.launch {
            db.documentTypeDao().insertDocumentTypeList(documentTypeList)
        }
    }

    /**
     * Получение списка типов документов
     */
    private fun getDocumentType() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getDocumentTypeList(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { docType ->
                        insertDocumentTypeList(docType)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка операций мероприятий в БД
     * @param operationList - список операций
     */
    private fun insertEventOperationList(operationList: List<EventOperationItem>) {

        Log.d("MM_INSERT_OPERATION", operationList.toString())

        viewModelScope.launch {
            db.eventOperationDao().insertEventOperationList(operationList)
        }
    }

    /**
     * Получение списка операций мероприятий
     */
    private fun getEventOperation() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEventOperation(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { operation ->
                        insertEventOperationList(operation)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка дисциплин в БД
     * @param disciplineList - список дисциплин
     */
    private fun insertDisciplineList(disciplineList: List<DisciplineItem>) {

        Log.d("MM_INSERT_DISCIPLINE", disciplineList.toString())

        viewModelScope.launch {
            db.disciplineDao().insertDisciplineList(disciplineList)
        }
    }

    /**
     * Получение списка дисциплин
     */
    private fun getDiscipline() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getDiscipline(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { discipline ->
                        insertDisciplineList(discipline)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка возможных статусов заявок в БД
     * @param requestStatusList - список статусов заявок
     */
    private fun insertRequestStatusList(requestStatusList: List<RequestStatusItem>) {

        Log.d("MM_INSERT_REQUEST_ST", requestStatusList.toString())

        viewModelScope.launch {
            db.requestStatusDao().insertRequestStatusList(requestStatusList)
        }
    }

    /**
     * Получение списка возможных статусов заявок
     */
    private fun getRequestStatus() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getRequestStatus(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { status ->
                        insertRequestStatusList(status)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Отправка на сервер ЦНО данных с геокоординатами оборудования
     *
     * @param equip оборудование, экземпляр класса [EquipItem]
     */
    fun sendEquipLocation(equip: EquipItem) {
        sendEquipLocationDisposable?.let {
            compositeDisposable.remove(it)
        }

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_EQUIP_ID] = equip.equipId.toString()
        map[QUERY_PARAM_LATITUDE] = equip.latitude.toString()
        map[QUERY_PARAM_LONGITUDE] = equip.longitude.toString()

        sendEquipLocationDisposable = ApiFactory.apiService.updateEquipGeo(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EQUIP_LOCATION", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.requestSuccess != null) {
                    it.requestSuccess?.id?.toLong()?.let {
                        setEquipLocationSendedById(it)
                    }
                }
                Log.d("MM_SEND_EQUIP_LOCATION", it.toString())
            }, {
                Log.d("MM_SEND_EQUIP_LOCATION", it.message.toString())
            })
        sendEquipLocationDisposable?.let {
            compositeDisposable.add(it)
        }
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setEquipLocationSendedById(id: Long) {

        Log.d("MM_EQUIP_LOCATION_SEND", id.toString())

        viewModelScope.launch {
            db.equipDao().setEquipLocationSendedById(id)
        }
    }

    /**
     * Отправка на сервер ЦНО данных с меткой оборудования
     *
     * @param equip оборудование, экземпляр класса [EquipItem]
     */
    fun sendEquipRFID(equip: EquipItem) {
        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_EQUIP_ID] = equip.equipId.toString()
        map[QUERY_PARAM_EQUIP_RFID] = equip.equipRFID.toString()

        sendEquipRFIDDisposable = ApiFactory.apiService.updateEquipRFID(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EQUIP_RFID", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.requestSuccess != null) {
                    it.requestSuccess?.id?.toLong()?.let {
                        setEquipRFIDSendedById(it)
                    }
                }
                Log.d("MM_SEND_EQUIP_RFID", it.toString())
            }, {
                Log.d("MM_SEND_EQUIP_RFID", it.message.toString())
            })
        sendEquipRFIDDisposable?.let {
            compositeDisposable.add(it)
        }

    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setEquipRFIDSendedById(id: Long) {

        Log.d("MM_SEND_EQUIP_RFID", id.toString())

        viewModelScope.launch {
            db.equipDao().setEquipRFIDSendedById(id)
        }
    }

    /**
     * Сохранение геопозиции устройства в БД
     * @param userLocation - геопозиция устройства
     */
    fun insertUserLocation(userLocation: UserLocation) {

        Log.d("MM_INSERT_USER_LOCATION", userLocation.toString())

        viewModelScope.launch {
            db.userLocationDao().insertUserLocation(userLocation)
        }
    }

    /**
     * Отправка данных геопозиции на сервер
     * @param userLocation - геопозиция устройства
     */
    fun sendUserLocation(userLocation: UserLocation) {

        sendUserLocationDisposable?.let {
            compositeDisposable.remove(it)
        }

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_ID] = userLocation.id.toString()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_ACCURACY] = userLocation.accuracy.toString()
        map[QUERY_PARAM_ALTITUDE] = userLocation.altitude.toString()
        map[QUERY_PARAM_BEARING] = userLocation.bearing.toString()
        map[QUERY_PARAM_ELAPSED_REALTIME_NANOS] = userLocation.elapsedRealtimeNanos.toString()
        map[QUERY_PARAM_LATITUDE] = userLocation.latitude.toString()
        map[QUERY_PARAM_LONGITUDE] = userLocation.longitude.toString()
        map[QUERY_PARAM_PROVIDER] = userLocation.provider.toString()
        map[QUERY_PARAM_SPEED] = userLocation.speed.toString()
        map[QUERY_PARAM_TIME] = userLocation.time.toString()

        sendUserLocationDisposable = ApiFactory.apiService.addLocation(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_USER_LOCATION", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setUserLocationSendedById(it)
                    }
                }
                Log.d("MM_SEND_USER_LOCATION", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_USER_LOCATION", it.message.toString())
                })
        sendUserLocationDisposable?.let {
            compositeDisposable.add(it)
        }
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setUserLocationSendedById(id: Long) {

        Log.d("MM_SET_LOCATION_SEND", id.toString())

        viewModelScope.launch {
            db.userLocationDao().setUserLocationSendedById(id)
        }
    }
}