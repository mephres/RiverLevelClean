package com.intas.metrolog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData

@Dao
interface FieldDictDataDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFieldDictData(fieldFieldDictData: FieldDictData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldDictData(fieldDictData: FieldDictData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldDictDataList(fieldDictDataList: List<FieldDictData>)
}