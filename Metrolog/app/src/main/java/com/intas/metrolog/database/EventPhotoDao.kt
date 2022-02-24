package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.event.event_photo.EventPhotoItem

@Dao
interface EventPhotoDao {
    @Query("SELECT * FROM event_photo WHERE isSended = 0 ORDER BY id")
    fun getNotSendedEventPhotoList(): LiveData<List<EventPhotoItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventPhoto(eventPhoto: EventPhotoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventPhoto(eventPhoto: EventPhotoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventPhotoList(eventPhotoList: List<EventPhotoItem>)

    @Query("UPDATE event_photo SET isSended = 1 WHERE id = :id")
    suspend fun setEventPhotoSendedById(id: Long)

    @Query("SELECT * FROM event_photo WHERE opId = :id ORDER BY datetime DESC")
    fun getEventPhotoListById(id: Long): LiveData<List<EventPhotoItem>>

    @Query("DELETE FROM event_photo WHERE opId = :id")
    suspend fun deleteEventPhotoByEventId(id: Long)
}