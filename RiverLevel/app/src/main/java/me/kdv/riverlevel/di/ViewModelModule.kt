package me.kdv.riverlevel.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import me.kdv.riverlevel.presentation.RiverViewModel

@Module
interface ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(RiverViewModel::class)
    fun bindRiverViewModel(viewModel: RiverViewModel): ViewModel
}