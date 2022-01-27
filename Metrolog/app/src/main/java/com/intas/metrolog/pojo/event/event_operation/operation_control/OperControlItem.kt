package com.intas.metrolog.pojo.event.event_operation.operation_control

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
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
     * идентификатор операции мероприятия [EventOperationItem]
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
    /**
     * идентификатор оборудования [EquipItem]
     */
    var equipId: Long = 0,
    /**
     * Признак отправки записи на сервер
     * 0 - не отправлена
     * 1 - отправлена
     */
    val isSended: Int = 1
) : Parcelable {

    /**
     * список полей операционного контроля
     */
    @Ignore
    @SerializedName("fields")
    val fieldList: List<FieldItem>? = null
}
