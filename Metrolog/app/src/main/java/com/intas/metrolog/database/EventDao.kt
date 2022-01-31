package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.EventItem

@Dao
interface EventDao {
        @Query("SELECT * FROM event order by opId asc")
        fun getEventList(): LiveData<List<EventItem>>

        @Query("SELECT * FROM event WHERE isSended = 0 ORDER BY opId")
        fun getNotSendedEventList(): LiveData<List<EventItem>>

        @Update(onConflict = OnConflictStrategy.REPLACE)
        suspend fun updateEvent(event: EventItem)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEvent(event: EventItem)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEventList(eventList: List<EventItem>)

        @Query("UPDATE event SET isSended = 1 WHERE opId = :id")
        suspend fun setEventSendedById(id: Long)

        @Query("SELECT * FROM event WHERE equipRfid = :rfid ORDER BY opId ASC")
        fun getEventListByRfid(rfid: String): List<EventItem>

        @Query("SELECT * FROM event WHERE opId = :id LIMIT 1")
        fun getEvent(id: Long): EventItem?

        @Query("UPDATE event SET equipRfid = :rfid WHERE equipId = :equipId")
        fun updateEventByRfid(equipId: Long, rfid: String): Int
}