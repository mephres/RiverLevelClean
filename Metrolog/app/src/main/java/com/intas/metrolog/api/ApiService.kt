package com.intas.metrolog.api

import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.authuser.AuthResponse
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.document_type.DocumentType
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
     * @param parameters параметры для авторизации
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
     * @return ответ сервера [UpdateDataResponse]
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
     * @param parameters параметры запроса
     * @return список мероприятий [EventItem]
     */
    @GET("getToir")
    fun getEventList(@Query(QUERY_PARAM_USER_ID) userId: Int,
                     @Query(QUERY_PARAM_MONTH) month: Int,
                     @Query(QUERY_PARAM_YEAR) year: Int
    ): Single<ResponseApi<EventItem>>


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
    }
}