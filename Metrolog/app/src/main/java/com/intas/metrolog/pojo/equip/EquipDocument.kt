package com.intas.metrolog.pojo.equip

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

/**
 * Документ для оборудования
 */
@Entity(tableName = "equipDocument", indices = [Index(value = arrayOf("equipId"), unique = false)])
data class EquipDocument(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    /**
     * идентификатор оборудования [Equip]
     */
    val equipId: Long = 0,
    /**
     * идентификатор типа документа [DocumentType]
     */
    val documentTypeId: Long = 0,
    /**
     * имя файла документа
     */
    val filename: String? = null,
    /**
     * дата-время создания документа
     */
    val dateTime: Long = DateTimeUtil.getUnixDateTimeNow(),
    /**
     * идентификатор пользователя [User]
     */
    val userId: Int = Util.authUser?.userId ?: 0,
    /**
     * путь к файлу документа
     */
    val filePath: String? = null,
    /**
     * Признак отсылки записи на сервер
     * 0 - не отослана
     * 1 - отослана
     */
    val isSended: Int = 0
) {
}