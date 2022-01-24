package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem

@Dao
interface RequestStatusDao {
    @Query("SELECT * FROM requestStatus")
    fun getAllRequestStatus(): LiveData<List<RequestStatusItem>>

    @Query("SELECT * FROM requestStatus WHERE id = :id")
    fun getRequestStatusById(id: Int): RequestStatusItem?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRequestStatus(requestStatus: RequestStatusItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestStatus(requestStatus: RequestStatusItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestStatusList(requestStatusList: List<RequestStatusItem>)

    @Query("DELETE FROM requestStatus")
    suspend fun deleteAllRequestStatus()
}