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

import androidx.concurrent.futures.await
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SynchroniseDataWorkManager @Inject constructor(
    private val workManager: WorkManager,
    private val workRequestFactory: WorkRequestFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val synchronisationPreferences: SynchronisationPreferences,
    private val syncTimeHelper: SyncTimeHelper
) {

    private val job = Job()

    fun schedulePeriodicSync() {
        CoroutineScope(coroutineDispatchers.io + job).launch {
            if (!synchronisationPreferences.refreshDataInBackground()) return@launch
            val workInfoRequest = workManager.getWorkInfosByTag(SYNC_DATA_TAG)
            val workInfo = workInfoRequest.await()
            val enqueuedWork = workInfo.filter { it.state == WorkInfo.State.ENQUEUED }
            if (enqueuedWork.isEmpty()) {
                enqueueWork()
            }
        }
    }

    fun reschedulePeriodicSync() {
        if (!synchronisationPreferences.refreshDataInBackground()) return
        enqueueWork()
    }

    private fun enqueueWork() {
        workManager.enqueueUniquePeriodicWork(
            SYNC_DATA_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequestFactory.periodicWorkRequest(
                repeatIntervalSeconds = TimeUnit.HOURS.toMillis(1),
                flexTimeIntervalSeconds = TimeUnit.MINUTES.toMillis(30),
                initialDelaySeconds = syncTimeHelper.timeToNextSyncInMillis()
            )
        )
    }

    fun cancelWork() {
        workManager.cancelAllWorkByTag(SYNC_DATA_TAG)
    }

    fun toggleRefresh() {
        val canRefresh = synchronisationPreferences.refreshDataInBackground()
        if (canRefresh) {
            schedulePeriodicSync()
        } else {
            cancelWork()
        }
    }

    companion object {
        const val SYNC_DATA_TAG = "SYNC_DATA"
    }
}
