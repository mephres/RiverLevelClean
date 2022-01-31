package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.event.event_status.EventStatus

@Dao
interface EventStatusDao {
    @Query("SELECT * FROM event_status")
    fun getAllEventStatus(): LiveData<List<EventStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventStatusList(eventStatusList: List<EventStatus>)
}