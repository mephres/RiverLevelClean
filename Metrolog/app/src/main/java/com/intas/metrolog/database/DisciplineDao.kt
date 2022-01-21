package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.discipline.DisciplineItem

@Dao
interface DisciplineDao {
    @Query("SELECT * FROM discipline order by name asc")
    fun getAllDiscipline(): LiveData<List<DisciplineItem>>

    @Query("SELECT * FROM discipline WHERE id = :id")
    fun getDisciplineById(id: Int): DisciplineItem?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDiscipline(discipline: DisciplineItem?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscipline(discipline: DisciplineItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisciplineList(disciplineList: List<DisciplineItem>)

    @Query("DELETE FROM discipline")
    suspend fun deleteAll()
}