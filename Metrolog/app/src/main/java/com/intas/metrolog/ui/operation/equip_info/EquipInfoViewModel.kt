package com.intas.metrolog.ui.operation.equip_info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipInfo
import kotlinx.coroutines.launch

class EquipInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)

    fun getEquipInfoById(equipId: Long): LiveData<List<EquipInfo>> {
        return db.equipInfoDao().getNotCheckedEquipInfoById(equipId)
    }

    fun updateEquipInfo(list: List<EquipInfo>) {
        viewModelScope.launch {
            db.equipInfoDao().insertEquipInfoList(list)
        }
    }
}