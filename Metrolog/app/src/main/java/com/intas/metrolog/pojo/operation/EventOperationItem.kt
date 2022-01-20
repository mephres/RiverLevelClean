package com.intas.metrolog.pojo.operation

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
/**
 * Тип операции мероприятия
 */
@Entity(tableName = "eventOperation")
data class EventOperationItem(
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
