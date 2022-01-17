package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import io.reactivex.Single

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

    @Query("SELECT COUNT(*) FROM equip")
    fun getEquipCount(): LiveData<Int>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateEquip(equip: EquipItem): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipInfoList(equipInfoList: List<EquipInfo>)

    @Query("SELECT * FROM equip WHERE equipId = :id")
    fun getEquipItemById(id: Long): EquipItem?

    @Query("SELECT * FROM equip WHERE equipRFID = :equipRFID")
    fun getEquipByRFID(equipRFID: String): Single<List<EquipItem>>

    @Query("SELECT * FROM equip WHERE isSendRFID = 0 ORDER BY equipId ASC LIMIT 1")
    fun getEquipNotSendRFID(): LiveData<List<EquipItem>>

    @Query("SELECT * FROM equip WHERE isSendRFID = 0")
    fun getEquipReplaceList(): LiveData<List<EquipItem>>

}