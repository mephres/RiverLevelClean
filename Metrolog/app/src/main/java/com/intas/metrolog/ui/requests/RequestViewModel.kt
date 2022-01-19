package com.intas.metrolog.ui.requests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.ui.requests.filter.RequestFilter

class RequestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    val requestList = db.requestDao().getAllRequest()
}