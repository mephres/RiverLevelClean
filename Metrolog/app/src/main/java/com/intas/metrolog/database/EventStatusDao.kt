package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.event_status.EventStatus

@Dao
interface EventStatusDao {
    @Query("SELECT * FROM eventStatus")
    fun getAllEventStatus(): LiveData<List<EventStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventStatus(eventStatus: EventStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventStatusList(eventStatusList: List<EventStatus>)

    @Query("DELETE FROM eventStatus")
    suspend fun deleteAllEventStatus()
}