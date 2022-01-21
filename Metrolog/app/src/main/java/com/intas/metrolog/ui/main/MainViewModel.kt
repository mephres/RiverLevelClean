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
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_EQUIP_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_EQUIP_RFID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_ID
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LATITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_LONGITUDE
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_PROVIDER
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_SPEED
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_TIME
import com.intas.metrolog.api.ApiService.Companion.QUERY_PARAM_USER_ID
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.OperControlItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.metrolog.ui.requests.filter.RequestFilter
import com.intas.metrolog.util.AppPreferences
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.SingleLiveEvent
import com.intas.metrolog.util.Util
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
import java.io.File
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    private var sendUserLocationDisposable: Disposable? = null
    private var sendEquipLocationDisposable: Disposable? = null
    private var sendEquipRFIDDisposable: Disposable? = null
    private var getEquipDisposable: Disposable? = null
    private var getRequestDisposable: Disposable? = null
    private var getEventDisposable: Disposable? = null
    private var sendEquipDocumentDisposable: Disposable? = null

    val notSendedUserLocationList = db.userLocationDao().getNotSendedUserLocationList()
    val notSendedEquipDocumentList = db.equipDocumentDao().getNotSendedEquipDocumentList()
    val notSendedEquipList = db.equipDao().getEquipNotSended()

    val onErrorMessage = SingleLiveEvent<String>()

    private var _requestFilter = MutableLiveData<RequestFilter>()
    val requestFilter: LiveData<RequestFilter>
        get() = _requestFilter

    init {
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
                        insertEquipInfoList(eil)
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
            db.equipDao().insertEquipInfoList(equipInfoList)
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
        sendEquipRFIDDisposable?.let {
            compositeDisposable.remove(it)
        }

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

    /**
     * Отправка документа оборудования на сервер
     * @param equipDocument - документ оборудования
     */
    fun sendEquipDocument(equipDocument: EquipDocument) {

        sendEquipDocumentDisposable?.let {
            compositeDisposable.remove(it)
        }

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

        sendEquipDocumentDisposable = ApiFactory.apiService.addEquipDocument(
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
        sendEquipDocumentDisposable?.let {
            compositeDisposable.add(it)
        }
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

    private fun getEvent() {
        Util.authUser?.userId?.let {

            getEventDisposable?.let {
                compositeDisposable.remove(it)
            }

            val month = DateTimeUtil.getDateNowByPattern("MM")
            val year = DateTimeUtil.getDateNowByPattern("YYYY")

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
            db.eventDao().insertEventList(eventList)

            eventList.forEach { event ->
                event.operation?.let { eol ->
                    insertEventOperations(eol, event.opId)
                }

            }
        }
    }

    private fun insertEventOperations(
        eventEventOperationList: List<EventOperationItem>,
        eventId: Long
    ) {
        viewModelScope.launch {
            db.eventOperationDao().insertEventOperationList(eventEventOperationList.map {
                it.opId = eventId
                it
            })

            eventEventOperationList.forEach { eventOperation ->
                eventOperation.operControl?.let {
                    it.eventId = eventId
                    it.opId = eventOperation.subId
                    insertOperControl(it)
                }
            }

        }
    }

    private fun insertOperControl(operControl: OperControlItem) {
        viewModelScope.launch {
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
                                    db.fieldDictDataDao().insertFieldDictData(dictDataObject) // запись в базу способа измерения
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}