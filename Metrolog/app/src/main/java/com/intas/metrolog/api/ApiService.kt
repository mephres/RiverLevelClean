package com.intas.metrolog.api

import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.authuser.AuthResponse
import com.intas.metrolog.pojo.http.ResponseApi
import com.intas.metrolog.pojo.http.UpdateResponse
import io.reactivex.Single
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
     * @return ответ сервера [UpdateDataResponse]
     */
    @FormUrlEncoded
    @POST("addLocation")
    fun addLocation(@FieldMap fields: Map<String, String>): Single<UpdateResponse>

    /**
     * Получение списка пользователей
     */
    @GET("getUserList")
    fun getUserList(@Query(QUERY_PARAM_USER_ID) userId: Int): Single<ResponseApi<UserItem>>

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
    }
}