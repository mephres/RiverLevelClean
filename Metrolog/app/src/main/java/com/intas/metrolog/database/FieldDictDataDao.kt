package com.intas.metrolog.database

import androidx.room.*
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData

@Dao
interface FieldDictDataDao {

    @Query("SELECT * FROM fieldDictData WHERE fieldId = :fieldId ORDER BY value ASC")
    fun getDictDataByFieldId(fieldId: Long): List<FieldDictData>?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFieldDictData(fieldFieldDictData: FieldDictData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldDictData(fieldDictData: FieldDictData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFieldDictDataList(fieldDictDataList: List<FieldDictData>)
}