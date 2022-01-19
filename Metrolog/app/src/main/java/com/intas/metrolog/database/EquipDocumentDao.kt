package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.equip.EquipDocument

@Dao
interface EquipDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipDocument(equipDocument: EquipDocument)

    @Query("SELECT * FROM equipDocument WHERE isSended = 0 ORDER BY id ASC LIMIT 1")
    fun getNotSendedEquipDocumentList(): LiveData<List<EquipDocument>>

    @Query("UPDATE equipDocument SET isSended = 1 WHERE id = :id")
    suspend fun setEquipDocumentSendedById(id: Long)
}