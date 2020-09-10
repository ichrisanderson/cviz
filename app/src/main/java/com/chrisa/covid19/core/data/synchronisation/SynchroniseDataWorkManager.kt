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

package com.chrisa.covid19.core.data.synchronisation

import androidx.concurrent.futures.await
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SynchroniseDataWorkManager @Inject constructor(
    private val workManager: WorkManager,
    private val workRequestFactory: WorkRequestFactory,
    private val coroutineDispatchers: CoroutineDispatchers
) {

    private val job = Job()

    fun syncData() {
        syncImmediate()
        schedulePeriodicSync()
    }

    private fun syncImmediate() {
        workManager.enqueue(workRequestFactory.oneTimeWorkRequest())
    }

    private fun schedulePeriodicSync() {
        CoroutineScope(coroutineDispatchers.io + job).launch {
            val workInfoRequest = workManager.getWorkInfosByTag(SYNC_DATA)
            val workInfo = workInfoRequest.await()
            if (workInfo.isEmpty())
                workManager
                    .enqueueUniquePeriodicWork(
                        SYNC_DATA,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        workRequestFactory.periodicWorkRequest()
                    )
        }
    }

    companion object {
        const val SYNC_DATA_ONE_SHOT = "SYNC_DATA_ONE_SHOT"
        const val SYNC_DATA = "SYNC_DATA"
    }
}

class WorkRequestFactory @Inject constructor() {

    fun oneTimeWorkRequest(): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<SynchroniseDataWorker>()
            .addTag(SynchroniseDataWorkManager.SYNC_DATA_ONE_SHOT)
            .setConstraints(workConstraints())
            .build()
    }

    fun periodicWorkRequest(): PeriodicWorkRequest {

        val data = Data.Builder()
            .putBoolean(SynchroniseDataWorker.SHOW_NOTIFICATION_KEY, true)
            .build()

        return PeriodicWorkRequestBuilder<SynchroniseDataWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .addTag(SynchroniseDataWorkManager.SYNC_DATA)
            .setConstraints(workConstraints())
            .setInputData(data)
            .build()
    }

    private fun workConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
}
