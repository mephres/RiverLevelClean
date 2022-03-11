package me.kdv.riverlevel.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import me.kdv.riverlevel.data.database.RiverLevelDao
import me.kdv.riverlevel.data.database.mapper.RiverMapper
import me.kdv.riverlevel.domain.RiverInfo
import me.kdv.riverlevel.domain.RiverLevelRepository
import me.kdv.riverlevel.workers.RefreshDataWorker
import me.kdv.riverlevel.workers.RefreshDataWorker.Companion.NAME
import javax.inject.Inject

class RiverLevelRepositoryImpl @Inject constructor(
    private val mapper: RiverMapper,
    private val riverLevelDao: RiverLevelDao,
    private val application: Application
): RiverLevelRepository {
    override fun getRiverLevelList(): LiveData<List<RiverInfo>> {
        return Transformations.map(riverLevelDao.getRiverLevelList()) {
            it.map {
                mapper.mapDbModelToEntity(it)
            }
        }
    }

    override fun loadData() {
        val workManager = WorkManager.getInstance(application)
        workManager.enqueueUniqueWork(
            NAME,
            ExistingWorkPolicy.REPLACE,
            RefreshDataWorker.makeRequest()
        )
    }
}