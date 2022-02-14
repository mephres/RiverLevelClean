package com.intas.metrolog.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intas.metrolog.api.ApiFactory
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ACCURACY
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ALTITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_BEARING
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_CATEGORY_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_COMMENT
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_COMPLETED
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_COMPLETED_USER_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DATA
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DATETIME
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DATE_END
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DATE_START
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DATE_TIME_START_TIMER
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DISCIPLINE_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_DURATION_TIMER
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ELAPSED_REALTIME_NANOS
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_EQUIP_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_EQUIP_RFID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_FACT_DATE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LATITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LONGITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_MESSAGE_TEXT
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_OPERATION_TYPE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_OP_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_PHOTO
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_PRIORITY
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_PROVIDER
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_REQUEST_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_REQUEST_PHOTO
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_SPEED
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_STATUS_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_SUB_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_SUB_MAN_HOUR
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_TIME
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_TO_USER_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_TYPE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_USER_ID
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.OperControlItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.CANCELED
import com.intas.metrolog.pojo.event.event_status.EventStatus.Companion.NEW
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.pojo.request.RequestPhoto
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.metrolog.ui.requests.filter.RequestFilter
import com.intas.metrolog.util.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    private var getEquipDisposable: Disposable? = null
    private var getRequestDisposable: Disposable? = null
    private var getEventDisposable: Disposable? = null
    private var loadMessageDisposable: Disposable? = null

    val notSendedUserLocationList = db.userLocationDao().getNotSendedUserLocationList()
    val notSendedEquipDocumentList = db.equipDocumentDao().getNotSendedEquipDocumentList()
    val notSendedEquipList = db.equipDao().getEquipNotSended()
    val notSendedEventList = db.eventDao().getNotSendedEventList()
    val notSendedEventOperationList = db.eventOperationDao().getNotSendedEventOperationList()
    val getNotSendedEventOperationControlList =
        db.operControlDao().getNotSendedEventOperationControlList()
    val getNotSendedEventPhotoList = db.eventPhotoDao().getNotSendedEventPhotoList()
    val getNotSendedRequestList = db.requestDao().getNotSendedRequestList()
    val getNotSendedEquipInfoList = db.equipInfoDao().getNotSendedEquipInfoList()
    val getNotSendedRequestPhotoList = db.requestPhotoDao().getNotSendedRequestPhotoList()
    val chatMessageLastId = db.chatMessageDao().getChatMessageLastId()
    val newChatMessageCount = db.chatMessageDao().getNewChatMessageCount(Util.authUser?.userId ?: 0)
    val notSendedChatMessageList = db.chatMessageDao().getNotSendedMessageList().distinctUntilChanged()


    val onErrorMessage = SingleLiveEvent<String>()

    private var _requestFilter = MutableLiveData<RequestFilter>()
    val requestFilter: LiveData<RequestFilter>
        get() = _requestFilter

    init {
        getEventStatus()
        getUserList()
        getRequestList()
        getEquip()
        getRequestStatus()
        getDiscipline()
        getEventOperation()
        getDocumentType()
        getEquipInfoPriority()
        getEventComment()
        getEvent()
        initMessageFirstId()
    }

    fun addRequestFilter(requestFilter: RequestFilter) {
        _requestFilter.value = requestFilter
    }

    /**
     * Сохранение списка пользователей в БД
     * @param userList - списка пользователей
     */
    private fun insertUserList(userList: List<UserItem>) {

        Log.d("MM_INSERT_USERS", userList.toString())

        viewModelScope.launch {
            db.userDao().insertUserList(userList)
        }
    }

    /**
     * Получение списка пользователей
     */
    private fun getUserList() {
        Util.authUser?.userId?.let {

            val disposable = ApiFactory.apiService.getUserList(it)
                .subscribeOn(Schedulers.io())
                .repeatWhen { completed ->
                    completed.delay(10, TimeUnit.MINUTES)
                }
                .retryWhen { f: Flowable<Throwable?> ->
                    f.take(600).delay(1, TimeUnit.MINUTES)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.list?.let { userList ->
                        insertUserList(userList)
                    }
                }, {
                    it.printStackTrace()
                })
            compositeDisposable.add(disposable)
        }
    }

    /**
     * Сохранение списка заявок в БД
     * @param requestList - список заявок
     */
    private fun insertRequestList(requestList: List<RequestItem>) {

        Log.d("MM_INSERT_REQUEST", requestList.toString())

        viewModelScope.launch {
            db.requestDao().insertRequestList(requestList)

            requestList.forEach {
                fillRequestEquipInfo(it)
            }
        }
    }

    /**
     * Установка наименования оборудования, места установки, тэга оборудования для заявки
     * Для поиска заявок по введенному тексту в поле поиска
     *
     * @param requestItem заявка, объект класса [RequestItem]
     */
    private fun fillRequestEquipInfo(requestItem: RequestItem) {

        val equipId = try {
            requestItem.equipId?.toLong()
        } catch (e: Exception) {
            -1
        }

        if (equipId != null) {
            if (equipId < 0) {
                return
            }

            val equipItem = db.equipDao().getEquipItemById(equipId) ?: return
            val equipInfo = String.format(
                "%s [%s] - %s",
                equipItem.equipName,
                equipItem.equipTag,
                equipItem.mestUstan
            )

            db.requestDao().updateRequestEquipInfo(requestItem.id, equipInfo)
        }
    }

    /**
     * Получение списка заявок
     */
    fun getRequestList() {
        Util.authUser?.userId?.let {

            getRequestDisposable?.let {
                compositeDisposable.remove(it)
            }

            getRequestDisposable = ApiFactory.apiService.getRequestList(it)
                .subscribeOn(Schedulers.io())
                .repeatWhen { completed ->
                    completed.delay(10, TimeUnit.MINUTES)
                }
                .retryWhen { f: Flowable<Throwable?> ->
                    f.take(600).delay(1, TimeUnit.MINUTES)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.list?.let { requestList ->
                        insertRequestList(requestList)
                    }
                }, {
                    onErrorMessage.postValue(
                        "При получении списка заявок с сервера произошла ошибка - " +
                                "${it.message}"
                    )
                    it.printStackTrace()
                })
            getRequestDisposable?.let {
                compositeDisposable.add(it)
            }
        }
    }

    /**
     * Сохранение списка оборудования в БД
     * @param equipList - список оборудования
     */
    private fun insertEquipList(equipList: List<EquipItem>) {

        Log.d("MM_INSERT_EQUIP", equipList.toString())

        viewModelScope.launch {
            val tempNotSendedEquipList = notSendedEquipList.value
            db.equipDao().insertEquipList(equipList)
            tempNotSendedEquipList?.let {
                if (!tempNotSendedEquipList.isNullOrEmpty()) {
                    db.equipDao().insertEquipList(it)
                }
            }

            for (equip in equipList) {
                equip.equipInfoList?.let { eil ->
                    if (eil.isNotEmpty()) {
                        val list = eil.filter {
                            val info = db.equipInfoDao().getEquipInfo(it.id)
                            info == null
                        }.map {
                            it.equipId = equip.equipId
                            it
                        }

                        insertEquipInfoList(list)
                    }
                }
            }
        }
    }

    /**
     * Сохранение информации по оборудованию в БД
     * @param equipInfoList - список информации по оборудованию
     */
    private fun insertEquipInfoList(equipInfoList: List<EquipInfo>) {

        Log.d("MM_INSERT_EQUIP_INFO", equipInfoList.toString())

        viewModelScope.launch {
            db.equipInfoDao().insertEquipInfoList(equipInfoList)
        }
    }

    /**
     * Получение списка оборудования
     */
    fun getEquip() {
        Util.authUser?.userId?.let {

            getEquipDisposable?.let {
                compositeDisposable.remove(it)
            }

            getEquipDisposable = ApiFactory.apiService.getEquip(it)
                .subscribeOn(Schedulers.io())
                .repeatWhen { completed ->
                    completed.delay(10, TimeUnit.MINUTES)
                }
                .retryWhen { f: Flowable<Throwable?> ->
                    f.take(600).delay(1, TimeUnit.MINUTES)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.list?.let { equipList ->
                        insertEquipList(equipList)
                    }
                }, {
                    onErrorMessage.postValue(
                        "При получении списка оборудования с сервера произошла ошибка - " +
                                "${it.message}"
                    )
                    it.printStackTrace()
                })
            getEquipDisposable?.let {
                compositeDisposable.add(it)
            }
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
     * @param operationTypeList - список операций
     */
    private fun insertEventOperationTypeList(operationTypeList: List<EventOperationTypeItem>) {

        Log.d("MM_INSERT_OPERATION", operationTypeList.toString())

        viewModelScope.launch {
            db.eventOperationTypeDao().insertEventOperationTypeList(operationTypeList)
        }
    }

    /**
     * Получение списка операций мероприятий
     */
    private fun getEventOperation() {
        Util.authUser?.userId?.let {
            val disposable = ApiFactory.apiService.getEventOperationType(it)
                .retryWhen { f: Flowable<Throwable?> ->
                    f.delay(1, TimeUnit.MINUTES)
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.list?.let { operationTypeList ->
                        insertEventOperationTypeList(operationTypeList)
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
                    it.list?.let { disciplineList ->
                        insertDisciplineList(disciplineList)

                        val list = mutableListOf<Int>()
                        for (disc in disciplineList) {
                            list.add(disc.id)
                        }
                        AppPreferences.requestFilterDiscList = list as ArrayList<Int>
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
                    it.list?.let { statusList ->
                        insertRequestStatusList(statusList)

                        val list = mutableListOf<Int>()
                        for (disc in statusList) {
                            list.add(disc.id)
                        }
                        AppPreferences.requestFilterStatusList = list as ArrayList<Int>
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

        Util.equipLocationQueue.addLast(equip.equipId)

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_EQUIP_ID] = equip.equipId.toString()
        map[QUERY_PARAM_LATITUDE] = equip.latitude.toString()
        map[QUERY_PARAM_LONGITUDE] = equip.longitude.toString()

        val sendEquipLocationDisposable = ApiFactory.apiService.updateEquipGeo(map)
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
        compositeDisposable.add(sendEquipLocationDisposable)
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setEquipLocationSendedById(id: Long) {

        Log.d("MM_EQUIP_LOCATION_SEND", id.toString())

        if (Util.equipRfidQueue.size > 50) {
            Util.equipRfidQueue.removeFirst()
        }

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

        Util.equipRfidQueue.addLast(equip.equipId)

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_EQUIP_ID] = equip.equipId.toString()
        map[QUERY_PARAM_EQUIP_RFID] = equip.equipRFID.toString()

        val sendEquipRFIDDisposable = ApiFactory.apiService.updateEquipRFID(map)
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
        compositeDisposable.add(sendEquipRFIDDisposable)
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setEquipRFIDSendedById(id: Long) {

        Log.d("MM_SEND_EQUIP_RFID", id.toString())

        if (Util.equipRfidQueue.size > 50) {
            Util.equipRfidQueue.removeFirst()
        }

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

        if (Util.userLocationQueue.contains(userLocation.id)) {
            return
        }
        Util.userLocationQueue.addLast(userLocation.id)

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

        val sendUserLocationDisposable = ApiFactory.apiService.addLocation(map)
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
        compositeDisposable.add(sendUserLocationDisposable)
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setUserLocationSendedById(id: Long) {

        Log.d("MM_SET_LOCATION_SEND", id.toString())

        if (Util.userLocationQueue.size > 500) {
            Util.userLocationQueue.removeFirst()
        }

        viewModelScope.launch {
            db.userLocationDao().setUserLocationSendedById(id)
        }
    }

    /**
     * Отправка документа оборудования на сервер
     * @param equipDocument - документ оборудования
     */
    fun sendEquipDocument(equipDocument: EquipDocument) {

        var multipartFile: MultipartBody.Part? = null

        equipDocument.filePath?.let {

            val file = File(it)

            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            multipartFile = MultipartBody.Part.createFormData("file", file.name, requestFile)
        }

        val multipartId = equipDocument.id.toString().toRequestBody(MultipartBody.FORM)
        val multipartUserId = Util.authUser?.userId.toString().toRequestBody(MultipartBody.FORM)
        val multipartEquipId = equipDocument.equipId.toString().toRequestBody(MultipartBody.FORM)
        val multipartType =
            equipDocument.documentTypeId.toString().toRequestBody(MultipartBody.FORM)
        val multipartDoc = equipDocument.filename.toString().toRequestBody(MultipartBody.FORM)

        val sendEquipDocumentDisposable = ApiFactory.apiService.addEquipDocument(
            file = multipartFile,
            id = multipartId,
            userId = multipartUserId,
            equipId = multipartEquipId,
            type = multipartType,
            doc = multipartDoc
        )
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EQUIP_DOCUMENT", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setEquipDocumentSendedById(it)
                    }
                }
                Log.d("MM_SEND_EQUIP_DOCUMENT", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_EQUIP_DOCUMENT", it.message.toString())
                })

        compositeDisposable.add(sendEquipDocumentDisposable)
    }

    /**
     * Установка признака отсылки данных на сервер
     * @param id - идентификатор записи
     */
    private fun setEquipDocumentSendedById(id: Long) {

        Log.d("MM_SET_EQUIP_DOC_SEND", id.toString())

        viewModelScope.launch {
            db.equipDocumentDao().setEquipDocumentSendedById(id)
        }
    }

    fun getEvent() {
        Util.authUser?.userId?.let {

            getEventDisposable?.let {
                compositeDisposable.remove(it)
            }

            val month =
                DateTimeUtil.getDateTimeFromMili(DateTimeUtil.getUnixDateTimeNow(), "MM").toInt()
            val year =
                DateTimeUtil.getDateTimeFromMili(DateTimeUtil.getUnixDateTimeNow(), "YYYY").toInt()

            getEventDisposable = ApiFactory.apiService.getEventList(it, month, year)
                .subscribeOn(Schedulers.io())
                .repeatWhen { completed ->
                    completed.delay(5, TimeUnit.MINUTES)
                }
                .retryWhen { f: Flowable<Throwable?> ->
                    f.take(600).delay(1, TimeUnit.MINUTES)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.list?.let { eventList ->
                        insertEventList(eventList)
                        Log.d("eventList", eventList.toString())
                    }
                }, {
                    onErrorMessage.postValue(
                        "При получении списка мероприятий с сервера произошла ошибка - " +
                                "${it.message}"
                    )
                    it.printStackTrace()
                })
            getEventDisposable?.let {
                compositeDisposable.add(it)
            }
        }
    }

    private fun insertEventList(eventList: List<EventItem>) {
        viewModelScope.launch {

            Journal.insertJournal("MainViewModel->insertEventList->list", list = eventList)
            val list = eventList.filter {
                val event = db.eventDao().getEvent(it.opId)
                (event?.isSended == 0 || (event?.status ?: 0 > NEW && event?.status ?: 0 != CANCELED)) == false
            }.map {
                it.needPhotoFix = it.operation?.any {
                    it.needPhotoFix == 1
                } == true
                it.equipId = it.equip?.equipId
                it.equipRfid = it.equip?.equipRFID
                it.equipName = it.equip?.equipName
                it
            }
            Journal.insertJournal("MainViewModel->insertEventList->list_for_insert", list = list)
            db.eventDao().insertEventList(list)

            list.forEach { event ->

                Util.safeLet(event.operation, event.equipId) { eol, equipId ->
                    insertEventOperationList(eol, event.opId, equipId)
                }
            }
        }
    }

    private fun insertEventOperationList(
        eventEventOperationList: List<EventOperationItem>,
        eventId: Long,
        equipId: Long
    ) {
        viewModelScope.launch {
            val tempNotSendedEventOperationList = notSendedEventOperationList.value

            db.eventOperationDao().insertEventOperationList(eventEventOperationList.map {
                it.opId = eventId
                it
            })

            tempNotSendedEventOperationList?.let {
                if (!tempNotSendedEventOperationList.isNullOrEmpty()) {
                    db.eventOperationDao().insertEventOperationList(it)
                }
            }

            eventEventOperationList.forEach { eventOperation ->
                eventOperation.operControl?.let {
                    it.eventId = eventId
                    it.opId = eventOperation.subId
                    it.equipId = equipId

                    eventOperation.hasOperationControl = true
                    db.eventOperationDao().updateEventOperation(eventOperation)

                    insertOperControl(it)
                }
            }

            updateEventCheckListSize(eventId)
        }
    }

    private fun updateEventCheckListSize(eventId: Long) {
        viewModelScope.launch {
            val event = db.eventDao().getEvent(eventId)

            event?.apply {
                operationListSize =
                    db.eventOperationDao().getOperationListSize(opId)
                db.eventDao().updateEvent(this)
            }
        }
    }

    private fun insertOperControl(operControl: OperControlItem) {
        viewModelScope.launch {

            val tempOperControl =
                db.operControlDao().getEventOperationControlById(operControl.id ?: 0)
            tempOperControl?.let {
                if (it.isSended == 0) {
                    return@launch
                }
            }

            db.operControlDao().insertOperControl(operControl)

            operControl.fieldList?.let { fieldList ->
                fieldList.forEach { field ->
                    field.eventId = operControl.eventId
                    field.operationId = operControl.opId
                    field.classCode = operControl.classCode

                    val fieldId = db.fieldDao().insertField(field)
                    field.dictData?.let { dictData ->
                        if (!dictData.isNullOrEmpty()) {
                            for ((code, value) in dictData) {
                                val dictDataObject =
                                    FieldDictData(fieldId = fieldId, code = code, value = value)
                                db.fieldDictDataDao()
                                    .insertFieldDictData(dictDataObject) // запись в базу способа измерения
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Отправка мероприятия на сервер
     * @param event - мероприятие, объект класса [EventItem]
     */
    fun sendEvent(event: EventItem) {

        Util.eventQueue.addLast(event.opId)

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_OP_ID] = event.opId.toString()
        map[QUERY_PARAM_FACT_DATE] = event.factDate.toString()
        map[QUERY_PARAM_STATUS_ID] = event.status.toString()
        map[QUERY_PARAM_DURATION_TIMER] = event.durationTimer.toString()
        map[QUERY_PARAM_DATE_TIME_START_TIMER] = event.dateTimeStartTimer.toString()
        map[QUERY_PARAM_COMMENT] = event.comment.toString()

        val sendEventDisposable = ApiFactory.apiService.updateEvent(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EVENT", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setEventSendedById(it)
                    }
                }
                Log.d("MM_SEND_EVENT", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_EVENT", it.message.toString())
                })
        compositeDisposable.add(sendEventDisposable)
    }

    private fun setEventSendedById(id: Long) {
        Log.d("MM_SET_EVENT_SEND", id.toString())

        if (Util.eventQueue.count() > 100) {
            Util.eventQueue.removeFirst()
        }

        viewModelScope.launch {
            db.eventDao().setEventSendedById(id)
        }
    }

    /**
     * Отправка операции комплексного мероприятия на сервер
     * @param eventOperation - операция комплексного мероприятия, объект класса [EventOperationItem]
     */
    @SuppressLint("LongLogTag")
    fun sendComplexEventOperation(eventOperation: EventOperationItem) {

        Util.eventOperationQueue.addLast(eventOperation.subId)

        val opId = eventOperation.subId

        var status = EventStatus.CANCELED
        var comment = "Отклонено"
        if (eventOperation.completed == 1) {
            status = EventStatus.COMPLETED
            comment = "Выполнено"
        }

        val factDate = eventOperation.dateEnd
        val durationTimer = 0
        val dateTimeStartTimer = 0

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_OP_ID] = opId.toString()
        map[QUERY_PARAM_FACT_DATE] = factDate.toString()
        map[QUERY_PARAM_STATUS_ID] = status.toString()
        map[QUERY_PARAM_DURATION_TIMER] = durationTimer.toString()
        map[QUERY_PARAM_DATE_TIME_START_TIMER] = dateTimeStartTimer.toString()
        map[QUERY_PARAM_COMMENT] = comment

        val sendComplexEventOperationDisposable = ApiFactory.apiService.updateEvent(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_COMPLEX_EVENT_OPERATION", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestError != null) {
                    if (it.requestError.code.equals("406") && it.requestError.message.equals("Мероприятие уже проводилось", true)) {
                        setEventOperationSendedById(eventOperation.subId)
                    }
                }
                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setEventOperationSendedById(it)
                    }
                }
                Log.d("MM_SEND_COMPLEX_EVENT_OPERATION", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_COMPLEX_EVENT_OPERATION", it.message.toString())
                })
        compositeDisposable.add(sendComplexEventOperationDisposable)
    }

    /**
     * Отправка операции мероприятия на сервер
     * @param eventOperation - операция мероприятия, объект класса [EventOperationItem]
     */
    fun sendEventOperation(eventOperation: EventOperationItem) {

        Util.eventOperationQueue.addLast(eventOperation.subId)

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_OP_ID] = eventOperation.opId.toString()
        map[QUERY_PARAM_SUB_ID] = eventOperation.subId.toString()
        map[QUERY_PARAM_SUB_MAN_HOUR] = eventOperation.subManhour.toString()
        map[QUERY_PARAM_DATE_START] = eventOperation.dateStart.toString()
        map[QUERY_PARAM_DATE_END] = eventOperation.dateEnd.toString()
        map[QUERY_PARAM_COMPLETED] = eventOperation.completed.toString()
        map[QUERY_PARAM_COMPLETED_USER_ID] = eventOperation.completedUserId.toString()

        val sendEventOperationDisposable = ApiFactory.apiService.addOperation(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EVENT_OPERATION", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setEventOperationSendedById(it)
                    }
                }
                Log.d("MM_SEND_EVENT_OPERATION", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_EVENT_OPERATION", it.message.toString())
                })
        compositeDisposable.add(sendEventOperationDisposable)
    }

    @SuppressLint("LongLogTag")
    private fun setEventOperationSendedById(id: Long) {
        Log.d("MM_SET_EVENT_OPERATION_SEND", id.toString())

        if (Util.eventOperationQueue.count() > 100) {
            Util.eventOperationQueue.removeFirst()
        }

        viewModelScope.launch {
            db.eventOperationDao().setEventOperationSendedById(id)
        }
    }

    /**
     * Отправка операционного контроля операции мероприятия на сервер
     * @param operControl - операционный контроль операции мероприятия, объект класса [OperControlItem]
     */
    fun sendEventOperationControl(operControl: OperControlItem) {

        Util.eventOperationControlQueue.addLast(operControl.id)

        val jsonObject =
            getOperControlJSON(operControl.eventId, operControl.opId, operControl.equipId)

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map.put(QUERY_PARAM_DATA, jsonObject["data"].toString())
        map.put(QUERY_PARAM_EQUIP_ID, operControl.equipId.toString())
        map.put(QUERY_PARAM_OP_ID, jsonObject["opId"].toString())

        // отправка полей операционного контроля на сервер ЦНО
        val sendOperControlDisposable = ApiFactory.apiService.addOperControlFact(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_OPER_CONTROL", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.let {
                        setEventOperControlSendedById(it)
                    }
                }
                Log.d("MM_SEND_OPER_CONTROL", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_OPER_CONTROL", it.message.toString())
                })
        compositeDisposable.add(sendOperControlDisposable)
    }

    @SuppressLint("LongLogTag")
    private fun setEventOperControlSendedById(ids: String) {
        Log.d("MM_SET_OPER_CONTROL_SEND", ids)

        viewModelScope.launch {

            // список id полученных записей
            val idArray = ids.replace("[", "").replace("]", "").split(",")
            idArray.forEach { id ->
                val fieldId = id.toLongOrNull()
                fieldId?.let {
                    val field = db.fieldDao().getFieldById(fieldId)
                    field?.let {
                        val opId: Long = field.operationId
                        val eventId: Long = field.eventId

                        field.isSended = 1

                        db.operControlDao().setEventOperationControlSendedById(eventId, opId)
                        db.fieldDao().updateField(field)
                    }
                }
            }
        }
    }

    /**
     * Формирование JSON структуры операционного контроля
     * @param eventId - идентификатор мероприятия
     * @param operationId - идентификатор операции меропрития
     * @param equipId - идентификатор оборудования
     * @return структура операционного контроля в формате JSON
     */
    @SuppressLint("LongLogTag")
    private fun getOperControlJSON(eventId: Long, operationId: Long, equipId: Long): JSONObject {

        val fieldList = db.fieldDao().getNotSendedEventOperationFieldList(eventId, operationId)

        val resultJSON = JSONObject()
        var opId = 0L

        val map = mutableMapOf<String, List<FieldItem>>()

        fieldList.forEach { field ->
            val classCode = field.classCode
            classCode?.let {
                var list = map[classCode]?.toMutableList()
                if (list == null) {
                    list = mutableListOf<FieldItem>()
                }

                list.add(field)
                map[classCode] = list
            }
        }
        try {
            // формирование JSON для отправки
            val dataJSON = JSONObject()
            val classCodes = map.keys
            classCodes.forEach { classCode ->
                val classCodeJSON = JSONObject()
                var operationJSONArray = JSONObject()
                var currentOperationId = 0L
                val fields = map[classCode]
                fields?.let {
                    fields.forEach { field ->
                        val operationJSON = JSONObject()

                        opId = field.eventId

                        operationJSON.put("id", field.id)
                        operationJSON.put("code", field.code)
                        operationJSON.put("type", field.type)
                        operationJSON.put("value", field.value)

                        val preCurOperationId = currentOperationId
                        currentOperationId = field.operationId
                        field.code?.let { fieldCode ->
                            if (preCurOperationId == currentOperationId || preCurOperationId == 0L) {
                                operationJSONArray.put(fieldCode, operationJSON)
                            } else {
                                operationJSONArray = JSONObject()
                                operationJSONArray.put(fieldCode, operationJSON)
                            }
                            classCodeJSON.put(currentOperationId.toString(), operationJSONArray)
                        }
                    }
                    dataJSON.put(classCode, classCodeJSON)
                }
            }

            resultJSON.put("data", dataJSON)
            resultJSON.put("userId", Util.authUser?.userId)
            resultJSON.put("equipId", equipId)
            resultJSON.put("opId", opId)

            Log.d("MM_GET_OPER_CONTROL_JSON", resultJSON.toString())
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return resultJSON
    }

    /**
     * Отправка изображения мероприятия на сервер
     * @param eventPhoto - изображение мероприятия, объект класса [EventPhotoItem]
     */
    fun sendEventPhoto(eventPhoto: EventPhotoItem) {

        eventPhoto.id?.let {
            Util.eventPhotoQueue.addLast(it)
        }

        val screen = ImageUtil.getBase64ScreenFromUri(
            getApplication<Application>().applicationContext,
            eventPhoto.photoUri
        )

        val map = mutableMapOf<String, String>()

        map[QUERY_PARAM_ID] = eventPhoto.id.toString()
        map[QUERY_PARAM_OP_ID] = eventPhoto.opId.toString()
        screen?.let {
            map[QUERY_PARAM_PHOTO] = screen
        }
        map[QUERY_PARAM_DATETIME] = eventPhoto.datetime.toString()
        map[QUERY_PARAM_USER_ID] = eventPhoto.userId.toString()

        val sendEventPhotoDisposable = ApiFactory.apiService.addEventPhoto(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EVENT_PHOTO", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                if (it.requestSuccess != null) {

                    it.requestSuccess?.id?.toLong()?.let {
                        setEventPhotoSendedById(it)
                    }
                }
                Log.d("MM_SEND_EVENT_PHOTO", it.toString())
            },
                {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_EVENT_PHOTO", it.message.toString())
                })
        compositeDisposable.add(sendEventPhotoDisposable)
    }

    @SuppressLint("LongLogTag")
    private fun setEventPhotoSendedById(id: Long) {

        if (Util.eventPhotoQueue.count() > 100) {
            Util.eventPhotoQueue.removeFirst()
        }

        Log.d("MM_SET_EVENT_PHOTO_SEND", id.toString())

        viewModelScope.launch {
            db.eventPhotoDao().setEventPhotoSendedById(id)
        }
    }

    /**
     * Отправка на сервер ЦНО неотправленных заявок
     *
     * @param request заявка, экземпляр класса [RequestItem]
     */
    fun sendRequest(request: RequestItem) {

        request.id.let {
            Util.requestQueue.addLast(it)
        }

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_ID] = request.id.toString()
        map[QUERY_PARAM_EQUIP_ID] = request.equipId.toString()
        map[QUERY_PARAM_EQUIP_RFID] = request.rfid.toString()
        map[QUERY_PARAM_COMMENT] = request.comment.toString()
        map[QUERY_PARAM_DATETIME] = request.creationDate.toString()
        map[QUERY_PARAM_TYPE] = request.typeRequest.toString()
        map[QUERY_PARAM_CATEGORY_ID] = request.categoryId.toString()
        map[QUERY_PARAM_DISCIPLINE_ID] = request.discipline.toString()
        map[QUERY_PARAM_OPERATION_TYPE] = request.operationType.toString()

        val sendRequestDisposable = ApiFactory.apiService.addRequest(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_REQUEST", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.requestSuccess?.let {
                    Util.safeLet(it.id, it.serverId) { id, serverId ->
                        setRequestSendedById(id.toLong(), serverId.toLong())
                    }
                }
                Log.d("MM_SEND_REQUEST", it.toString())
            }, {
                Log.d("MM_SEND_REQUEST", it.message.toString())
            })

        compositeDisposable.add(sendRequestDisposable)
    }

    /**
     * Установка признака отсылки заявки на сервер
     * @param id - идентификатор записи
     */
    private fun setRequestSendedById(id: Long, serverId: Long) {

        if (Util.requestQueue.count() > 100) {
            Util.requestQueue.removeFirst()
        }

        Log.d("MM_SET_REQUEST_SEND", id.toString())

        viewModelScope.launch {
            try {
                db.requestDao().setRequestSendedById(id, serverId)
            } catch (e: SQLiteConstraintException) {
                db.requestDao().deleteRequestById(id)
            }
            db.requestPhotoDao().updateRequestPhoto(id, serverId)
        }
    }

    /**
     * Отправка на сервер ЦНО фото к заявкам
     *
     * @param requestPhoto фото к заявке, экземпляр класса [RequestPhoto]
     */
    fun sendRequestPhoto(requestPhoto: RequestPhoto) {

        requestPhoto.id.let {
            Util.requestPhoto.addLast(it)
        }

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_ID] = requestPhoto.id.toString()
        map[QUERY_PARAM_REQUEST_ID] = requestPhoto.requestId.toString()
        map[QUERY_PARAM_REQUEST_PHOTO] = requestPhoto.photo.toString()
        map[QUERY_PARAM_DATETIME] = requestPhoto.dateTime.toString()

        val sendRequestPhotoDisposable = ApiFactory.apiService.addRequestPhoto(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_REQUEST_PHOTO", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.requestSuccess?.let {
                    Util.safeLet(it.id, it.serverId) { id, serverId ->
                        setRequestPhotoSendedById(id.toLong(), serverId.toLong())
                    }
                }
                Log.d("MM_SEND_REQUEST_PHOTO", it.toString())
            }, {
                Log.d("MM_SEND_REQUEST_PHOTO", it.message.toString())
            })
        compositeDisposable.add(sendRequestPhotoDisposable)
    }

    /**
     * Установка признака отсылки фото к заявке на сервер
     * @param id - идентификатор записи
     */
    private fun setRequestPhotoSendedById(id: Long, serverId: Long) {

        Log.d("MM_SET_REQUEST_PH_SEND", id.toString())

        if (Util.requestPhoto.count() > 100) {
            Util.requestPhoto.removeFirst()
        }

        viewModelScope.launch {
            db.requestPhotoDao().setRequestPhotoSendedById(id, serverId)
        }
    }

    /**
     * Отправка на сервер ЦНО комментария к оборудованию
     *
     * @param equipInfo комментарий к оборудованию, экземпляр класса [EquipInfo]
     */
    fun sendEquipInfo(equipInfo: EquipInfo) {

        equipInfo.id.let {
            Util.equipInfoQueue.addLast(it)
        }

        val map = mutableMapOf<String, String>()
        map[QUERY_PARAM_USER_ID] = (Util.authUser?.userId).toString()
        map[QUERY_PARAM_ID] = equipInfo.id.toString()
        map[QUERY_PARAM_EQUIP_ID] = equipInfo.equipId.toString()
        map[QUERY_PARAM_COMMENT] = equipInfo.text.toString()
        map[QUERY_PARAM_PRIORITY] = equipInfo.priority.toString()
        map[QUERY_PARAM_DATETIME] = equipInfo.dateTime.toString()

        val sendEquipInfoDisposable = ApiFactory.apiService.addEquipInfo(map)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(600).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                FirebaseCrashlytics.getInstance().recordException(it)
                Log.d("MM_SEND_EQUIP_INFO", it.message.toString())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.requestSuccess != null) {

                    it.requestSuccess?.let {
                        Util.safeLet(it.id, it.serverId) { id, serverId ->
                            setEquipInfoSendedById(id.toLong(), serverId.toLong())
                        }
                    }
                }
                Log.d("MM_SEND_EQUIP_INFO", it.toString())
            }, {
                Log.d("MM_SEND_EQUIP_INFO", it.message.toString())
            })

        compositeDisposable.add(sendEquipInfoDisposable)
    }

    /**
     * Установка признака отсылки фото к заявке на сервер
     * @param id - идентификатор записи
     */
    private fun setEquipInfoSendedById(id: Long, serverId: Long) {

        if (Util.equipInfoQueue.count() > 100) {
            Util.equipInfoQueue.removeFirst()
        }

        Log.d("MM_SET_EQUIP_INFO_SEND", id.toString())

        viewModelScope.launch {
            db.equipInfoDao().setEquipInfoSendedById(id, serverId)
        }
    }

    private fun getEventStatus() {

        val eventStatusList = mutableListOf<EventStatus>().apply {
            add(EventStatus(0, "Новое мероприятие"))
            add(EventStatus(1, "Выполняется"))
            add(EventStatus(2, "Остановлено"))
            add(EventStatus(3, "Выполнено"))
            add(EventStatus(4, "Отменено"))
        }

        viewModelScope.launch {
            db.eventStatusDao().insertEventStatusList(eventStatusList)
        }
    }

    fun loadMessageList(messageLastId: Int) {
        loadMessageDisposable?.let {
            compositeDisposable.remove(it)
        }

        loadMessageDisposable =
            ApiFactory.apiService.getChatMessage(
                userId = Util.authUser?.userId ?: 0,
                id = messageLastId
            )
                .subscribeOn(Schedulers.io())
                .repeatWhen {
                    it.delay(60, TimeUnit.SECONDS)
                }
                .retryWhen { f: Flowable<Throwable?> ->
                    f.take(600).delay(1, TimeUnit.MINUTES)
                }
                .doOnError {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_EQUIP_INFO", it.message.toString())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.list?.let {
                        insertMessageList(it)
                    }
                    it.requestError?.let {
                        Log.d("MM_LOAD_MESSAGES", it.message.toString())
                    }
                }, {
                    it.printStackTrace()
                    Log.d("MM_LOAD_MESSAGES", it.message.toString())
                    FirebaseCrashlytics.getInstance().recordException(it)
                })

        loadMessageDisposable?.let {
            compositeDisposable.add(it)
        }
    }

    private fun insertMessageList(list: List<MessageItem>) {
        viewModelScope.launch {

            Log.d("MM_INSERT_MESSAGES", list.size.toString())

            db.chatMessageDao().insertMessageList(list)
        }
    }

    fun sendChatMessage(chatMessage: MessageItem) {

        chatMessage.id?.let {
            Util.chatMessageQueue.addLast(it)
        }

        val messagesMap = mutableMapOf<String, String>()
        messagesMap[QUERY_PARAM_USER_ID] = Util.authUser?.userId.toString()
        messagesMap[QUERY_PARAM_ID] = chatMessage.id.toString()
        messagesMap[QUERY_PARAM_MESSAGE_TEXT] = chatMessage.message.toString()
        messagesMap[QUERY_PARAM_TO_USER_ID] = chatMessage.companionUserId.toString()
        messagesMap[QUERY_PARAM_DATETIME] = chatMessage.dateTime.toString()

        val disposable = ApiFactory.apiService.addChatMessage(messagesMap)
            .retryWhen { f: Flowable<Throwable?> ->
                f.take(500).delay(1, TimeUnit.MINUTES)
            }
            .doOnError {
                Log.d("MM_UPLOAD_CHAT_MESSAGES", it.message.toString())
                FirebaseCrashlytics.getInstance().recordException(it)
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                it.requestSuccess?.let {
                    Util.safeLet(it.id, it.serverId) { id, serverId ->
                        setChatMessageSended(id.toInt(), serverId.toInt())
                    }
                }

                it.requestError?.let {
                    Log.d("MM_SEND_CHAT_MESSAGES", it.message.toString())
                }
                Log.d("MM_SEND_CHAT_MESSAGES", it.toString())
            },
                {
                    it.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(it)
                    Log.d("MM_SEND_CHAT_MESSAGES", it.message.toString())
                })
        compositeDisposable.add(disposable)
    }

    /**
     * Установка признака успешной отсылки сообщения чата на сервер
     * @param id - идентификатор записи сообщения на стороне мобильного устройства
     * @param serverId - идентификатор записи на стороне сервера
     */
    private fun setChatMessageSended(id: Int, serverId: Int) {
        if (Util.chatMessageQueue.count() > 100) {
            Util.chatMessageQueue.removeFirst()
        }

        viewModelScope.launch {
            try {
                db.chatMessageDao().setMessageSentBy(id = id, serverId = serverId)
            } catch (e: SQLiteConstraintException) {
                e.printStackTrace()
                if (e.localizedMessage.contains("UNIQUE constraint")) {
                    db.chatMessageDao().deleteMessageBy(id)
                }
            }
        }
    }

    private fun initMessageFirstId() {
        db.chatMessageDao().deleteValue()
        db.chatMessageDao().insertValue()
        db.chatMessageDao().deleteValue()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}