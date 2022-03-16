package me.kdv.riverlevel.domain

import javax.inject.Inject

class LoadDataUseCase @Inject constructor(private val repository: RiverLevelRepository) {
    operator fun invoke() = repository.loadData()
}