package com.intas.metrolog.ui.chat

import android.app.Application
import androidx.lifecycle.*
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.ChatItem
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val messageList =
        db.chatMessageDao().getAllLastMessages(Util.authUser?.userId ?: 0).distinctUntilChanged()

    val chatItemList: LiveData<List<ChatItem>> = messageList.switchMap {
        liveData(viewModelScope.coroutineContext) {
            emit(createChatItems(it))
        }
    }

    private fun getNotViewedMessagesCount(senderId: Int, companionId: Int): Int {
        return db.chatMessageDao().getNotViewedCount(senderId, companionId)
    }

    private fun getCompanionById(companionId: Int): UserItem? {
        return db.userDao().getUserById(companionId)
    }

    private suspend fun createChatItems(list: List<MessageItem>): List<ChatItem> {
        return withContext(Dispatchers.IO) {

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
                            lastMessageDate = messageDateTime,
                            companion = it
                        )
                        chatItemList.add(chatItem)
                    }
                    chatItemList.removeAll { chatItem -> chatItemList.any { chatItem.companion == it.companion && it.id > chatItem.id } }
                    chatItemList.sortByDescending {
                        it.lastMessageDate
                    }

                    chatItemList.forEach {
                        it.notViewedMessageCount = getNotViewedMessagesCount(
                            companion?.id ?: 0,
                            currentUserId ?: 0
                        )
                    }
                }
            }
            chatItemList
        }
    }
}
