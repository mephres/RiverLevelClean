package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.event_comment.EventComment

@Dao
interface EventCommentDao {
    @Query("SELECT * FROM eventComment")
    fun getAllEventComment(): LiveData<List<EventComment>>

    @Query("SELECT * FROM eventComment where type = :type")
    fun getEventCommentByType(type: Int): List<EventComment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventCommentList(commentList: List<EventComment>)

    @Query("DELETE FROM eventComment")
    suspend fun deleteAllEventComment()

}