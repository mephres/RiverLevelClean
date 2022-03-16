package me.kdv.riverlevel.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import me.kdv.riverlevel.data.database.AppDatabase
import me.kdv.riverlevel.data.database.mapper.RiverMapper
import me.kdv.riverlevel.data.network.ApiFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefreshDataWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    private val riverLevelDao = AppDatabase.getInstance(context).riverLevelDao()
    private val apiService = ApiFactory.apiService

    private val mapper = RiverMapper()

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
            return OneTimeWorkRequestBuilder<RefreshDataWorker>().build()
        }
    }
}