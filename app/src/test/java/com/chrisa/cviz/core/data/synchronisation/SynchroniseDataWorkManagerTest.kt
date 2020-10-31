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

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class SynchroniseDataWorkManagerTest {

    private val workRequestFactory = mockk<WorkRequestFactory>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val sut = SynchroniseDataWorkManager(
        workManager,
        workRequestFactory,
        TestCoroutineDispatchersImpl(testDispatcher)
    )

    @Test
    fun `WHEN syncData THEN immediate sync is enqueued`() = testDispatcher.runBlockingTest {

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SynchroniseDataWorker>()
            .addTag(SynchroniseDataWorkManager.SYNC_DATA_ONE_SHOT)
            .build()

        every { workRequestFactory.oneTimeWorkRequest() } returns oneTimeWorkRequest

        sut.syncData()

        verify(exactly = 1) { workManager.enqueue(oneTimeWorkRequest) }
    }

    @Test
    fun `GIVEN scheduled jobs WHEN syncData THEN periodic sync is enqueued`() = testDispatcher.runBlockingTest {

        val periodicWorkRequestBuilder = PeriodicWorkRequestBuilder<SynchroniseDataWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .addTag(SynchroniseDataWorkManager.SYNC_DATA)
            .build()

        val workInfo = mockk<WorkInfo>()

        val workInfosByTagRequest = mockk<ListenableFuture<List<WorkInfo>>>() {
            every { isDone } returns true
            every { get() } returns listOf(workInfo)
        }

        every { workRequestFactory.periodicWorkRequest() } returns periodicWorkRequestBuilder
        every { workManager.getWorkInfosByTag(SynchroniseDataWorkManager.SYNC_DATA) } returns workInfosByTagRequest

        sut.syncData()

        verify(exactly = 0) {
            workManager.enqueueUniquePeriodicWork(
                SynchroniseDataWorkManager.SYNC_DATA,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequestBuilder
            )
        }
    }

    @Test
    fun `GIVEN no scheduled jobs WHEN syncData THEN periodic sync is enqueued`() = testDispatcher.runBlockingTest {

        val periodicWorkRequestBuilder = PeriodicWorkRequestBuilder<SynchroniseDataWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 30,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .addTag(SynchroniseDataWorkManager.SYNC_DATA)
            .build()

        val workInfosByTagRequest = mockk<ListenableFuture<List<WorkInfo>>>() {
            every { isDone } returns true
            every { get() } returns emptyList()
        }

        every { workRequestFactory.periodicWorkRequest() } returns periodicWorkRequestBuilder
        every { workManager.getWorkInfosByTag(SynchroniseDataWorkManager.SYNC_DATA) } returns workInfosByTagRequest

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
