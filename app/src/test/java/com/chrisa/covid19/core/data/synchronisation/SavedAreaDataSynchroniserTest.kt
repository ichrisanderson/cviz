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
import com.chrisa.covid19.core.data.db.Constants
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SavedAreaDataSynchroniserTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataSynchroniser = mockk<AreaDataSynchroniser>()
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

            val onError: (error: Throwable) -> Unit = { }
            coEvery { areaDataSynchroniser.performSync(any(), any(), onError) } just Runs
            every { areaDao.allSavedAreas() } returns emptyList()

            sut.performSync(onError)

            coVerify(exactly = 1) { areaDataSynchroniser.performSync(Constants.UK_AREA_CODE, "overview", onError) }
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
            val onError: (error: Throwable) -> Unit = { }
            coEvery { areaDataSynchroniser.performSync(any(), any(), onError) } just Runs
            every { areaDao.allSavedAreas() } returns listOf(area1, area2)

            sut.performSync(onError)

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    area1.areaCode,
                    area1.areaType,
                    onError
                )
            }
            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    area2.areaCode,
                    area2.areaType,
                    onError
                )
            }
        }
}
