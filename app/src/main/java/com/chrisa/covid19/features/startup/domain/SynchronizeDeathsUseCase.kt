package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.synchronization.DeathDataSynchronizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SynchronizeDeathsUseCase @Inject constructor(
    private val deathDataSynchronizer: DeathDataSynchronizer
) {
    suspend fun execute(syncScope: CoroutineScope) {
        syncScope.launch {
            deathDataSynchronizer.performSync()
        }
    }
}
