package me.kdv.riverlevel.presentation

import android.app.Application
import androidx.work.Configuration
import me.kdv.riverlevel.data.database.AppDatabase
import me.kdv.riverlevel.data.database.mapper.RiverMapper
import me.kdv.riverlevel.data.network.ApiFactory
import me.kdv.riverlevel.di.DaggerApplicationComponent
import me.kdv.riverlevel.workers.RefreshDataWorkerFactory

class RiverApp : Application(), Configuration.Provider {
    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setWorkerFactory(
            RefreshDataWorkerFactory(
                AppDatabase.getInstance(this).riverLevelDao(),
                ApiFactory.apiService,
                RiverMapper()
            )
        ).build()
    }
}