package com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Список способов измерения для параметра операционного контроля
 */
@Parcelize
@Entity(tableName = "fieldDictData")
data class FieldDictData(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    /**
     * идентификатор поля операционного контроля
     */
    val fieldId: Long = 0,
    /**
     * измеренное значение
     */
    val value: String? = null,
    /**
     * код способа измерения
     */
    val code: String? = null,
): Parcelable
