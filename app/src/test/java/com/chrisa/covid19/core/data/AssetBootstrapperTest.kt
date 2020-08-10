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

package com.chrisa.covid19.core.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDao
import com.chrisa.covid19.core.data.db.AreaDataDao
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.mockTransaction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AssetBootstrapperTest {

    private val assetDataSource = mockk<AssetDataSource>(relaxed = true)
    private val appDatabase = mockk<AppDatabase>()
    private val areaDao = mockk<AreaDao>(relaxed = true)
    private val areaDataDao = mockk<AreaDataDao>(relaxed = true)
    private val metadataDao = mockk<MetadataDao>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)

    private lateinit var sut: AssetBootstrapper

    @Before
    fun setup() {
        every { appDatabase.areaDao() } returns areaDao
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaDataDao() } returns areaDataDao

        every { areaDataDao.countAllByType(any()) } returns 1
        every { areaDao.count() } returns 1

        sut = AssetBootstrapper(
            assetDataSource,
            appDatabase,
            TestCoroutineDispatchersImpl(testDispatcher)
        )
    }

    @Test
    fun `GIVEN offline areas WHEN bootstrap data THEN area data is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { areaDao.count() } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                assetDataSource.getAreas()
            }
        }

    @Test
    fun `GIVEN no offline areas WHEN bootstrap data THEN area data is updated`() =
        testCoroutineScope.runBlockingTest {
            mockkStatic(LocalDateTime::class)

            val area = AreaModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview"
            )

            val date = LocalDateTime.of(2020, 1, 1, 0, 0)
            val areas = listOf(area)

            appDatabase.mockTransaction()

            coEvery { assetDataSource.getAreas() } returns areas
            every { areaDao.count() } returns 0
            every { LocalDateTime.now() } returns date

            sut.bootstrapData()

            verify {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetadataEntity.AREA_METADATA_ID,
                        lastUpdatedAt = date.minusDays(1)
                    )
                )
            }
            verify {
                areaDao.insertAll(areas.map {
                    AreaEntity(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType
                    )
                })
            }
        }

    @Test
    fun `GIVEN offline area data overview WHEN bootstrap data THEN area data overview is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { areaDataDao.countAllByType("overview") } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                assetDataSource.getOverviewAreaData()
            }
        }

    @Test
    fun `GIVEN no offline area data overview WHEN bootstrap data THEN area data overview is updated`() =
        testCoroutineScope.runBlockingTest {
            mockkStatic(LocalDateTime::class)

            val area = AreaDataModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview",
                cumulativeCases = 100,
                date = LocalDate.now(),
                infectionRate = 100.0,
                newCases = 10
            )

            val date = LocalDateTime.of(2020, 1, 1, 0, 0)
            val areas = listOf(area)

            appDatabase.mockTransaction()

            coEvery { assetDataSource.getOverviewAreaData() } returns areas
            every { areaDataDao.countAllByType("overview") } returns 0
            every { LocalDateTime.now() } returns date

            sut.bootstrapData()

            verify {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetadataEntity.AREA_DATA_OVERVIEW_METADATA_ID,
                        lastUpdatedAt = date.minusDays(1)
                    )
                )
            }
            verify {
                areaDataDao.insertAll(areas.map {
                    AreaDataEntity(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType,
                        cumulativeCases = it.cumulativeCases!!,
                        date = it.date,
                        newCases = it.newCases!!,
                        infectionRate = it.infectionRate!!
                    )
                })
            }
        }
}
