package com.intas.metrolog.pojo.event.event_operation_type

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
/**
 * Тип операции мероприятия
 */
@Entity(tableName = "eventOperationType")
data class EventOperationTypeItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Int,
    /**
     * наименование типа операции
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * код типа операции
     */
    @SerializedName("code")
    @Expose
    val code: String
)
