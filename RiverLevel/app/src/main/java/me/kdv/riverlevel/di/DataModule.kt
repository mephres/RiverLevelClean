package me.kdv.riverlevel.di

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import me.kdv.riverlevel.data.RiverLevelRepositoryImpl
import me.kdv.riverlevel.data.database.AppDatabase
import me.kdv.riverlevel.data.database.RiverLevelDao
import me.kdv.riverlevel.domain.RiverLevelRepository

@Module
interface DataModule {
    @Binds
    fun bindRiverRepository(impl: RiverLevelRepositoryImpl): RiverLevelRepository

    companion object {
        @Provides
        fun provideRiverInfoDao(application: Application): RiverLevelDao {
            return AppDatabase.getInstance(application).riverLevelDao()
        }
    }
}