package com.intas.metrolog.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.ChatItem
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.util.Util

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val messageList =
        db.chatMessageDao().getAllLastMessages(Util.authUser?.userId ?: 0)

    val chatItemList: LiveData<List<ChatItem>> = Transformations.switchMap(messageList) {
        createChatItemsLiveData(it)
    }

    private fun getNotViewedMessagesCount(senderId: Int, companionId: Int): Int {
        return db.chatMessageDao().getNotViewedCount(senderId, companionId)
    }

    private fun getCompanionById(companionId: Int): UserItem? {
        return db.userDao().getUserById(companionId)
    }

    private fun createChatItemsLiveData(list: List<MessageItem>): LiveData<List<ChatItem>> {
        return liveData {
            val chatItemList = mutableListOf<ChatItem>()
            list.forEach { message ->
                message.senderUserId?.let { messageSenderId ->
                    val companion: UserItem?
                    val currentUserId = Util.authUser?.userId

                    companion = if (messageSenderId != currentUserId) {
                        getCompanionById(messageSenderId)
                    } else {
                        val companionId = message.companionUserId ?: 0
                        getCompanionById(companionId)
                    }

                    companion?.let {
                        val messageText = message.message ?: ""
                        val messageId = message.id ?: 0
                        val messageDateTime = message.dateTime ?: 0

                        val chatItem = ChatItem(
                            id = messageId,
                            lastMessage = messageText,
                            companion = it,
                            lastMessageDate = messageDateTime
                        )
                        chatItemList.add(chatItem)
                    }
                }
                chatItemList.removeAll { chatItem -> chatItemList.any { chatItem.companion == it.companion && it.id > chatItem.id } }
                chatItemList.sortByDescending {
                    it.lastMessageDate
                }

                chatItemList.forEach {
                    it.notViewedMessageCount = getNotViewedMessagesCount(
                        it.companion.id,
                        Util.authUser?.userId ?: 0
                    )
                }
            }
            emit(chatItemList)
        }
    }
}
