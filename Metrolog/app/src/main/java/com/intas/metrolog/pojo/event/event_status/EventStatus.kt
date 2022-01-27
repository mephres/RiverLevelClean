package com.intas.metrolog.pojo.event.event_status

class EventStatus {
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