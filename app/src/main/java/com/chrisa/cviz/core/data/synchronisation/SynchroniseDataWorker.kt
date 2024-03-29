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

package com.chrisa.cviz.core.data.synchronisation

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import kotlinx.coroutines.withContext

class SynchroniseDataWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val dataSynchroniser: DataSynchroniser,
    private val synchronisationPreferences: SynchronisationPreferences,
    private val syncNotification: SyncNotification,
    private val synchroniseDataWorkManager: SynchroniseDataWorkManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        try {
            dataSynchroniser.syncData()
            if (showNotification()) {
                syncNotification.showSuccess()
            }
            synchroniseDataWorkManager.reschedulePeriodicSync()
            Result.success()
        } catch (throwable: Throwable) {
            return@withContext Result.failure()
        }
    }

    private fun showNotification(): Boolean {
        return params.inputData.getBoolean(SHOW_NOTIFICATION_KEY, false) &&
            synchronisationPreferences.showNotificationAfterDataRefresh()
    }

    companion object {
        const val SHOW_NOTIFICATION_KEY = "ShowNotification"
    }
}
