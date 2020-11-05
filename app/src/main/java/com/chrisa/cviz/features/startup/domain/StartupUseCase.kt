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

import com.chrisa.cviz.core.data.db.Bootstrapper
import com.chrisa.cviz.core.data.synchronisation.DataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.SynchroniseDataWorkManager
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.startup.data.StartupDataSource
import javax.inject.Inject
import kotlinx.coroutines.withContext

class StartupUseCase @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val bootstrapper: Bootstrapper,
    private val dataSynchroniser: DataSynchroniser,
    private val synchroniseDataWorkManager: SynchroniseDataWorkManager,
    private val startupDataSource: StartupDataSource
) {

    suspend fun execute(): StartupResult = withContext(coroutineDispatchers.io) {
        try {
            bootstrapper.execute()
            dataSynchroniser.syncData()
            synchroniseDataWorkManager.schedulePeriodicSync()
            StartupResult.ShowHomeScreen
        } catch (t: Throwable) {
            val areaData = startupDataSource.dataCount()
            when {
                areaData.isNotEmpty() -> StartupResult.ShowHomeScreen
                else -> StartupResult.ShowFatalError
            }
        }
    }
}
