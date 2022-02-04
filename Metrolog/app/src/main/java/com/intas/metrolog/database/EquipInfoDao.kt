package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.equip.EquipInfo

@Dao
interface EquipInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipInfoList(equipInfoList: List<EquipInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipInfo(equipInfo: EquipInfo): Long

    @Query("SELECT * FROM equipInfo WHERE isSended = 0 ORDER BY id")
    fun getNotSendedEquipInfoList(): LiveData<List<EquipInfo>>

    @Query("UPDATE equipInfo SET isSended = 1 AND id = :serverId WHERE id = :id")
    suspend fun setEquipInfoSendedById(id: Long, serverId: Long)
}