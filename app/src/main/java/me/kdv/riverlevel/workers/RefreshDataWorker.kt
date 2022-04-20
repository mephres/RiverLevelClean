package me.kdv.riverlevel.workers

import android.content.Context
import android.util.Config
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.delay
import me.kdv.riverlevel.data.database.AppDatabase
import me.kdv.riverlevel.data.database.RiverLevelDao
import me.kdv.riverlevel.data.database.mapper.RiverMapper
import me.kdv.riverlevel.data.network.ApiFactory
import me.kdv.riverlevel.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RefreshDataWorker @Inject constructor(
    context: Context,
    workerParameters: WorkerParameters,
    private val riverLevelDao: RiverLevelDao,
    private val apiService: ApiService,
    private val mapper: RiverMapper
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        while (true) {
            try {
                val response = apiService.getRiverLevel().string()
                val riverLevelDtoList = mapper.mapHtmlContainerToListRiverLevel(response)
                val dbModelList = riverLevelDtoList.map {
                    mapper.mapDtoToDbModel(it)
                }
                riverLevelDao.insertRiverLevelList(dbModelList)
            } catch (e: Exception) {
                Log.d("ERROR", e.localizedMessage)
            }
            delay(30000)
        }
    }

    companion object {
        const val NAME = "RefreshDataWorker"

        fun makeRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<RefreshDataWorker>()
                .setConstraints(constraints)
                .build()
        }
    }

    class Factory @Inject constructor(
        private val riverLevelDao: RiverLevelDao,
        private val apiService: ApiService,
        private val mapper: RiverMapper
    ) : ChildWorkerFactory {
        override fun create(
            context: Context,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return RefreshDataWorker(context, workerParameters, riverLevelDao, apiService, mapper)
        }
    }
}