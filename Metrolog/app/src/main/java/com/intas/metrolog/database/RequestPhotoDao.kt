package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.request.RequestPhoto

@Dao
interface RequestPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestPhotoList(list: List<RequestPhoto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestPhoto(requestPhoto: RequestPhoto): Long

    @Query("SELECT * FROM requestPhoto WHERE isSended = 0 ORDER BY id ASC LIMIT 1")
    fun getNotSendedRequestPhotoList(): LiveData<List<RequestPhoto>>

    @Query("UPDATE requestPhoto SET isSended = 1, id = :serverId WHERE id = :id")
    suspend fun setRequestPhotoSendedById(id: Long, serverId: Long)

    @Query("UPDATE requestPhoto SET isSended = 0, requestId = :newRequestId WHERE requestId = :oldRequestId")
    suspend fun updateRequestPhoto(oldRequestId: Long, newRequestId: Long)
}