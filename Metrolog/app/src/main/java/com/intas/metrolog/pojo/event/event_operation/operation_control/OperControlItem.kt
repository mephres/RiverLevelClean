package com.intas.metrolog.pojo.event.event_operation.operation_control

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem
import kotlinx.parcelize.Parcelize

/**
 * Операционный контроль
 */
@Parcelize
@Entity(tableName = "oper_control", indices = [Index(value = ["opId"], unique = false)])
data class OperControlItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    /**
     * идентификатор операции мероприятия [OperationItem]
     */
    var opId: Long = 0,
    /**
     * идентификатор мероприятия [EventItem]
     */
    var eventId: Long = 0,
    /**
     * код
     */
    val classCode: String? = null,
) : Parcelable {

    /**
     * список полей операционного контроля
     */
    @Ignore
    @SerializedName("fields")
    val fieldList: List<FieldItem>? = null

    constructor() : this(0,0,0,"")
}
