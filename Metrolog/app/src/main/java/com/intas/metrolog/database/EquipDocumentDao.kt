package com.intas.metrolog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.intas.metrolog.pojo.equip.EquipDocument

@Dao
interface EquipDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipDocument(equipDocument: EquipDocument)
}