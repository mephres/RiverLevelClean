package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.EventItem

@Dao
interface EventDao {
        @Query("SELECT * FROM event order by opId asc")
        fun getEventList(): LiveData<List<EventItem>>

        @Update(onConflict = OnConflictStrategy.REPLACE)
        suspend fun updateEvent(event: EventItem)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEvent(event: EventItem)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEventList(eventList: List<EventItem>)
}