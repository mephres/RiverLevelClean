package com.intas.metrolog.ui.events.select_event

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.event.EventItem

class SelectEventViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    fun getEventByRfid(rfid: String): LiveData<List<EventItem>> {
        return db.eventDao().getEventListByRfid(rfid)
    }

}