package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.synchronization.CaseDataSynchronizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SynchronizeCasesUseCase @Inject constructor(
    private val caseDataSynchronizer: CaseDataSynchronizer
) {
    suspend fun execute(syncScope: CoroutineScope) {
        syncScope.launch {
            caseDataSynchronizer.performSync()
        }
    }
}
