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
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SynchroniseDataWorkManagerTest {

    private val workRequestFactory = mockk<WorkRequestFactory>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val syncTimeHelper = mockk<SyncTimeHelper>(relaxed = true)
    private val synchronisationPreferences = mockk<SynchronisationPreferences>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val periodicWorkRequestBuilder = periodicWorkRequest()
    private val sut = SynchroniseDataWorkManager(
        workManager,
        workRequestFactory,
        TestCoroutineDispatchersImpl(testDispatcher),
        synchronisationPreferences,
        syncTimeHelper
    )

    @Before
    fun setup() {
        val workInfosByTagRequest = mockk<ListenableFuture<List<WorkInfo>>> {
            every { isDone } returns true
            every { get() } returns emptyList()
        }
        every {
            workRequestFactory.periodicWorkRequest(
                any(),
                any(),
                any()
            )
        } returns periodicWorkRequestBuilder
        every { workManager.getWorkInfosByTag(SynchroniseDataWorkManager.SYNC_DATA_TAG) } returns workInfosByTagRequest
        every { synchronisationPreferences.refreshDataInBackground() } returns true
    }

    @Test
    fun `GIVEN refresh in background disabled jobs WHEN syncData THEN periodic sync is not enqueued`() =
        testDispatcher.runBlockingTest {
            every { synchronisationPreferences.refreshDataInBackground() } returns false

            sut.schedulePeriodicSync()

            verify(exactly = 0) {
                workManager.enqueueUniquePeriodicWork(
                    SynchroniseDataWorkManager.SYNC_DATA_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    any()
                )
            }
        }

    @Test
    fun `GIVEN cancelled jobs WHEN syncData THEN periodic sync is enqueued`() =
        testDispatcher.runBlockingTest {
            val workInfo = mockk<WorkInfo> {
                every { state } returns WorkInfo.State.CANCELLED
            }
            val workInfosByTagRequest = mockk<ListenableFuture<List<WorkInfo>>>() {
                every { isDone } returns true
                every { get() } returns listOf(workInfo)
            }
            every { workManager.getWorkInfosByTag(SynchroniseDataWorkManager.SYNC_DATA_TAG) } returns workInfosByTagRequest

            sut.schedulePeriodicSync()

            verify(exactly = 1) {
                workManager.enqueueUniquePeriodicWork(
                    SynchroniseDataWorkManager.SYNC_DATA_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWorkRequestBuilder
                )
            }
        }

    @Test
    fun `GIVEN enqueued jobs WHEN syncData THEN periodic sync is not enqueued`() =
        testDispatcher.runBlockingTest {
            val workInfo = mockk<WorkInfo> {
                every { state } returns WorkInfo.State.ENQUEUED
            }
            val workInfosByTagRequest = mockk<ListenableFuture<List<WorkInfo>>>() {
                every { isDone } returns true
                every { get() } returns listOf(workInfo)
            }
            every { workManager.getWorkInfosByTag(SynchroniseDataWorkManager.SYNC_DATA_TAG) } returns workInfosByTagRequest

            sut.schedulePeriodicSync()

            verify(exactly = 0) {
                workManager.enqueueUniquePeriodicWork(
                    SynchroniseDataWorkManager.SYNC_DATA_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWorkRequestBuilder
                )
            }
        }

    @Test
    fun `GIVEN no scheduled jobs WHEN syncData THEN periodic sync is enqueued`() =
        testDispatcher.runBlockingTest {
            sut.schedulePeriodicSync()

            verify(exactly = 1) {
                workManager.enqueueUniquePeriodicWork(
                    SynchroniseDataWorkManager.SYNC_DATA_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWorkRequestBuilder
                )
            }
        }

    @Test
    fun `WHEN cancel work THEN all sync jobs are cancelled`() =
        testDispatcher.runBlockingTest {
            sut.cancelWork()

            verify(exactly = 1) {
                workManager.cancelAllWorkByTag(SynchroniseDataWorkManager.SYNC_DATA_TAG)
            }
        }

    @Test
    fun `GIVEN refresh data in background is disabled WHEN toggleRefresh THEN all sync jobs are cancelled`() =
        testDispatcher.runBlockingTest {
            every { synchronisationPreferences.refreshDataInBackground() } returns false

            sut.toggleRefresh()

            verify(exactly = 1) {
                workManager.cancelAllWorkByTag(SynchroniseDataWorkManager.SYNC_DATA_TAG)
            }
        }

    @Test
    fun `GIVEN refresh data in background is enabled WHEN toggleRefresh THEN sync jobs is created`() =
        testDispatcher.runBlockingTest {
            sut.toggleRefresh()

            verify(exactly = 1) {
                workManager.enqueueUniquePeriodicWork(
                    SynchroniseDataWorkManager.SYNC_DATA_TAG,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    periodicWorkRequestBuilder
                )
            }
        }

    companion object {
        private fun periodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SynchroniseDataWorker>(
                repeatInterval = 7,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 30,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .addTag(SynchroniseDataWorkManager.SYNC_DATA_TAG)
                .build()
        }
    }
}
