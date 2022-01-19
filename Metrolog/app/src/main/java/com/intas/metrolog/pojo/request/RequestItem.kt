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
    val id: Long,
    /**
     * идентификатор пользователя, отправившего заявку на сервер ЦНО
     */
    @SerializedName("senderId")
    @Expose
    val senderId: Int,
    /**
     * идентификатор пользоваетеля-исполнителя заявки
     */
    @SerializedName("executorId")
    @Expose
    val executorId: String?,
    /**
     * идентификатор оборудования [EquipItem]
     */
    @SerializedName("equipId")
    @Expose
    val equipId: Int,
    /**
     * тип операции мероприятия
     */
    @SerializedName("operationType")
    @Expose
    val operationType: Int,
    /**
     * статус заявки
     */
    @SerializedName("status")
    @Expose
    val status: Int,
    /**
     * дата-время создания заявки в UNIX формате
     */
    @SerializedName("creationDate")
    @Expose
    val creationDate: Long,
    /**
     * приоритет заявки (0 - текущая, 1 - срочная)
     */
    @SerializedName("typeRequest")
    @Expose
    val typeRequest: Int,
    /**
     * дата-время закрытия завки в UNIX формате
     */
    @SerializedName("dueDate")
    @Expose
    val dueDate: String?,
    /**
     * текст пояснения к заявке
     */
    @SerializedName("comment")
    @Expose
    val comment: String?,
    /**
     * комментарий исполнителя
     */
    @SerializedName("executorComment")
    @Expose
    val executorComment: String?,
    /**
     * идентификатор пользователя-создателя заявки
     */
    @SerializedName("applicationCreator")
    @Expose
    val applicationCreator: Int,
    /**
     * вывод/заключение при закрытии заявки
     */
    @SerializedName("conclusion")
    @Expose
    val conclusion: String?,
    /**
     * идентификатор графика ППР
     */
    @SerializedName("pprId")
    @Expose
    val pprId: Int,
    /**
     * идентификатор дисциплины [DisciplineItem] (механика, метрология, энергетика….)
     */
    @SerializedName("discipline")
    @Expose
    val discipline: Int,

    /**
     * категория заявки - авария,дефект, информация для ТО [Comment]
     */
    val categoryId: Int,
    /**
     * идентификатор записи на сервере ЦНО
     */
    val serverId: Long,
    /**
     * метка оборудования
     */
    var rfid: String? = null,
    /**
     * информация по оборудованию (название, серийный номер, завод изготовитель) для поиска
     */
    val equipInfo: String? = null,
    /**
     * информация о дисциплине для поиска
     */
    val disciplineInfo: String? = null,
    /**
     * информация по операции мероприятия для поиска
     */
    val eventOperationInfo: String? = null
) : Parcelable
