package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem

@Dao
interface FieldDao {

    @Query("SELECT * FROM field WHERE isSended = 0 ORDER BY id ASC LIMIT 1")
    fun getNotSendedEventOperationFieldList(): LiveData<List<FieldItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateField(field: FieldItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldList(field: List<FieldItem>)
}