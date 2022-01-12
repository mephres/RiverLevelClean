package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem

@Dao
interface EquipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipItem(equipItem: EquipItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipList(equipList: List<EquipItem>)

    @Query("UPDATE equip SET isSendGeo = 1 WHERE equipId = :id")
    suspend fun setEquipLocationSendedById(id: Long)

    @Query("UPDATE equip SET isSendRFID = 1 WHERE equipId = :id")
    suspend fun setEquipRFIDSendedById(id: Long)

    @Query("SELECT * FROM equip")
    fun getAllEquip(): LiveData<List<EquipItem>>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipInfoList(equipInfoList: List<EquipInfo>)

}