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
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.cviz.features.startup.data.AreaData
import com.chrisa.cviz.features.startup.data.StartupDataSource
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class StartupUseCaseTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val dataSynchroniser: DataSynchroniser = mockk()
    private val bootstrapper: Bootstrapper = mockk()
    private val synchroniseDataWorkManager: SynchroniseDataWorkManager = mockk()
    private val startupDataSource: StartupDataSource = mockk()
    private val sut = StartupUseCase(
        TestCoroutineDispatchersImpl(testDispatcher),
        bootstrapper,
        dataSynchroniser,
        synchroniseDataWorkManager,
        startupDataSource
    )

    @Before
    fun setup() {
        coEvery { bootstrapper.execute() } just Runs
        coEvery { dataSynchroniser.syncData() } just Runs
        coEvery { synchroniseDataWorkManager.schedulePeriodicSync() } just Runs
        every { startupDataSource.dataCount() } returns AreaData(0, 0)
    }

    @Test
    fun `WHEN execute called THEN bootstrapper is executed`() =
        testDispatcher.runBlockingTest {
            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowHomeScreen)
            verify(exactly = 1) { bootstrapper.execute() }
        }

    @Test
    fun `GIVEN sync succeeds WHEN execute called THEN periodic sync is scheduled`() =
        testDispatcher.runBlockingTest {
            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowHomeScreen)
            verify(exactly = 1) { synchroniseDataWorkManager.schedulePeriodicSync() }
        }

    @Test
    fun `GIVEN no data WHEN area list fails to sync THEN no data error is returned`() =
        testDispatcher.runBlockingTest {
            coEvery { dataSynchroniser.syncData() } throws IOException()

            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowFatalError)
            verify(exactly = 0) { synchroniseDataWorkManager.schedulePeriodicSync() }
        }

    @Test
    fun `GIVEN no data WHEN area summary fails to sync THEN no data error is returned`() =
        testDispatcher.runBlockingTest {
            coEvery { dataSynchroniser.syncData() } throws IOException()

            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowFatalError)
            verify(exactly = 0) { synchroniseDataWorkManager.schedulePeriodicSync() }
        }

    @Test
    fun `GIVEN only area data is present WHEN area list fails to sync THEN no data error is returned`() =
        testDispatcher.runBlockingTest {
            every { startupDataSource.dataCount() } returns AreaData(1, 0)
            coEvery { dataSynchroniser.syncData() } throws IOException()

            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowFatalError)
            verify(exactly = 0) { synchroniseDataWorkManager.schedulePeriodicSync() }
        }

    @Test
    fun `GIVEN only area summary is present WHEN area list fails to sync THEN no data error is returned`() =
        testDispatcher.runBlockingTest {
            every { startupDataSource.dataCount() } returns AreaData(0, 1)
            coEvery { dataSynchroniser.syncData() } throws IOException()

            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowFatalError)
            verify(exactly = 0) { synchroniseDataWorkManager.schedulePeriodicSync() }
        }

    @Test
    fun `GIVEN all data is present WHEN area list fails to sync THEN home screen is shown`() =
        testDispatcher.runBlockingTest {
            every { startupDataSource.dataCount() } returns AreaData(1, 1)
            coEvery { dataSynchroniser.syncData() } throws IOException()

            val result = sut.execute()

            assertThat(result).isEqualTo(StartupResult.ShowHomeScreen)
            verify(exactly = 0) { synchroniseDataWorkManager.schedulePeriodicSync() }
        }
}
