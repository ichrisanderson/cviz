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
import com.chrisa.cviz.core.data.db.AreaSummaryEntity
import com.chrisa.cviz.core.data.db.AreaSummaryEntityDao
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.network.AreaDataModelStructureMapper
import com.chrisa.cviz.core.data.network.Page
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.core.util.NetworkUtils
import com.chrisa.cviz.core.util.mockTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyOrder
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AreaSummaryDataSynchroniserTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaSummaryEntityDao = mockk<AreaSummaryEntityDao>()
    private val areaDao = mockk<AreaDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val networkUtils = mockk<NetworkUtils>()
    private val monthlyDataLoader = mockk<MonthlyDataLoader>()
    private val areaEntityListBuilder = mockk<AreaEntityListBuilder>()
    private val areaDataModelStructureMapper = mockk<AreaDataModelStructureMapper>()
    private val timeProvider = mockk<TimeProvider>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val areaDataModel = "{}"
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private val syncDate = syncTime.toLocalDate()
    private val lastDate = syncDate.minusDays(3)

    private lateinit var sut: AreaSummaryDataSynchroniser

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.areaDao() } returns areaDao
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaSummaryEntityDao() } returns areaSummaryEntityDao
        every { timeProvider.currentDate() } returns syncDate
        every { areaSummaryEntityDao.deleteAll() } just Runs
        every { areaSummaryEntityDao.insertAll(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs
        every { areaDao.insertAll(any()) } just Runs
        every { areaDataModelStructureMapper.mapAreaTypeToDataModel(any()) } returns areaDataModel

        appDatabase.mockTransaction()

        sut =
            AreaSummaryDataSynchroniser(
                appDatabase,
                monthlyDataLoader,
                areaEntityListBuilder,
                networkUtils,
                timeProvider
            )
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {

            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync()

            coVerify(exactly = 0) { monthlyDataLoader.load(any(), any()) }
        }

    @Test
    fun `GIVEN data can be loaded from the api WHEN performSync THEN data is cached`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.areaSummaryId(),
                lastUpdatedAt = syncTime.minusDays(4),
                lastSyncTime = syncTime.minusDays(4)
            )
            val monthlyData = MonthlyData(
                lastDate = lastDate,
                areaType = AreaType.LTLA,
                week1 = Page(length = 1, maxPageLimit = null, data = listOf()),
                week2 = Page(length = 1, maxPageLimit = null, data = listOf()),
                week3 = Page(length = 1, maxPageLimit = null, data = listOf()),
                week4 = Page(length = 1, maxPageLimit = null, data = listOf())
            )
            val areaSummaryEntity = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                areaName = "UK",
                date = syncTime.toLocalDate(),
                baseInfectionRate = 100.0,
                cumulativeCasesWeek1 = 100,
                cumulativeCaseInfectionRateWeek1 = 85.0,
                newCaseInfectionRateWeek1 = 25.0,
                newCasesWeek1 = 30,
                cumulativeCasesWeek2 = 80,
                cumulativeCaseInfectionRateWeek2 = 80.0,
                newCaseInfectionRateWeek2 = 22.0,
                newCasesWeek2 = 22,
                cumulativeCasesWeek3 = 66,
                cumulativeCaseInfectionRateWeek3 = 82.0,
                newCaseInfectionRateWeek3 = 33.0,
                newCasesWeek3 = 26,
                cumulativeCasesWeek4 = 50,
                cumulativeCaseInfectionRateWeek4 = 75.0
            )
            val areaSummaryEntityList = listOf(areaSummaryEntity)
            every { metadataDao.metadata(MetaDataIds.areaSummaryId()) } returns metadataEntity
            coEvery { monthlyDataLoader.load(lastDate, AreaType.LTLA) } returns monthlyData
            every { areaEntityListBuilder.build(monthlyData) } returns areaSummaryEntityList

            sut.performSync()

            verifyOrder {
                areaSummaryEntityDao.deleteAll()
                areaSummaryEntityDao.insertAll(areaSummaryEntityList)
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.areaSummaryId(),
                        lastUpdatedAt = lastDate.atStartOfDay(),
                        lastSyncTime = lastDate.atStartOfDay()
                    )
                )
                areaDao.insertAll(
                    areaSummaryEntityList.map {
                        AreaEntity(
                            areaCode = it.areaCode,
                            areaType = it.areaType,
                            areaName = it.areaName
                        )
                    }
                )
            }
        }
}
