package me.kdv.riverlevel.domain

import androidx.lifecycle.LiveData
import me.kdv.riverlevel.data.database.RiverLevelDbModel

interface RiverLevelRepository {
    fun getRiverLevelList(): LiveData<List<RiverInfo>>
    fun loadData()
}