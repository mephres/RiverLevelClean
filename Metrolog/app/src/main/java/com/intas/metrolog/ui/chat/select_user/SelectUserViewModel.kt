package com.intas.metrolog.ui.chat.select_user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.UserItem

class SelectUserViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    val chatUserList = db.userDao().getUserList()

    fun getCompanionById(companionId: Int): UserItem? {
        return db.userDao().getUserById(companionId)
    }
}