package com.intas.metrolog.pojo.request

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.discipline.DisciplineItem

/**
 * Заявка
 */
@Parcelize
@Entity(tableName = "request", indices = [Index(value = ["id"], unique = true)])
data class RequestItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    @SerializedName("idRequest")
    @Expose
    val id: Long = 0,
    /**
     * идентификатор пользователя, отправившего заявку на сервер ЦНО
     */
    @SerializedName("senderId")
    @Expose
    val senderId: Int = 0,
    /**
     * идентификатор пользоваетеля-исполнителя заявки
     */
    @SerializedName("executorId")
    @Expose
    val executorId: String? = null,
    /**
     * идентификатор оборудования [EquipItem]
     */
    @SerializedName("equipId")
    @Expose
    val equipId: Long = 0,
    /**
     * тип операции мероприятия
     */
    @SerializedName("operationType")
    @Expose
    val operationType: Int = 0,
    /**
     * статус заявки
     */
    @SerializedName("status")
    @Expose
    val status: Int = 0,
    /**
     * дата-время создания заявки в UNIX формате
     */
    @SerializedName("creationDate")
    @Expose
    val creationDate: Long = 0,
    /**
     * приоритет заявки (0 - текущая, 1 - срочная)
     */
    @SerializedName("typeRequest")
    @Expose
    val typeRequest: Int = 0,
    /**
     * дата-время закрытия заявки в UNIX формате
     */
    @SerializedName("dueDate")
    @Expose
    val dueDate: String? = null,
    /**
     * текст пояснения к заявке
     */
    @SerializedName("comment")
    @Expose
    val comment: String? = null,
    /**
     * комментарий исполнителя
     */
    @SerializedName("executorComment")
    @Expose
    val executorComment: String? = null,
    /**
     * идентификатор пользователя-создателя заявки
     */
    @SerializedName("applicationCreator")
    @Expose
    val applicationCreator: Int = 0,
    /**
     * вывод/заключение при закрытии заявки
     */
    @SerializedName("conclusion")
    @Expose
    val conclusion: String? = null,
    /**
     * идентификатор графика ППР
     */
    @SerializedName("pprId")
    @Expose
    val pprId: Int = 0,
    /**
     * идентификатор дисциплины [DisciplineItem] (механика, метрология, энергетика….)
     */
    @SerializedName("discipline")
    @Expose
    val discipline: Int = 0,

    /**
     * категория заявки - авария,дефект, информация для ТО [Comment]
     */
    val categoryId: Int = 0,
    /**
     * идентификатор записи на сервере ЦНО
     */
    val serverId: Long = 0,
    /**
     * метка оборудования
     */
    var rfid: String? = null,
    /**
     * информация по оборудованию (название, серийный номер, завод изготовитель) для поиска
     */
    var equipInfo: String? = null,
    /**
     * информация о дисциплине для поиска
     */
    var disciplineInfo: String? = null,
    /**
     * информация по операции мероприятия для поиска
     */
    var eventOperationInfo: String? = null,
    /**
     * Признак отсылки записи на сервер
     * 0 - не отослана
     * 1 - отослана
     */
    var isSended: Int = 1
) : Parcelable
