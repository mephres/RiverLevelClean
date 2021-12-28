package com.intas.metrolog.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.intas.metrolog.util.DateTimeUtil

/**
 * Список журналов работы
 */
@Entity(tableName = "journal")
data class JournalItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    /**
     * дата-время события в UNIX формате
     */
    val dateTime: Long = DateTimeUtil.getUnixDateTimeNow(),
    /**
     * дата-время события
     */
    val dateTimeString: String = DateTimeUtil.getLongDateFromMili(dateTime),
    /**
     * комментарий
     */
    val comment: String? = null,
    /**
     * текст события
     */
    val journalText: String? = null,
    /**
     * тип события (1 - GET-запрос, 2 - POST-запрос)
     */
    var journalType: Int = 0
)
