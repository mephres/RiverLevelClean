package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.event_priority.EventPriority

@Dao
interface EventPriorityDao {
    @Query("SELECT * FROM eventPriority")
    fun getAllEventPriority(): LiveData<List<EventPriority>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventPriorityList(eventPriorityList: List<EventPriority>)

    @Query("DELETE FROM eventPriority")
    suspend fun deleteAllEventPriority()
}