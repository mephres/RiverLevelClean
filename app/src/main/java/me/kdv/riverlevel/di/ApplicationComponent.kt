package me.kdv.riverlevel.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import me.kdv.riverlevel.presentation.MainActivity
import me.kdv.riverlevel.presentation.RiverApp

@ApplicationScope
@Component(
    modules = [DataModule::class, ViewModelModule::class, WorkerModule::class]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun inject(application: RiverApp)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}