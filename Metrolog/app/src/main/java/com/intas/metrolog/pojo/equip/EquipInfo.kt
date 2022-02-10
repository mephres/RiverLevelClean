package com.intas.metrolog.pojo.equip

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Информация по оборудованию
 */
@Parcelize
@Entity(tableName = "equipInfo")
data class EquipInfo(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    @SerializedName("prId")
    @Expose
    val id: Int = 0,
    /**
     * идентификатор оборудования [Equip]
     */
    @SerializedName("equipId")
    @Expose
    var equipId: Long = 0,
    /**
     * текст информации по оборудованию
     */
    @SerializedName("text")
    @Expose
    val text: String? = null,
    /**
     * дата-время создания информации по оборудованию в UNIX формате
     */
    @SerializedName("dateTime")
    @Expose
    val dateTime: Long = 0,
    /**
     * идентификатор пользователя
     */
    @SerializedName("userId")
    @Expose
    val userId: Int = 0,
    /**
     * приоритет информации по оборудованию
     */
    @SerializedName("priority")
    @Expose
    val priority: Int = 0,
    /**
     * информация просмотрена пользователем (0 - не просмотрена, 1 - просмотрена)
     */
    @SerializedName("checked")
    @Expose
    var checked: Boolean = false,
    /**
     * дата-время просмотра информации по оборудованию
     */
    @SerializedName("checkedDateTime")
    @Expose
    var checkedDateTime: Long = 0,
    /**
     * идентификатор промотревшего информацию пользователя
     */
    @SerializedName("checkedUserId")
    @Expose
    var checkedUserId: Int = 0,
    /**
     * Признак отсылки записи на сервер
     * 0 - не отослана
     * 1 - отослана
     */
    var isSended: Int = 1
) : Parcelable
