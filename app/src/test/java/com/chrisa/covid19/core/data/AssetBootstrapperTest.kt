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
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.time.TimeProvider
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.mockTransaction
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class AssetBootstrapperTest {

    private val assetDataSource = mockk<AssetDataSource>(relaxed = true)
    private val appDatabase = mockk<AppDatabase>()
    private val areaDao = mockk<AreaDao>(relaxed = true)
    private val areaDataDao = mockk<AreaDataDao>(relaxed = true)
    private val metadataDao = mockk<MetadataDao>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)
    private val timeProvider = mockk<TimeProvider>()
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

    private lateinit var sut: AssetBootstrapper

    @Before
    fun setup() {
        every { appDatabase.areaDao() } returns areaDao
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { timeProvider.currentTime() } returns syncTime

        every { areaDataDao.countAllByAreaType(any()) } returns 1
        every { areaDao.count() } returns 1

        sut = AssetBootstrapper(
            appDatabase,
            assetDataSource,
            TestCoroutineDispatchersImpl(testDispatcher),
            timeProvider
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

            val area = AreaModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview"
            )

            val areas = listOf(area)

            appDatabase.mockTransaction()

            coEvery { assetDataSource.getAreas() } returns areas
            every { areaDao.count() } returns 0

            sut.bootstrapData()

            verify {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.areaListId(),
                        lastUpdatedAt = AssetBootstrapper.BOOTSTRAP_DATA_TIMESTAMP,
                        lastSyncTime = syncTime
                    )
                )
            }
            verify {
                areaDao.insertAll(areas.map {
                    AreaEntity(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = AreaType.from(it.areaType)!!
                    )
                })
            }
        }

    @Test
    fun `GIVEN offline area data overview WHEN bootstrap data THEN area data overview is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { areaDataDao.countAllByAreaType(AreaType.OVERVIEW) } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                assetDataSource.getOverviewAreaData()
            }
        }

    @Test
    fun `GIVEN no offline area data overview WHEN bootstrap data THEN area data overview is updated`() =
        testCoroutineScope.runBlockingTest {
            val area = AreaDataModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview",
                cumulativeCases = 100,
                date = LocalDate.now(),
                infectionRate = 100.0,
                newCases = 10,
                newDeathsByPublishedDate = 15,
                cumulativeDeathsByPublishedDate = 20,
                cumulativeDeathsByPublishedDateRate = 30.0,
                newDeathsByDeathDate = 40,
                cumulativeDeathsByDeathDate = 50,
                cumulativeDeathsByDeathDateRate = 60.0,
                newAdmissions = 70,
                cumulativeAdmissions = 80,
                occupiedBeds = 90
            )
            val areas = listOf(area)

            appDatabase.mockTransaction()

            coEvery { assetDataSource.getOverviewAreaData() } returns areas
            every { areaDataDao.countAllByAreaType(AreaType.OVERVIEW) } returns 0

            sut.bootstrapData()

            verify {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                        lastUpdatedAt = AssetBootstrapper.BOOTSTRAP_DATA_TIMESTAMP,
                        lastSyncTime = syncTime
                    )
                )
            }
            verify {
                areaDataDao.insertAll(areas.map {
                    AreaDataEntity(
                        metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = AreaType.from(it.areaType)!!,
                        cumulativeCases = it.cumulativeCases!!,
                        date = it.date,
                        newCases = it.newCases!!,
                        infectionRate = it.infectionRate!!,
                        newDeathsByPublishedDate = it.newDeathsByPublishedDate!!,
                        cumulativeDeathsByPublishedDate = it.cumulativeDeathsByPublishedDate!!,
                        cumulativeDeathsByPublishedDateRate = it.cumulativeDeathsByPublishedDateRate!!,
                        newDeathsByDeathDate = it.newDeathsByDeathDate!!,
                        cumulativeDeathsByDeathDate = it.cumulativeDeathsByDeathDate!!,
                        cumulativeDeathsByDeathDateRate = it.cumulativeDeathsByDeathDateRate!!,
                        newAdmissions = it.newAdmissions!!,
                        cumulativeAdmissions = it.cumulativeAdmissions!!,
                        occupiedBeds = it.occupiedBeds!!
                    )
                })
            }
        }
}
