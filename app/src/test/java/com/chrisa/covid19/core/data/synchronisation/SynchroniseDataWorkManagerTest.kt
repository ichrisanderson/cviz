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

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.TimeUnit
import org.junit.Test

class SynchroniseDataWorkManagerTest {

    private val workRequestFactory = mockk<WorkRequestFactory>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    val sut = SynchroniseDataWorkManager(workManager, workRequestFactory)

    @Test
    fun `WHEN syncData THEN immediate sync is enqueued`() {

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SynchroniseDataWorker>()
            .addTag(SynchroniseDataWorkManager.SYNC_DATA_ONE_SHOT)
            .build()

        every { workRequestFactory.oneTimeWorkRequest() } returns oneTimeWorkRequest

        sut.syncData()

        verify(exactly = 1) { workManager.enqueue(oneTimeWorkRequest) }
    }

    @Test
    fun `WHEN syncData THEN periodic sync is enqueued`() {

        val periodicWorkRequestBuilder = PeriodicWorkRequestBuilder<SynchroniseDataWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .addTag(SynchroniseDataWorkManager.SYNC_DATA)
            .build()

        every { workRequestFactory.periodicWorkRequest() } returns periodicWorkRequestBuilder

        sut.syncData()

        verify(exactly = 1) {
            workManager.enqueueUniquePeriodicWork(
                SynchroniseDataWorkManager.SYNC_DATA,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequestBuilder
            )
        }
    }
}
