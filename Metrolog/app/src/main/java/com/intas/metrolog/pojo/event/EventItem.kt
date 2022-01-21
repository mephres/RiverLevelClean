package com.intas.metrolog.pojo.event

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.event_priority.EventPriority
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.pojo.event.operation.OperationItem
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "event", indices = [Index(value = ["status"], unique = false)])
data class EventItem(
    /**
     * идентификатор записи мероприятия
     */
    @PrimaryKey(autoGenerate = false)
    @SerializedName("opId")
    @Expose
    var opId: Long = 0,
    /**
     * Наименование мероприятия
     */
    @SerializedName("name")
    @Expose
    val name: String? = null,
    /**
     * тип мероприятия
     */
    @SerializedName("type")
    @Expose
    val type: String? = null,
    /**
     * планируемая дата выполнения мероприятия в UNIX формате
     */
    @SerializedName("planDate")
    @Expose
    val planDate: Long? = null,
    /**
     * признак - мероприятие запланировано или внеплановое (0 - запланированное, 1 - внеплановое)
     */
    @SerializedName("unscheduled")
    @Expose
    val unscheduled: Int? = 0,
    /**
     * дата фактического выполнения мероприятия в UNIX формате (может принимать значение false при загрузке данных с сервера ЦНО)
     */
    @SerializedName("factDate")
    @Expose
    val factDate: String? = null,
    /**
     * фио ответственного, заполнено, если мероприятие выполнено
     */
    @SerializedName("otv")
    @Expose
    val otv: String? = null,
    /**
     * идентификатор пользователя, выполневшего мероприятие
     */
    @SerializedName("userId")
    @Expose
    val userId: String? = null,
    /**
     * признак - мероприятие выполнено (true - мероприятие выполнено, false - не выполнено)
     */
    @SerializedName("eventDone")
    @Expose
    val eventDone: Boolean = false,
    /**
     * текст комментария к мероприятию
     */
    @SerializedName("comment")
    @Expose
    val comment: String? = null,

    /**
     * признак - фотофиксация при выполнении мероприятия (0 - фото не нужно, 1 - фото нужно)
     */
    val needPhotoFix: Int = 0,
    /**
     * месяц, на который запланировано данное мероприятие
     */
    val month: Int = 0,
    /**
     * день, на который запланировано данное мероприятие
     */
    val day: Int = 0,
    /**
     * дата-время начала выполнения мероприятия в UNIX формате
     */
    @SerializedName("dateTimeStartTimer")
    @Expose
    val dateTimeStartTimer: Long? = 0,
    /**
     * продолжительность выполнения мероприятия в милисекундах
     */
    @SerializedName("durationTimer")
    @Expose
    val durationTimer: Long? = 0,
    /**
     * статус мероприятия (0 - новое, 1 - в работе, 2 - остановлено, 3 - выполнено, 4 - отказ)
     */
    @SerializedName("status")
    @Expose
    val status: Int = EventStatus.NEW.ordinal,

    /**
     * приоритет мероприятия: 1 - обычное(плановое), 2 - важное(срочное), 3 - авария
     */
    @SerializedName("priority")
    @Expose
    val priority: Int? = EventPriority.PLANED.ordinal,

    /**
     * 0 - не отправлено на сервер
     * 1 - отправлено на сервер
     */
    val isSended: Int = 1
) : Parcelable {
    /**
     * Оборудование
     */
    @Ignore
    @SerializedName("equip")
    @Expose
    var equip: EquipItem? = null

    /**
     * список операций для мероприятия
     */
    @Ignore
    @SerializedName("checkList")
    @Expose
    var operation: List<OperationItem>? = null

    /**
     * количество операций мероприятия
     */
    var operationListSize: Int = 0
    get() = operation?.size ?: 0

    /**
     * идентификатор оборудования мероприятия [EquipItem]
     */
    var equipId: Long? = 0
    get() = equip?.equipId

    /**
     * метка оборудования мероприятия
     */
    var equipRfid: String? = null
    get() = equip?.equipRFID

    constructor() : this(
        0,
        "",
        "",
        0L,
        1,
        "",
        "",
        "",
        false,
        "",
        0,
        1,
        1,
        0L,
        0,
        0,
        1,
        1
    )
}
