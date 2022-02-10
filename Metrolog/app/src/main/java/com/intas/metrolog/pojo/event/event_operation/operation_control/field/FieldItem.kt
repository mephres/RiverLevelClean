package com.intas.metrolog.pojo.event.event_operation.operation_control.field

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Параметр операционного контроля, который заполняется при выполнении операции мероприятия
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
     * dict - это спиннер со своим списком из таблицы [FieldDictData]
     * иначе - это EditText
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
    var value: String? = null,

    /**
     * идентификатор пользователя, проводившего измерения
     */
    var userId: Int? = null,

    /**
     * дата-время измерения в UNIX формате
     */
    var dateTime: Long? = null,

    /**
     * код
     */
    var classCode: String? = null,
    /**
     * Признак отправки записи на сервер
     * 0 - не отправлена
     * 1 - отправлена
     */
    var isSended: Int = 1
) : Parcelable {
    /**
     * список способов измерения
     */
    @Ignore
    @SerializedName("dictData")
    val dictData: Map<String, String>? = null
}
