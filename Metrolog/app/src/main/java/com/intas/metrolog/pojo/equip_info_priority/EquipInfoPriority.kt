package com.intas.metrolog.pojo.equip_info_priority

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Приоритет информации для оборудования
 */
@Entity(tableName = "equipInfoPriority")
data class EquipInfoPriority(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Int,
    /**
     * Наименование приоритета информации для оборудования
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * Код приоритета информации для оборудования
     */
    @SerializedName("code")
    @Expose
    val code: String
)
