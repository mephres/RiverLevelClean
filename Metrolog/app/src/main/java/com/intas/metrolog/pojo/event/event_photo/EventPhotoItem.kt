package com.intas.metrolog.pojo.event.event_photo

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.intas.metrolog.util.DateTimeUtil
import com.intas.metrolog.util.Util

@Entity(tableName = "event_photo", indices = [Index(value = ["opId"], unique = false)])
data class EventPhotoItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    /**
     * идентификатор мероприятия
     */
    val opId: Long? = null,
    /**
     * изображение в кодировке base64
     */
    val photoUrl: String? = null,
    /**
     * дата-время изображения в unix формате
     */
    val datetime: Long = DateTimeUtil.getUnixDateTimeNow(),
    /**
     * идентификатор пользователя, сделавшего изображение
     */
    val userId: Int = Util.authUser?.userId ?: 0,
    /**
     * 0 - не отправлено на сервер
     * 1 - отправлено на сервер
     */
    val isSended: Int = 1
)

