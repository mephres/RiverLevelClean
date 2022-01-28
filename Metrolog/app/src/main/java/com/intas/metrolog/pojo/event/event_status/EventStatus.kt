package com.intas.metrolog.pojo.event.event_status

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_status")
data class EventStatus(
    @PrimaryKey
    val id: Int = 0,
    val name: String
) {
    companion object {
        /**
         * новое мероприятияе
         */
        const val NEW = 0

        /**
         * мероприятие в работе
         */
        const val IN_WORK = 1

        /**
         * мероприятие остановлено
         */
        const val PAUSED = 2

        /**
         * мероприятияе выполнено
         */
        const val COMPLETED = 3

        /**
         * отказное мероприятие
         */
        const val CANCELED = 4
    }
}