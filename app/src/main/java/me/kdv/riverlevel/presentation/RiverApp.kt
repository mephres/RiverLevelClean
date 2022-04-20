package me.kdv.riverlevel.presentation

import android.app.Application
import androidx.work.Configuration
import me.kdv.riverlevel.di.DaggerApplicationComponent
import me.kdv.riverlevel.workers.RiverWorkerFactory
import javax.inject.Inject

class RiverApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: RiverWorkerFactory

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()
    }

    override fun getWorkManagerConfiguration(): Configuration {

        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}