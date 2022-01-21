package com.intas.metrolog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem

@Dao
interface EventOperationDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventOperation(eventOperation: EventOperationItem?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperation(eventOperation: EventOperationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperationList(eventOperationList: List<EventOperationItem>)
}