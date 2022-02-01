package com.intas.metrolog.api

import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.authuser.AuthResponse
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.http.ResponseApi
import com.intas.metrolog.pojo.http.UpdateResponse
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    /**
     * Авторицация пользователя
     *
     * @param login - имя пользователя
     * @param password - пароль пользователя
     * @return [AuthResponse]
     */
    @GET("auth")
    fun authUser(
        @Query(QUERY_PARAM_LOGIN) login: String = "",
        @Query(QUERY_PARAM_PASSWORD) password: String = "",
    ): Single<AuthResponse>

    /**
     * Добавление географических координат местоположения мобильного устройства
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addLocation")
    fun addLocation(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Получение списка пользователей
     */
    @GET("getChatUser")
    fun getUserList(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<UserItem>>

    /**
     * Получение списка оборудования
     *
     * @param userId параметр для запроса
     * @return [EquipItem]
     */
    @GET("getEquip")
    fun getEquip(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<EquipItem>>

    /**
     * Получение списка возможных статусов заявок
     *
     * @param userId параметр для запроса
     * @return список статусов [RequestStatusItem]
     */
    @GET("getDictRequestStatus")
    fun getRequestStatus(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<RequestStatusItem>>

    /**
     * Получение списка дисциплин
     *
     * @param userId параметр для запроса
     * @return список дисциплин [DisciplineItem]
     */
    @GET("getDictDiscipline")
    fun getDiscipline(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<DisciplineItem>>

    /**
     * Получение списка операций мероприятия
     *
     * @param userId параметр для запроса
     * @return список операций мероприятия [EventOperationTypeItem]
     */
    @GET("getDictOper")
    fun getEventOperationType(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<EventOperationTypeItem>>

    /**
     * Получение списка типов документов для генерации PDF
     *
     * @param userId параметр для запроса
     * @return список типов документов для генерации PDF [DocumentType]
     */
    @GET("getDictEquipDocType")
    fun getDocumentTypeList(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<DocumentType>>

    /**
     * Получение списка приоритетов для комментариев по оборудованию
     *
     * @param userId параметр для запроса
     * @return список приоритетов для комментариев по оборудованию [EquipInfoPriority]
     */
    @GET("getDictEquipInfoPriority")
    fun getEquipInfoPriorityList(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<EquipInfoPriority>>

    /**
     * Получение списка комментариев
     *
     * @param userId параметр для запроса
     * @return список комментариев [EventComment]
     */
    @GET("getDictComment")
    fun getEventCommentList(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<EventComment>>

    /**
     * Обновление метки оборудования на сервере ЦНО
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addEquipRFID")
    fun updateEquipRFID(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Обновление гео данных оборудования на сервере ЦНО
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addEquipLocation")
    fun updateEquipGeo(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Получение списка заявок для авторизованного пользователя с сервера ЦНО
     *
     * @param userId параметр для запроса
     * @return список заявок [RequestItem]
     */
    @GET("getRequest")
    fun getRequestList(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<RequestItem>>

    /**
     * Добавление заявки на сервер
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addRequest")
    fun addRequest(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Добавление документа для оборудования на сервер ЦНО
     *
     * @param file    файл документа для оборудования
     * @param id      id записи из таблицы [EquipDocument]
     * @param userId  id авторизованного пользователя
     * @param equipId id оборудования
     * @param type    тип документа из справочника [DocumentType]
     * @param doc     произвольная информация
     * @return ответ сервера [UpdateResponse]
     */
    @Streaming
    @Multipart
    @POST("addEquipDoc")
    fun addEquipDocument(
        @Part file: MultipartBody.Part?,
        @Part(QUERY_PARAM_ID) id: RequestBody,
        @Part(QUERY_PARAM_USER_ID) userId: RequestBody,
        @Part(QUERY_PARAM_EQUIP_ID) equipId: RequestBody,
        @Part(QUERY_PARAM_TYPE) type: RequestBody,
        @Part(QUERY_PARAM_EQUIP_DOCUMENT) doc: RequestBody
    ): Single<UpdateResponse>

    /**
     * Получение списка мероприятий
     *
     * @param userId - идентификатор авторизованного пользователя
     * @param month - порядковый номер месяца
     * @param year - год
     * @return список мероприятий [EventItem]
     */
    @GET("getToir")
    fun getEventList(@Query(QUERY_PARAM_USER_ID) userId: Int,
                     @Query(QUERY_PARAM_MONTH) month: Int,
                     @Query(QUERY_PARAM_YEAR) year: Int
    ): Single<ResponseApi<EventItem>>

    /**
     * Обновление данных мероприятия на сервере
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("updToir")
    fun updateEvent(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Выполнение операции из чек-листа мероприятия
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addOperFact")
    fun addOperation(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Добавление на сервер ЦНО списка параметров параметров с измерениями
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addOperControlFact")
    fun addOperControlFact(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Добавление изображения для мероприятия
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addToirPhoto")
    fun addEventPhoto(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Добавление комментария к оборудованию
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addEquipInfo")
    fun addEquipInfo(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Добавление картинки или фото для заявки
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addRequestPhoto")
    fun addRequestPhoto(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Получение списка сообщений для переписки с сервера ЦНО
     *
     * @param userId параметр для запроса
     * @param id параметр для запроса
     * @return список заявок [MessageItem]
     */
    @GET("getChatMessage")
    fun getChatMessage(
        @Query(QUERY_PARAM_USER_ID) userId: Int,
        @Query(QUERY_PARAM_ID) id: Int
    ): Single<ResponseApi<MessageItem>>

    /**
     * Добавление сообщения переписки на сервер
     *
     * @param fields параметры запроса
     * @return ответ сервера [UpdateResponse]
     */
    @FormUrlEncoded
    @POST("addChatMessage")
    fun addChatMessage(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    companion object {
        const val QUERY_PARAM_LOGIN = "login"
        const val QUERY_PARAM_PASSWORD = "pass"

        const val QUERY_PARAM_USER_ID = "userId"
        const val QUERY_PARAM_ID = "id"
        const val QUERY_PARAM_ACCURACY = "accuracy"
        const val QUERY_PARAM_ALTITUDE = "altitude"
        const val QUERY_PARAM_BEARING = "bearing"
        const val QUERY_PARAM_ELAPSED_REALTIME_NANOS = "elapsedRealtimeNanos"
        const val QUERY_PARAM_LATITUDE = "latitude"
        const val QUERY_PARAM_LONGITUDE = "longitude"
        const val QUERY_PARAM_PROVIDER = "provider"
        const val QUERY_PARAM_SPEED = "speed"
        const val QUERY_PARAM_TIME = "time"
        const val QUERY_PARAM_MONTH = "month"
        const val QUERY_PARAM_YEAR = "year"
        const val QUERY_PARAM_LAST_ID = "lastId"
        const val QUERY_PARAM_EQUIP_ID = "equipId"
        const val QUERY_PARAM_EQUIP_RFID = "equipRFID"
        const val QUERY_PARAM_TYPE = "type"
        const val QUERY_PARAM_EQUIP_DOCUMENT = "doc"

        const val QUERY_PARAM_DATETIME = "dateTime"

        const val QUERY_PARAM_OP_ID = "opId"
        const val QUERY_PARAM_FACT_DATE = "factDate"
        const val QUERY_PARAM_STATUS_ID = "status"
        const val QUERY_PARAM_DURATION_TIMER = "durationTimer"
        const val QUERY_PARAM_DATE_TIME_START_TIMER = "dateTimeStartTimer"
        const val QUERY_PARAM_COMMENT = "comment"

        const val QUERY_PARAM_RFID = "rfid"
        const val QUERY_PARAM_CATEGORY_ID = "categoryId"
        const val QUERY_PARAM_DISCIPLINE_ID = "disciplineId"
        const val QUERY_PARAM_OPERATION_TYPE = "operationType"
        const val QUERY_PARAM_REQUEST_ID = "requestId"
        const val QUERY_PARAM_REQUEST_PHOTO = "photo"
        const val QUERY_PARAM_PRIORITY = "priority"

        const val QUERY_PARAM_SUB_ID = "subId"
        const val QUERY_PARAM_SUB_MAN_HOUR = "subManhour"
        const val QUERY_PARAM_DATE_START = "dateStart"
        const val QUERY_PARAM_DATE_END = "dateEnd"
        const val QUERY_PARAM_COMPLETED = "completed"
        const val QUERY_PARAM_COMPLETED_USER_ID = "completedUserId"

        const val QUERY_PARAM_DATA = "data"
        const val QUERY_PARAM_PHOTO = "photo"
    }
}