package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem

@Dao
interface EventOperationDao {

    @Query("SELECT * FROM eventOperation WHERE isSended = 0 ORDER BY subId ASC LIMIT 1")
    fun getNotSendedEventOperationList(): LiveData<List<EventOperationItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventOperation(eventOperation: EventOperationItem?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperation(eventOperation: EventOperationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperationList(eventOperationList: List<EventOperationItem>)
}