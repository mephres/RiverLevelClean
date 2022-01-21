package com.intas.metrolog.pojo.event.event_operation.operation_control.field

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Поле для заполнения при выполнении операции мероприятия
 */
@Parcelize
@Entity(tableName = "field")
data class FieldItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    /**
     * идентификатор мероприятия
     */
    var eventId: Long = 0,
    /**
     * идентификатор операции мероприятия
     */
    var operationId: Long = 0,
    /**
     * наименование поля
     */
    val name: String? = null,
    /**
     * код поля
     */
    val code: String? = null,
    /**
     * тип поля
     */
    val type: String? = null,
    /**
     * код способа измерения
     */
    val dictCode: String? = null,

    /**
     * значение по-умолчанию
     */
    @SerializedName("default")
    val defaultValue: String? = null,

    /**
     * значение после измерения
     */
    val value: String? = null,

    /**
     * идентификатор пользователя, проводившего измерения
     */
    val userId: Long = 0,

    /**
     * дата-время измерения в UNIX формате
     */
    val dateTime: Long = 0,

    /**
     * код
     */
    var classCode: String? = null
) : Parcelable {
    /**
     * список способов измерения
     */
    @Ignore
    @SerializedName("dictData")
    val dictData: Map<String, String>? = null

    constructor() : this(0, 0, 0, "", "", "", "", "", "", 0, 0, "")
}
