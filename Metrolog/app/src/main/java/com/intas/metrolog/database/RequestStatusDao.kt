package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem

@Dao
interface RequestStatusDao {
    @Query("SELECT * FROM requestStatus")
    fun getRequestStatusList(): LiveData<List<RequestStatusItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRequestStatus(requestStatus: RequestStatusItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestStatus(requestStatus: RequestStatusItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequestStatusList(requestStatusList: List<RequestStatusItem>)

    @Query("DELETE FROM requestStatus")
    suspend fun deleteAllRequestStatus()
}