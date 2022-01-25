package com.intas.metrolog.pojo.request

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "requestPhoto")
data class RequestPhoto(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    /**
     * идентификатор записи на сервере ЦНО
     */
    var serverId: Long = 0,
    /**
     * идентификатор заявки [RequestItem]
     */
    val requestId: Long = 0,
    /**
     * изображение в кодировке base64
     */
    val photo: String? = null,
    /**
     * дата-время изображения в unix формате
     */
    val dateTime: Long = 0,
    /**
     * идентификатор пользователя, сделавшего изображение
     */
    val userId: Int = 0,
    /**
     * Признак отсылки записи на сервер
     * 0 - не отослана
     * 1 - отослана
     */
    var isSended: Int = 1
) : Parcelable
