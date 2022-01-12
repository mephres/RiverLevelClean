package com.intas.metrolog.pojo.event_priority

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Приоритет мероприятия
 */
@Entity(tableName = "eventPriority")
data class EventPriority(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Long,
    /**
     * наименование приоритета мероприятия
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * код приоритета мероприятия
     */
    @SerializedName("code")
    @Expose
    val code: String
)
