package com.intas.metrolog.database

import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem

@Dao
interface FieldDao {

    @Query("SELECT * FROM field WHERE isSended = 0 AND eventId = :eventId AND operationId = :operationId ORDER BY id ASC")
    fun getNotSendedEventOperationFieldList(eventId: Long, operationId: Long): List<FieldItem>

    @Query("SELECT * FROM field WHERE id = :id")
    fun getFieldById(id: Long): FieldItem?

    @Query("SELECT * FROM field WHERE operationId = :operationId")
    fun getFieldsByOperationId(operationId: Long): List<FieldItem>?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateField(field: FieldItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldList(field: List<FieldItem>)
}