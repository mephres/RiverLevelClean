package com.intas.metrolog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem

@Dao
interface FieldDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateField(field: FieldItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldList(field: List<FieldItem>)
}