package com.intas.metrolog.ui.equip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem

class EquipViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    fun getEquipList(): LiveData<List<EquipItem>> {
        return db.equipDao().getAllEquip()
    }

}