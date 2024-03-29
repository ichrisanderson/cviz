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
import com.chrisa.cviz.core.data.db.AreaDataDao
import com.chrisa.cviz.core.data.db.AreaDataEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.core.data.network.AREA_DATA_FILTER
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.AreaDataModelStructureMapper
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.Page
import com.chrisa.cviz.core.data.network.Utils.emptyJsonResponse
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.core.util.NetworkUtils
import com.chrisa.cviz.core.util.mockTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class AreaDataSynchroniserImplTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val areaDao = mockk<AreaDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val areaDataModelStructureMapper = mockk<AreaDataModelStructureMapper>()
    private val timeProvider = mockk<TimeProvider>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: AreaDataSynchroniser
    private val areaCode = "1234"
    private val areaType = AreaType.OVERVIEW
    private val areaDataModel = "{}"
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { areaDataModelStructureMapper.mapAreaTypeToDataModel(any()) } returns areaDataModel
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { appDatabase.areaDao() } returns areaDao
        every { areaDataDao.deleteAllByAreaCode(areaCode) } just Runs
        every { areaDataDao.insertAll(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs
        every { areaDao.insertAll(any()) } just Runs
        every { timeProvider.currentTime() } returns syncTime
        every { metadataDao.metadata(any()) } returns null

        appDatabase.mockTransaction()

        sut = AreaDataSynchroniserImpl(
            covidApi,
            appDatabase,
            areaDataModelStructureMapper,
            networkUtils,
            timeProvider
        )
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {

            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 0) { covidApi.pagedAreaDataResponse(any(), any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN no area metadata WHEN performSync THEN api is called with no modified date`() =
        testDispatcher.runBlockingTest {
            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns null
            coEvery { covidApi.pagedAreaDataResponse(any(), any(), any()) } returns
                Response.error(500, emptyJsonResponse())

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 1) {
                covidApi.pagedAreaDataResponse(
                    null,
                    AREA_DATA_FILTER(areaCode, areaType.value),
                    areaDataModel
                )
            }
        }

    @Test
    fun `GIVEN recent area metadata WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetadataIds.areaCodeId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusSeconds(1)
            )
            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns metadataEntity

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 0) {
                covidApi.pagedAreaDataResponse(any(), any(), any())
            }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN api fails WHEN performSync THEN HttpException is thrown`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetadataIds.areaCodeId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusSeconds(301)
            )

            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedAreaDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.error(
                404,
                emptyJsonResponse()
            )

            sut.performSync(areaCode, areaType)
        }

    @Test(expected = NullPointerException::class)
    fun `GIVEN api succeeds with null response WHEN performSync THEN area data is not updated`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetadataIds.areaCodeId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )

            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedAreaDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(null)

            sut.performSync(areaCode, areaType)
        }

    @Test
    fun `GIVEN api succeeds with non-null response WHEN performSync THEN area data is updated`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetadataIds.areaCodeId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )
            val areaModel = AreaDataModel(
                areaCode = Constants.UK_AREA_CODE,
                areaName = Constants.UK_AREA_NAME,
                areaType = AreaType.OVERVIEW.value,
                cumulativeCases = 100,
                date = LocalDate.now(),
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
            val pageModel = Page(
                length = 1,
                maxPageLimit = null,
                data = listOf(areaModel)
            )
            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedAreaDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(pageModel)

            sut.performSync(areaCode, areaType)

            verify(exactly = 1) { areaDataDao.deleteAllByAreaCode(areaCode) }
            verify(exactly = 1) {
                areaDataDao.insertAll(
                    listOf(
                        AreaDataEntity(
                            metadataId = metadataEntity.id,
                            areaCode = areaModel.areaCode,
                            cumulativeCases = areaModel.cumulativeCases!!,
                            date = areaModel.date,
                            newCases = areaModel.newCases!!,
                            infectionRate = areaModel.infectionRate!!,
                            newDeathsByPublishedDate = areaModel.newDeathsByPublishedDate!!,
                            cumulativeDeathsByPublishedDate = areaModel.cumulativeDeathsByPublishedDate!!,
                            cumulativeDeathsByPublishedDateRate = areaModel.cumulativeDeathsByPublishedDateRate!!,
                            newDeathsByDeathDate = areaModel.newDeathsByDeathDate!!,
                            cumulativeDeathsByDeathDate = areaModel.cumulativeDeathsByDeathDate!!,
                            cumulativeDeathsByDeathDateRate = areaModel.cumulativeDeathsByDeathDateRate!!,
                            newOnsDeathsByRegistrationDate = areaModel.newOnsDeathsByRegistrationDate,
                            cumulativeOnsDeathsByRegistrationDate = areaModel.cumulativeOnsDeathsByRegistrationDate,
                            cumulativeOnsDeathsByRegistrationDateRate = areaModel.cumulativeOnsDeathsByRegistrationDateRate
                        )
                    )
                )
            }
            verify(exactly = 1) {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetadataIds.areaCodeId(areaCode),
                        lastSyncTime = syncTime,
                        lastUpdatedAt = syncTime
                    )
                )
            }
        }
}
