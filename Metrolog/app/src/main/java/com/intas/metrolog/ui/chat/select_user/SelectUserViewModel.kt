package com.intas.metrolog.ui.chat.select_user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.chat.MessageItem
import kotlinx.coroutines.launch

class SelectUserViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    val chatUserList = db.userDao().getUserList()

    fun insertMessage(message: MessageItem) {
        viewModelScope.launch {
            db.chatMessageDao().insertMessage(message)
        }
    }
}