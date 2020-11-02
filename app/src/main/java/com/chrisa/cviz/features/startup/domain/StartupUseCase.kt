/*
 * Copyright 2020 Chris Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chrisa.cviz.features.startup.domain

import com.chrisa.cviz.core.data.synchronisation.AreaListSynchroniser
import com.chrisa.cviz.core.data.synchronisation.AreaSummaryDataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.SavedAreaDataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.SynchroniseDataWorkManager
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.startup.data.StartupDataSource
import javax.inject.Inject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class StartupUseCase @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val areaListSynchroniser: AreaListSynchroniser,
    private val areaSummaryDataSynchroniser: AreaSummaryDataSynchroniser,
    private val savedAreaDataSynchroniser: SavedAreaDataSynchroniser,
    private val synchroniseDataWorkManager: SynchroniseDataWorkManager,
    private val startupDataSource: StartupDataSource
) {

    suspend fun execute(): StartupResult = withContext(coroutineDispatchers.io) {
        try {
            syncData()
            synchroniseDataWorkManager.schedulePeriodicSync()
            return@withContext StartupResult.ShowHomeScreen
        } catch (t: Throwable) {
            val dataCount = startupDataSource.dataCount()
            return@withContext when {
                dataCount.areaSummaryEntities > 0 && dataCount.areaData > 0 -> StartupResult.ShowHomeScreenWithSyncError
                else -> StartupResult.ShowFatalError
            }
        }
    }

    private suspend fun syncData() {
        coroutineScope {
            val jobs = listOf(
                async(start = CoroutineStart.LAZY) { syncAreaSummaries() },
                async(start = CoroutineStart.LAZY) { syncAreaList() },
                async(start = CoroutineStart.LAZY) { syncSavedAreas() }
            )
            jobs.awaitAll()
        }
    }

    private suspend fun syncAreaList() =
        areaListSynchroniser.performSync()

    private suspend fun syncAreaSummaries() =
        areaSummaryDataSynchroniser.performSync()

    private suspend fun syncSavedAreas() =
        savedAreaDataSynchroniser.performSync()
}
