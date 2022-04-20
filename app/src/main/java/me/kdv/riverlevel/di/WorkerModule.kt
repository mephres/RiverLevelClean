package me.kdv.riverlevel.di

import androidx.lifecycle.ViewModel
import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import me.kdv.riverlevel.presentation.RiverViewModel
import me.kdv.riverlevel.workers.ChildWorkerFactory
import me.kdv.riverlevel.workers.RefreshDataWorker

@Module
interface WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(RefreshDataWorker::class)
    fun bindRefreshDataWorkerFactory(worker: RefreshDataWorker.Factory): ChildWorkerFactory
}