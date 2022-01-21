package com.intas.metrolog.pojo.event.operation

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.intas.metrolog.pojo.event.operation.operation_control.OperControlItem
import kotlinx.parcelize.Parcelize

/**
 * Операция мероприятия
 */
@Parcelize
@Entity(tableName = "operation", indices = [Index(value = ["opId"], unique = false)])
data class OperationItem(
    @PrimaryKey(autoGenerate = false)
    /**
     * идентификатор записи
     */
    var subId: Long = 0,
    /**
     * идентификатор мероприятия
     */
    val opId: Long = 0,
    /**
     * идентификатор типа операции [EventOperationItem]
     */
    val subType: Long? = null,
    /**
     * наименование операции
     */
    val subName: String? = null,
    /**
     * время выполнения в человекочасах
     */
    val subManhour: Double? = null,
    /**
     * фактическое время выполнения в человеко часах
     */
    val subManhourFact: Double? = null,
    /**
     * идентификатор оборудования для комплексного мероприятия
     */
    val equipId: Long = 0,
    /**
     * дата-время начала выполнения операции
     */
    val dateStart: Long = 0,

    /**
     * дата-время окончания выполнения операции
     */
    val dateEnd: Long = 0,

    /**
     * признак - операция выполнена (0 - не выполнена, 1 - выполнена)
     */
    val completed: Int = 0,

    /**
     * идентификатор пользователя, выполнивший операцию мероприятия
     */
    val completedUserId: Long = 0,

    /**
     * признак - фотофиксация при выполнении мероприятия (0 - фото не нужно, 1 - фото нужно)
     */
    val needPhotoFix: Int = 0,

    /**
     * Признак отправки на сервер
     * 0 - не отправлена
     * 1 - отправлена
     */
    val isSended: Int = 1
) : Parcelable {
    /**
     * операционный контроль
     */
    @androidx.room.Ignore
    @SerializedName("operControl")
    val operControl: OperControlItem? = null

    constructor() : this(
        0,
        0,
        0,
        "",
        0.0,
        0.0,
        0,
        0,
        0,
        0,
        0,
        0,
        1
    )
}
