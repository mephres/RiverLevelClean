package com.intas.metrolog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.intas.metrolog.pojo.event.event_operation.operation_control.OperControlItem

@Dao
interface OperControlDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateOperControl(operControl: OperControlItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperControl(operControl: OperControlItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperControlList(operControlList: List<OperControlItem>)
}