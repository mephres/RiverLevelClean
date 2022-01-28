package com.intas.metrolog.ui.operation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem

class OperationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    fun getEquip(equipId: Long): EquipItem? {
        return db.equipDao().getEquipItemById(equipId)
    }

    fun getCheckList(opId: Long): LiveData<List<EventOperationItem>> {
        return db.eventOperationDao().getCheckList(opId)
    }
}