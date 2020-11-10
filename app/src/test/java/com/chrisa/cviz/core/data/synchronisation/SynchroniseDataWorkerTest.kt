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
import androidx.work.WorkerParameters
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class SynchroniseDataWorkerTest {

    private val dataSynchroniser: DataSynchroniser = mockk(relaxed = true)
    private val syncNotification: SyncNotification = mockk(relaxed = true)
    private val synchronisationPreferences = mockk<SynchronisationPreferences>(relaxed = true)
    private val synchroniseDataWorkManager = mockk<SynchroniseDataWorkManager>(relaxed = true)
    private val params: WorkerParameters = mockk(relaxed = true)
    private val context: Context = mockk()
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)

    private val sut = SynchroniseDataWorker(
        context,
        params,
        TestCoroutineDispatchersImpl(testDispatcher),
        dataSynchroniser,
        synchronisationPreferences,
        syncNotification,
        synchroniseDataWorkManager
    )

    @Test
    fun `WHEN work is run THEN synchronisers are launched`() = testCoroutineScope.runBlockingTest {
        every { synchronisationPreferences.showNotificationAfterDataRefresh() } returns true
        every {
            params.inputData.getBoolean(
                SynchroniseDataWorker.SHOW_NOTIFICATION_KEY,
                false
            )
        } returns true

        sut.doWork()

        coVerify { dataSynchroniser.syncData() }
    }

    @Test
    fun `GIVEN work succeeds WHEN work is run THEN period sync is rescheduled`() =
        testCoroutineScope.runBlockingTest {
            every { synchronisationPreferences.showNotificationAfterDataRefresh() } returns true
            every {
                params.inputData.getBoolean(
                    SynchroniseDataWorker.SHOW_NOTIFICATION_KEY,
                    false
                )
            } returns true

            sut.doWork()

            verify { synchroniseDataWorkManager.reschedulePeriodicSync() }
        }

    @Test
    fun `GIVEN notification disabled WHEN work is run THEN notification is not shown`() =
        testCoroutineScope.runBlockingTest {
            every { synchronisationPreferences.showNotificationAfterDataRefresh() } returns false
            every {
                params.inputData.getBoolean(
                    SynchroniseDataWorker.SHOW_NOTIFICATION_KEY,
                    false
                )
            } returns true

            sut.doWork()

            coVerify { dataSynchroniser.syncData() }
            verify(exactly = 0) { syncNotification.showSuccess() }
        }

    @Test
    fun `GIVEN notification enabled WHEN work is run THEN notification is not shown`() =
        testCoroutineScope.runBlockingTest {
            every { synchronisationPreferences.showNotificationAfterDataRefresh() } returns true
            every {
                params.inputData.getBoolean(
                    SynchroniseDataWorker.SHOW_NOTIFICATION_KEY,
                    false
                )
            } returns true

            sut.doWork()

            coVerify { dataSynchroniser.syncData() }
            verify(exactly = 1) { syncNotification.showSuccess() }
        }

    @Test
    fun `GIVEN notification parameter disabled WHEN work is run THEN notification is not shown`() =
        testCoroutineScope.runBlockingTest {
            every { synchronisationPreferences.showNotificationAfterDataRefresh() } returns true
            every {
                params.inputData.getBoolean(
                    SynchroniseDataWorker.SHOW_NOTIFICATION_KEY,
                    false
                )
            } returns false

            sut.doWork()

            coVerify { dataSynchroniser.syncData() }
            verify(exactly = 0) { syncNotification.showSuccess() }
        }
}
