package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.chat.MessageItem
import io.reactivex.Flowable

@Dao
interface ChatMessageDao {
    @Query("INSERT INTO chat_message (id) VALUES(1000000000)")
    fun insertValue()

    @Query("DELETE FROM chat_message WHERE id = 1000000000")
    fun deleteValue()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageList(messageList: List<MessageItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageItem): Long

    @Query("SELECT * FROM chat_message WHERE id IN (SELECT Max(id) FROM chat_message where senderUserId = :userId OR companionUserId = :userId GROUP BY companionUserId, senderUserId )")
    fun getAllLastMessages(userId: Int): LiveData<List<MessageItem>>

    @Query("SELECT id FROM chat_message WHERE isSent = 1 order by id desc limit 1")
    fun getChatMessageLastId(): LiveData<Int>

    @Query("SELECT count(*) FROM chat_message WHERE isViewed = 0 and (senderUserId = :senderId AND companionUserId =:companionId)")
    fun getNotViewedCount(senderId: Int, companionId: Int): Int

    @Query("SELECT count(*) FROM chat_message WHERE isViewed = 0 AND senderUserId != :currentUserId")
    fun getNewChatMessageCount(currentUserId: Int): LiveData<Int>

    @Query("UPDATE chat_message SET isViewed = 1 WHERE senderUserId = :senderId AND isViewed = 0")
    suspend fun setMessageViewedBy(senderId: Int)

    @Query("SELECT * FROM chat_message WHERE ((senderUserId = :senderId AND companionUserId =:companionId) OR (senderUserId = :companionId AND companionUserId =:senderId))")
    fun getMessageListBy(senderId: Int, companionId: Int): LiveData<List<MessageItem>>

    @Query("UPDATE chat_message SET isSent = 1, isViewed = 1, id = :serverId WHERE id == :id")
    fun setMessageSentBy(id: Int, serverId: Int)

    @Query("SELECT * FROM chat_message WHERE isSent = 0")
    fun getNotSendedMessageList(): LiveData<List<MessageItem>>

    @Query("DELETE FROM chat_message WHERE id = :id")
    suspend fun deleteMessageBy(id: Int)
}