package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem

@Dao
interface EventPhotoDao {
    @Query("SELECT * FROM event_photo WHERE isSended = 0 ORDER BY id ASC LIMIT 1")
    fun getNotSendedEventPhotoList(): LiveData<List<EventPhotoItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventPhoto(eventPhoto: EventPhotoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventPhoto(eventPhoto: EventPhotoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventPhotoList(eventPhotoList: List<EventPhotoItem>)

    @Query("UPDATE event_photo SET isSended = 1 WHERE id = :id")
    suspend fun setEventPhotoSendedById(id: Long)
}