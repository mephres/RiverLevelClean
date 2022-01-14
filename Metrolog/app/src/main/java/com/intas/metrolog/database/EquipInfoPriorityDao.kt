package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority

@Dao
interface EquipInfoPriorityDao {
    @Query("SELECT * FROM equipInfoPriority")
    fun getAllEquipInfoPriority(): LiveData<List<EquipInfoPriority>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipInfoPriorityList(equipInfoPriorityList: List<EquipInfoPriority>)

    @Query("DELETE FROM equipInfoPriority")
    suspend fun deleteAllEquipInfoPriority()
}