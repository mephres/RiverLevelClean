package com.intas.metrolog.ui.chat.messages

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.chat.MessageItem
import com.intas.metrolog.util.Util
import kotlinx.coroutines.launch

class MessageViewModel(
    private val application: Application,
    private val companion: UserItem
) : ViewModel() {

    private val db = AppDatabase.getInstance(application)

    fun getMessageList(): LiveData<List<MessageItem>> {
        return db.chatMessageDao().getMessageListBy(Util.authUser?.userId ?: 0, companion.id)
    }

    fun setChatMessageViewed(senderId: Int) {
        viewModelScope.launch {
            db.chatMessageDao().setMessageViewedBy(senderId)
        }
    }
}