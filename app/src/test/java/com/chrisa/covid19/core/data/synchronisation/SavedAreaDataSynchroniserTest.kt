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

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDao
import com.chrisa.covid19.core.data.db.AreaEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import timber.log.Timber

@ExperimentalCoroutinesApi
class SavedAreaDataSynchroniserTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataSynchroniser = mockk<UnsafeAreaDataSynchroniser>()
    private val areaDao = mockk<AreaDao>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: SavedAreaDataSynchroniser

    @Before
    fun setup() {
        every { appDatabase.areaDao() } returns areaDao

        sut = SavedAreaDataSynchroniser(areaDataSynchroniser, appDatabase)
    }

    @Test
    fun `GIVEN no saved areas WHEN performSync THEN no area data is synced`() =
        testDispatcher.runBlockingTest {

            every { areaDao.allSavedAreas() } returns emptyList()

            sut.performSync()

            coVerify(exactly = 0) { areaDataSynchroniser.performSync(any(), any()) }
        }

    @Test
    fun `GIVEN saved areas WHEN performSync THEN area data is synced`() =
        testDispatcher.runBlockingTest {

            val area1 = AreaEntity(
                areaName = "UK",
                areaCode = "1234",
                areaType = "overview"
            )
            val area2 = AreaEntity(
                areaName = "Scotland",
                areaCode = "12345",
                areaType = "nation"
            )
            coEvery { areaDataSynchroniser.performSync(any(), any()) } just Runs
            every { areaDao.allSavedAreas() } returns listOf(area1, area2)

            sut.performSync()

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    area1.areaCode,
                    area1.areaType
                )
            }
            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    area2.areaCode,
                    area2.areaType
                )
            }
        }

    @Test
    fun `GIVEN areaDataSynchroniser throws WHEN performSync THEN error is handled`() =
        testDispatcher.runBlockingTest {

            mockkStatic(Timber::class)
            val area1 = AreaEntity(
                areaName = "UK",
                areaCode = "1234",
                areaType = "overview"
            )
            val area2 = AreaEntity(
                areaName = "Scotland",
                areaCode = "12345",
                areaType = "nation"
            )
            val error = IOException()
            every { areaDao.allSavedAreas() } returns listOf(area1, area2)
            coEvery { areaDataSynchroniser.performSync(any(), any()) } throws error

            sut.performSync()

            verify { Timber.e(error, "Error syncing saved area ${area1.areaCode}") }
            verify { Timber.e(error, "Error syncing saved area ${area2.areaCode}") }
        }
}
