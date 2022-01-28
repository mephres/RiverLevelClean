package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem

@Dao
interface EventOperationDao {

    @Query("SELECT * FROM eventOperation WHERE isSended = 0 ORDER BY subId")
    fun getNotSendedEventOperationList(): LiveData<List<EventOperationItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventOperation(eventOperation: EventOperationItem?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperation(eventOperation: EventOperationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperationList(eventOperationList: List<EventOperationItem>)

    @Query("UPDATE eventOperation SET isSended = 1 WHERE subId = :id")
    suspend fun setEventOperationSendedById(id: Long)

    @Query("SELECT * FROM eventOperation WHERE opId = :opId")
    fun getCheckList(opId: Long): LiveData<List<EventOperationItem>>
}