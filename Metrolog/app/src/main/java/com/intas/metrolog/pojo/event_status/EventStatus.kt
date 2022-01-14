package com.intas.metrolog.pojo.event_status

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Статус мероприятия
 */
@Entity(tableName = "eventStatus")
data class EventStatus(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Long,
    /**
     * наименование статуса мероприятия
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * код статуса мероприятия
     */
    @SerializedName("code")
    @Expose
    val code: String
)
