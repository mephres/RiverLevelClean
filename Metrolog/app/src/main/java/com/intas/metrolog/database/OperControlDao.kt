package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation.operation_control.OperControlItem

@Dao
interface OperControlDao {

    @Query("SELECT * FROM oper_control WHERE isSended = 0")
    fun getNotSendedEventOperationControlList(): LiveData<List<OperControlItem>>

    @Query("SELECT * FROM oper_control WHERE id = :id")
    fun getEventOperationControlById(id: Int): OperControlItem?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateOperControl(operControl: OperControlItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperControl(operControl: OperControlItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperControlList(operControlList: List<OperControlItem>)

    @Query("UPDATE oper_control SET isSended = 1 WHERE eventId = :eventId AND opId = :operationId")
    suspend fun setEventOperationControlSendedById(eventId: Long, operationId: Long)

    @Query("UPDATE oper_control SET isSended = 0 WHERE eventId = :eventId AND opId = :operationId")
    suspend fun setEventOperationControlReadyForSendBy(eventId: Long, operationId: Long)
}