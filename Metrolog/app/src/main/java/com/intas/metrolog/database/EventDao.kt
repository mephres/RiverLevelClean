package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.EventItem

@Dao
interface EventDao {
        @Query("SELECT * FROM event order by opId asc")
        fun getEventList(): LiveData<List<EventItem>>

        @Query("SELECT * FROM event WHERE :dateStart <= planDate AND planDate <= :dateEnd AND status < 3 order by opId asc")
        fun getEventList(dateStart: Long, dateEnd: Long): LiveData<List<EventItem>>

        @Query("SELECT * FROM event WHERE :dateStart <= planDate AND planDate <= :dateEnd AND status = :status order by opId asc")
        fun getEventList(dateStart: Long, dateEnd: Long, status: Int): LiveData<List<EventItem>>

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

        @Query("SELECT * FROM event WHERE opId = :id LIMIT 1")
        fun getEventLD(id: Long): LiveData<EventItem>

        @Query("UPDATE event SET equipRfid = :rfid WHERE equipId = :equipId")
        fun updateEventByRfid(equipId: Long, rfid: String): Int

        @Query("SELECT * FROM event WHERE priority > 1 AND eventDone = 0 AND status = 0 ORDER BY priority DESC")
        fun getHighPriorityEventList(): List<EventItem>

        @Query("SELECT EXISTS(SELECT * FROM event WHERE priority > 1 AND eventDone = 0 AND status = 0)")
        fun isHighPriorityEventsExists(): Boolean
}