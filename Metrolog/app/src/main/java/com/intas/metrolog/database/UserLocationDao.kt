package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.userlocation.UserLocation

@Dao
interface UserLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLocation(userLocation: UserLocation): Long

    @Query("UPDATE user_location SET isSended = 1 WHERE id = :id")
    suspend fun setUserLocationSendedById(id: Long)

    @Query("SELECT * FROM user_location WHERE isSended = 0")
    fun getNotSendedUserLocationList(): LiveData<List<UserLocation>>

    @Query("DELETE FROM user_location")
    suspend fun deleteAllUserLocations()
}