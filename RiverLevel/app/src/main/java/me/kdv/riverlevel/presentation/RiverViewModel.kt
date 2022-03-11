package me.kdv.riverlevel.presentation

import androidx.lifecycle.ViewModel
import me.kdv.riverlevel.domain.GetRiverLevelListUseCase
import me.kdv.riverlevel.domain.LoadDataUseCase
import javax.inject.Inject

class RiverViewModel @Inject constructor(
    private val getRiverLevelListUseCase: GetRiverLevelListUseCase,
    private val loadDataUseCase: LoadDataUseCase
) : ViewModel() {

    val riverInfoList = getRiverLevelListUseCase()

    init {
        loadDataUseCase()
    }
}