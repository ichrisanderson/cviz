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
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.HealthcareDao
import com.chrisa.cviz.core.data.db.HealthcareEntity
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.network.AREA_DATA_FILTER
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.HealthcareData
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
class HealthcareDataSynchroniserImplTest {

    private val appDatabase = mockk<AppDatabase>()
    private val healthcareDao = mockk<HealthcareDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val timeProvider = mockk<TimeProvider>()
    private val testDispatcher = TestCoroutineDispatcher()

    private val areaCode = "1234"
    private val areaType = AreaType.OVERVIEW
    private val areaDataModel = "{}"
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private lateinit var sut: HealthcareDataSynchroniserImpl

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.healthcareDao() } returns healthcareDao
        every { healthcareDao.deleteAllByAreaCode(areaCode) } just Runs
        every { healthcareDao.insertAll(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs
        every { timeProvider.currentTime() } returns syncTime

        appDatabase.mockTransaction()

        sut = HealthcareDataSynchroniserImpl(
            covidApi,
            appDatabase,
            networkUtils,
            timeProvider
        )
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {

            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 0) { covidApi.pagedHealthcareDataResponse(any(), any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN no area metadata WHEN performSync THEN api is called with no modified date`() =
        testDispatcher.runBlockingTest {
            every { metadataDao.metadata(MetaDataIds.healthcareId(areaCode)) } returns null
            coEvery { covidApi.pagedHealthcareDataResponse(any(), any(), any()) } returns
                Response.error(500, emptyJsonResponse())

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 1) {
                covidApi.pagedHealthcareDataResponse(
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
                id = MetaDataIds.healthcareId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusSeconds(1)
            )
            every { metadataDao.metadata(MetaDataIds.healthcareId(areaCode)) } returns metadataEntity

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 0) {
                covidApi.pagedHealthcareDataResponse(any(), any(), any())
            }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN api fails WHEN performSync THEN HttpException is thrown`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.healthcareId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusSeconds(301)
            )

            every { metadataDao.metadata(MetaDataIds.healthcareId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedHealthcareDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.error(404, emptyJsonResponse())

            sut.performSync(areaCode, areaType)
        }

    @Test(expected = NullPointerException::class)
    fun `GIVEN api succeeds with null response WHEN performSync THEN area data is not updated`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.healthcareId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )

            every { metadataDao.metadata(MetaDataIds.healthcareId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedHealthcareDataResponse(
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
                id = MetaDataIds.healthcareId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )
            val healthcareData = HealthcareData(
                areaCode = "001",
                areaName = "UK",
                areaType = "overview",
                date = LocalDate.now(),
                newAdmissions = 10,
                cumulativeAdmissions = 60,
                occupiedBeds = 30,
                transmissionRateGrowthRateMax = 10.0,
                transmissionRateGrowthRateMin = 5.0,
                transmissionRateMax = 1.0,
                transmissionRateMin = 1.0
            )
            val pageModel = Page(
                length = 1,
                maxPageLimit = null,
                data = listOf(healthcareData)
            )
            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

            every { metadataDao.metadata(MetaDataIds.healthcareId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedHealthcareDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(pageModel)

            sut.performSync(areaCode, areaType)

            verify(exactly = 1) { healthcareDao.deleteAllByAreaCode(areaCode) }
            verify(exactly = 1) {
                healthcareDao.insertAll(
                    listOf(
                        HealthcareEntity(
                            areaCode = healthcareData.areaCode,
                            areaName = healthcareData.areaName,
                            areaType = AreaType.from(healthcareData.areaType)!!,
                            date = healthcareData.date,
                            newAdmissions = healthcareData.newAdmissions,
                            cumulativeAdmissions = healthcareData.cumulativeAdmissions,
                            occupiedBeds = healthcareData.occupiedBeds
                        )
                    )
                )
            }
            verify(exactly = 1) {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.healthcareId(areaCode),
                        lastSyncTime = syncTime,
                        lastUpdatedAt = syncTime
                    )
                )
            }
        }
}
