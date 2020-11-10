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

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkRequestFactory @Inject constructor() {

    fun periodicWorkRequest(
        repeatIntervalSeconds: Long,
        flexTimeIntervalSeconds: Long,
        initialDelaySeconds: Long
    ): PeriodicWorkRequest {
        val data = Data.Builder()
            .putBoolean(SynchroniseDataWorker.SHOW_NOTIFICATION_KEY, true)
            .build()

        return PeriodicWorkRequestBuilder<SynchroniseDataWorker>(
            repeatInterval = repeatIntervalSeconds,
            repeatIntervalTimeUnit = TimeUnit.MILLISECONDS,
            flexTimeInterval = flexTimeIntervalSeconds,
            flexTimeIntervalUnit = TimeUnit.MILLISECONDS
        )
            .addTag(SynchroniseDataWorkManager.SYNC_DATA_TAG)
            .setConstraints(workConstraints())
            .setInitialDelay(initialDelaySeconds, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
    }

    private fun workConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
}
