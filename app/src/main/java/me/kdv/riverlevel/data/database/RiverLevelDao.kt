package me.kdv.riverlevel.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RiverLevelDao {
    @Query("SELECT * FROM river_level ORDER BY lastUpdate DESC")
    fun getRiverLevelList(): LiveData<List<RiverLevelDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiverLevelList(riverLevelList: List<RiverLevelDbModel>)
}