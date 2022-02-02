package com.intas.metrolog.pojo.event

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.pojo.event.event_priority.EventPriority
import com.intas.metrolog.pojo.event.event_status.EventStatus
import com.intas.metrolog.util.DateTimeUtil
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
    var factDate: String? = null,
    /**
     * фио ответственного, заполнено, если мероприятие выполнено
     */
    @SerializedName("otv")
    @Expose
    var otv: String? = null,
    /**
     * идентификатор пользователя, выполневшего мероприятие
     */
    @SerializedName("userId")
    @Expose
    var userId: String? = null,
    /**
     * признак - мероприятие выполнено (true - мероприятие выполнено, false - не выполнено)
     */
    @SerializedName("eventDone")
    @Expose
    var eventDone: Boolean = false,
    /**
     * текст комментария к мероприятию
     */
    @SerializedName("comment")
    @Expose
    val comment: String? = null,
    /**
     * дата-время начала выполнения мероприятия в UNIX формате
     */
    @SerializedName("dateTimeStartTimer")
    @Expose
    var dateTimeStartTimer: Long? = 0,
    /**
     * продолжительность выполнения мероприятия в милисекундах
     */
    @SerializedName("durationTimer")
    @Expose
    var durationTimer: Long? = 0,
    /**
     * статус мероприятия (0 - новое, 1 - в работе, 2 - остановлено, 3 - выполнено, 4 - отказ)
     */
    @SerializedName("status")
    @Expose
    var status: Int = EventStatus.NEW,

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
    var isSended: Int = 1,
    /**
     * количество операций мероприятия
     */
    var operationListSize: Int = 0,

    /**
     * идентификатор оборудования мероприятия [EquipItem]
     */
    var equipId: Long? = 0,

    /**
     * метка оборудования мероприятия
     */
    var equipRfid: String? = null,

    /**
     * Название оборудования мероприятия
     */
    var equipName: String? = null,

    /**
     * признак - фотофиксация при выполнении мероприятия (0 - фото не нужно, 1 - фото нужно)
     */
    var needPhotoFix: Boolean = false
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
    var operation: List<EventOperationItem>? = null

    /**
     * месяц, на который запланировано данное мероприятие
     */
    val month: Int
        get() = DateTimeUtil.getDateTimeFromMili(planDate ?: 0, "MM").toInt()

    /**
     * день, на который запланировано данное мероприятие
     */
    val day: Int
        get() = DateTimeUtil.getDateTimeFromMili(planDate ?: 0, "dd").toInt()
}