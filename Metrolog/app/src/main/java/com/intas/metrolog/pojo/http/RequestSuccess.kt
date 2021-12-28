package com.intas.metrolog.pojo.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestSuccess(
    @SerializedName("id")
    @Expose
    var id: String? = null,
    @SerializedName("code")
    @Expose
    val code: String? = null,
    @SerializedName("description")
    @Expose
    val description: String? = null,
    @SerializedName("message")
    @Expose
    val message: String? = null,
    /**
     * id записи в базе данных сервера для последующего обновления на стороне мобильного устройства
     */
    @SerializedName("serverId")
    @Expose
    val serverId: String? = null,
    /**
     * Идентификатор комнаты чата
     */
    @SerializedName("roomId")
    @Expose
    val roomId: Int? = null,
    /**
     * Признак загрузки файла с мобильного устройства на сервер
     * true - файл загружен
     * false - файл не загружен
     */
    @SerializedName("upload")
    @Expose
    val upload: String? = null
) {
}