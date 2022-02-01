package com.intas.metrolog.ui.chat.messages

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.event.EventItem

class MessagesViewModelFactory(
    private val companion: UserItem,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            return MessageViewModel(application, companion) as T
        }
        throw RuntimeException("Unknown view model class $modelClass")
    }
}