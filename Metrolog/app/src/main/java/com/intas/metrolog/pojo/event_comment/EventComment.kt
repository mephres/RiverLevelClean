package com.intas.metrolog.pojo.event_comment

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Справочник комментариев для выполнения мероприятия или отказа от выполнения
 */
@Entity(tableName = "eventComment")
data class EventComment(
    /**
     * идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Long,
    /**
     * Текст комментария
     */
    @SerializedName("comment")
    @Expose
    val comment: String,
    /**
     * Тип комментария
     * 4 - отказ, 3 - выполнено, 2 - остановка, 100 - новая заявка, 200 - блок приоритетной информации
     */
    @SerializedName("type")
    @Expose
    val type: Long
)
