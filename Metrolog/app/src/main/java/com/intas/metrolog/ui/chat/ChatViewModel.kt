package com.intas.metrolog.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.util.Util

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val messageList = db.chatMessageDao().getAllLastMessages(Util.authUser?.userId ?: 0)

    init {

    }

    fun getNotViewedMessagesCount(senderId: Int, companionId: Int): Int {
        return db.chatMessageDao().getNotViewedCount(senderId, companionId)
    }

    fun getCompanionById(companionId: Int): UserItem? {
        return db.userDao().getUserById(companionId)
    }

    override fun onCleared() {
        super.onCleared()
    }
}