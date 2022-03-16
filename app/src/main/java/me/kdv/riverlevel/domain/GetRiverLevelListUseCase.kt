package me.kdv.riverlevel.domain

import javax.inject.Inject

class GetRiverLevelListUseCase @Inject constructor(private val repository: RiverLevelRepository) {
    operator fun invoke() = repository.getRiverLevelList()
}