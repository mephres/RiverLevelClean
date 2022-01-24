package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem

@Dao
interface EventOperationTypeDao {
    @Query("SELECT * FROM eventOperationType order by name asc")
    fun getAllEventOperationType(): LiveData<List<EventOperationTypeItem>>

    @Query("SELECT * FROM eventOperationType WHERE id = :id")
    fun getEventOperationTypeById(id: Int): EventOperationTypeItem?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventOperationType(eventOperationType: EventOperationTypeItem?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperationType(eventOperationType: EventOperationTypeItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventOperationTypeList(eventOperationTypeList: List<EventOperationTypeItem>)

    @Query("DELETE FROM eventOperationType")
    suspend fun deleteAllEventOperationType()
}