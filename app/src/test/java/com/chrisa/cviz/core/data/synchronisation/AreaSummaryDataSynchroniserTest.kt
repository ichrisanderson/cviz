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
import com.chrisa.cviz.core.data.db.AreaSummaryDao
import com.chrisa.cviz.core.data.db.AreaSummaryEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.core.data.network.AreaDataModel
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
    private val areaSummaryEntityDao = mockk<AreaSummaryDao>()
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
        every { appDatabase.areaSummaryDao() } returns areaSummaryEntityDao
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
                id = MetadataIds.areaSummaryId(),
                lastUpdatedAt = syncTime.minusDays(4),
                lastSyncTime = syncTime.minusDays(4)
            )
            val week1Data = AreaDataModel(
                areaCode = "LDN",
                areaName = "London",
                areaType = AreaType.REGION.value,
                date = lastDate,
                cumulativeCases = 100,
                newCases = 10,
                infectionRate = 100.0,
                newDeathsByPublishedDate = 15,
                cumulativeDeathsByPublishedDate = 20,
                cumulativeDeathsByPublishedDateRate = 30.0,
                newDeathsByDeathDate = 40,
                cumulativeDeathsByDeathDate = 50,
                cumulativeDeathsByDeathDateRate = 60.0,
                newOnsDeathsByRegistrationDate = 10,
                cumulativeOnsDeathsByRegistrationDate = 53,
                cumulativeOnsDeathsByRegistrationDateRate = 62.0
            )
            val monthlyData = MonthlyData(
                lastDate = lastDate,
                areaType = AreaType.LTLA,
                week1 = Page(length = 1, maxPageLimit = null, data = listOf(week1Data)),
                week2 = Page(length = 1, maxPageLimit = null, data = listOf(week1Data.copy(date = week1Data.date.minusWeeks(1)))),
                week3 = Page(length = 1, maxPageLimit = null, data = listOf(week1Data.copy(date = week1Data.date.minusWeeks(2)))),
                week4 = Page(length = 1, maxPageLimit = null, data = listOf(week1Data.copy(date = week1Data.date.minusWeeks(3))))
            )
            val areaSummaryEntity = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
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
            every { metadataDao.metadata(MetadataIds.areaSummaryId()) } returns metadataEntity
            coEvery { monthlyDataLoader.load(lastDate, AreaType.LTLA) } returns monthlyData
            every { areaEntityListBuilder.build(monthlyData) } returns areaSummaryEntityList

            sut.performSync()

            verifyOrder {
                areaSummaryEntityDao.deleteAll()
                areaDao.insertAll(
                    areaSummaryEntityList.map {
                        AreaEntity(
                            areaCode = week1Data.areaCode,
                            areaType = AreaType.from(week1Data.areaType)!!,
                            areaName = week1Data.areaName
                        )
                    }
                )
                metadataDao.insert(
                    MetadataEntity(
                        id = MetadataIds.areaSummaryId(),
                        lastUpdatedAt = lastDate.atStartOfDay(),
                        lastSyncTime = lastDate.atStartOfDay()
                    )
                )
                areaSummaryEntityDao.insertAll(areaSummaryEntityList)
            }
        }
}
