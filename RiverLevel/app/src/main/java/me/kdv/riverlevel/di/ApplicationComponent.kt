package me.kdv.riverlevel.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import me.kdv.riverlevel.presentation.MainActivity

@Component(
    modules = [DataModule::class, ViewModelModule::class]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}