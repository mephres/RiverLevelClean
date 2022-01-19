package com.intas.metrolog.pojo.requestStatus

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Статус заявки
 */
@Entity(tableName = "requestStatus")
data class RequestStatusItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Int,
    /**
     * наименование статуса
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * код статуса
     */
    @SerializedName("code")
    @Expose
    val code: String
)
