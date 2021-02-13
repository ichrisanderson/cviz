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

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDao
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
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
    private val soaDataSynchroniser = mockk<SoaDataSynchroniser>()
    private val areaDao = mockk<AreaDao>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: SavedAreaDataSynchroniser

    @Before
    fun setup() {
        every { appDatabase.areaDao() } returns areaDao

        sut = SavedAreaDataSynchroniser(areaDataSynchroniser, soaDataSynchroniser, appDatabase)
    }

    @Test
    fun `GIVEN no saved areas WHEN performSync THEN overview data is synced`() =
        testDispatcher.runBlockingTest {

            coEvery { areaDataSynchroniser.performSync(any(), any()) } just Runs
            every { areaDao.allSavedAreas() } returns emptyList()

            sut.performSync()

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    Constants.UK_AREA_CODE,
                    AreaType.OVERVIEW
                )
            }
        }

    @Test
    fun `GIVEN no saved areas WHEN performSync THEN nation data is synced`() =
        testDispatcher.runBlockingTest {

            coEvery { areaDataSynchroniser.performSync(any(), any()) } just Runs
            every { areaDao.allSavedAreas() } returns emptyList()

            sut.performSync()

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    Constants.ENGLAND_AREA_CODE,
                    AreaType.NATION
                )
            }

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    Constants.NORTHERN_IRELAND_AREA_CODE,
                    AreaType.NATION
                )
            }

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    Constants.SCOTLAND_AREA_CODE,
                    AreaType.NATION
                )
            }

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    Constants.WALES_AREA_CODE,
                    AreaType.NATION
                )
            }
        }

    @Test
    fun `GIVEN saved areas WHEN performSync THEN area data is synced`() =
        testDispatcher.runBlockingTest {

            val area1 = AreaEntity(
                areaName = "Lambeth",
                areaCode = "1",
                areaType = AreaType.LTLA
            )
            val area2 = AreaEntity(
                areaName = "Southwark",
                areaCode = "2",
                areaType = AreaType.LTLA
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
    fun `GIVEN saved soa areas WHEN performSync THEN area data is synced`() =
        testDispatcher.runBlockingTest {

            val area = AreaEntity(
                areaName = "Lambeth",
                areaCode = "1",
                areaType = AreaType.MSOA
            )
            coEvery { areaDataSynchroniser.performSync(any(), any()) } just Runs
            coEvery { soaDataSynchroniser.performSync(any()) } just Runs
            every { areaDao.allSavedAreas() } returns listOf(area)

            sut.performSync()

            coVerify(exactly = 1) {
                soaDataSynchroniser.performSync(
                    area.areaCode
                )
            }
        }
}
