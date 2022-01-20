package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.request.RequestItem

@Dao
interface RequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestList(requestList: List<RequestItem>)

    @Query("SELECT * FROM request ORDER BY id DESC")
    fun getAllRequest(): LiveData<List<RequestItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(requestItem: RequestItem)
}