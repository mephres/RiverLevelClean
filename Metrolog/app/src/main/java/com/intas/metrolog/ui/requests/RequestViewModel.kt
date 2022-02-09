package com.intas.metrolog.ui.requests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intas.metrolog.database.AppDatabase

class RequestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    private val _scroll = MutableLiveData<Int>()
    val scroll: LiveData<Int> get() = _scroll

    val requestList = db.requestDao().getAllRequest()

    fun onScrolled(dy: Int) {
        _scroll.value = dy
    }
}