package com.intas.metrolog.ui.events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.intas.metrolog.database.AppDatabase

class EventsViewModel(application: Application) : AndroidViewModel(application)  {
    private val db = AppDatabase.getInstance(application)

    val eventList = db.eventDao().getEventList()

}