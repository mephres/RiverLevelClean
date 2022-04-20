package me.kdv.riverlevel.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import me.kdv.riverlevel.data.database.AppDatabase
import me.kdv.riverlevel.data.database.RiverLevelDao
import me.kdv.riverlevel.data.database.mapper.RiverMapper
import me.kdv.riverlevel.data.network.ApiFactory
import me.kdv.riverlevel.data.network.ApiService
import javax.inject.Inject

class RefreshDataWorkerFactory @Inject constructor(
    private val riverLevelDao: RiverLevelDao,
    private val apiService: ApiService,
    private val mapper: RiverMapper
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return RefreshDataWorker(appContext, workerParameters, riverLevelDao, apiService, mapper)
    }
}