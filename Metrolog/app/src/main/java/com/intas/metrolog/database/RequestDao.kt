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
    suspend fun insertRequestList(requestList: List<RequestItem>): List<Long>

    @Query("SELECT * FROM request ORDER BY id DESC")
    fun getAllRequest(): LiveData<List<RequestItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(requestItem: RequestItem): Long

    @Query("UPDATE request SET equipInfo = :equipInfo WHERE id = :id")
    fun updateRequestEquipInfo(id: Long, equipInfo: String): Int

    @Query("SELECT * FROM request WHERE isSended = 0 ORDER BY id")
    fun getNotSendedRequestList(): LiveData<List<RequestItem>>

    @Query("UPDATE request SET isSended = 1, id = :serverId WHERE id = :id")
    suspend fun setRequestSendedById(id: Long, serverId: Long)

    @Query("DELETE FROM request WHERE id = :id")
    suspend fun deleteRequestById(id: Long)
}