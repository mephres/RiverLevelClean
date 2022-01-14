package com.intas.metrolog.pojo.document_type

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
/**
 * Тип документа оборудования
 */
@Entity(tableName = "documentType")
data class DocumentType(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Long,
    /**
     * Название документа
     */
    @SerializedName("name")
    @Expose
    val name: String,
    /**
     * Код документа
     */
    @SerializedName("code")
    @Expose
    val code: String
)
