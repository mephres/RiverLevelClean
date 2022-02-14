package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.event.EventItem

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

    @Query("SELECT * FROM equipInfo WHERE equipId = :equipId AND checked = 0 ORDER BY priority DESC")
    fun getNotCheckedEquipInfoById(equipId: Long): LiveData<List<EquipInfo>>

    @Query("SELECT EXISTS(SELECT * FROM equipInfo WHERE equipId = :equipId AND checked = 0)")
    fun isNotCheckedEquipInfoExists(equipId: Long): Boolean

    @Query("SELECT * FROM equipInfo WHERE id = :id LIMIT 1")
    fun getEquipInfo(id: Int): EquipInfo?
}