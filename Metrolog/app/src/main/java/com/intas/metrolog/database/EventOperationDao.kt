package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.operation.EventOperationItem

@Dao
interface EventOperationDao {
    @Query("SELECT * FROM eventOperation order by name asc")
    fun getAllEventOperation(): LiveData<List<EventOperationItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventOperation(eventOperation: EventOperationItem?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperation(eventOperation: EventOperationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperationList(eventOperationList: List<EventOperationItem>)

    @Query("DELETE FROM eventOperation")
    suspend fun deleteAllEventOperation()
}