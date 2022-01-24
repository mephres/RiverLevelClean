package com.intas.metrolog.pojo.discipline

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Дисциплина
 */
@Entity(tableName = "discipline")
data class DisciplineItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Int,
    /**
     * наименование дисциплины
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * код дисциплины
     */
    @SerializedName("code")
    @Expose
    val code: String
)