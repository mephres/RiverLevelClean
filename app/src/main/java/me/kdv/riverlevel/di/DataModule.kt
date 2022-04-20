package me.kdv.riverlevel.di

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import me.kdv.riverlevel.data.RiverLevelRepositoryImpl
import me.kdv.riverlevel.data.database.AppDatabase
import me.kdv.riverlevel.data.database.RiverLevelDao
import me.kdv.riverlevel.data.network.ApiFactory
import me.kdv.riverlevel.data.network.ApiService
import me.kdv.riverlevel.domain.RiverLevelRepository

@Module
interface DataModule {
    @ApplicationScope
    @Binds
    fun bindRiverRepository(impl: RiverLevelRepositoryImpl): RiverLevelRepository

    companion object {
        @ApplicationScope
        @Provides
        fun provideRiverInfoDao(application: Application): RiverLevelDao {
            return AppDatabase.getInstance(application).riverLevelDao()
        }

        @ApplicationScope
        @Provides
        fun provideApiService(): ApiService {
            return ApiFactory.apiService
        }
    }
}