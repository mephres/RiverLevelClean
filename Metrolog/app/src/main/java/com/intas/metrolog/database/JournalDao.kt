package com.intas.metrolog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.JournalItem

@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journalItem: JournalItem)

    @Query("DELETE FROM journal WHERE dateTime < :dateTime")
    suspend fun deleteJournalByDate(dateTime: Long)

    @Query("SELECT * FROM journal WHERE dateTime >= :startTime AND dateTime <= :endTime")
    suspend fun getJournalByDateRange(startTime: Long, endTime: Long): List<JournalItem>

    @Query("DELETE FROM journal")
    suspend fun deleteAllJournal()
}