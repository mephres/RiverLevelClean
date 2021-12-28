package com.intas.metrolog.pojo.userlocation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_location")
data class UserLocation(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    /**
     * идентификатор пользователя
     */
    var userId: Int = 0,
    /**
     * точность
     */
    var accuracy: Float = 0f,
    /**
     * Высота
     */
    var altitude: Double = 0.0,
    /**
     * азимут, пеленг
     */
    var bearing: Float = 0f,
    /**
     * время fix с момента загрузки системы
     */
    var elapsedRealtimeNanos: Long = 0,
    /**
     * широта (Y)
     */
    var latitude: Double = 0.0,
    /**
     * долгота (Х)
     */
    var longitude: Double = 0.0,
    /**
     * провайдер
     */
    var provider: String? = null,
    /**
     * скорость
     */
    var speed: Float = 0f,
    /**
     * дата время
     */
    var time: Long = 0,

    /**
     * признак отправки на сервер
     * 0 - неотправлено
     * 1 - отправлено
     */
    var isSended: Int = 0
) {
}