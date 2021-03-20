/*
 * Copyright 2021 Chris Anderson.
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
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.core.data.db.SoaDataDao
import com.chrisa.cviz.core.data.db.SoaDataEntity
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.LatestChangeModel
import com.chrisa.cviz.core.data.network.RollingChangeModel
import com.chrisa.cviz.core.data.network.SoaDataModel
import com.chrisa.cviz.core.data.network.Utils
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
class SoaDataSynchroniserImplTest {

    private val appDatabase = mockk<AppDatabase>()
    private val soaDataDao = mockk<SoaDataDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val areaDao = mockk<AreaDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val timeProvider = mockk<TimeProvider>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: SoaDataSynchroniserImpl
    private val areaCode = "1234"
    private val areaType = AreaType.MSOA
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.soaDataDao() } returns soaDataDao
        every { appDatabase.areaDao() } returns areaDao
        every { soaDataDao.deleteAllByAreaCode(areaCode) } just Runs
        every { soaDataDao.insertAll(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs
        every { areaDao.insert(any()) } just Runs
        every { timeProvider.currentTime() } returns syncTime
        every { metadataDao.metadata(any()) } returns null

        appDatabase.mockTransaction()

        sut = SoaDataSynchroniserImpl(
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

            sut.performSync(areaCode)

            coVerify(exactly = 0) { covidApi.pagedAreaDataResponse(any(), any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN no area metadata WHEN performSync THEN api is called with no modified date`() =
        testDispatcher.runBlockingTest {
            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns null
            coEvery { covidApi.soaData(any(), any()) } returns
                Response.error(500, Utils.emptyJsonResponse())

            sut.performSync(areaCode)

            coVerify(exactly = 1) {
                covidApi.soaData(
                    null,
                    SoaDataModel.maosFilter(areaCode)
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

            sut.performSync(areaCode)

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
                covidApi.soaData(
                    any(),
                    any()
                )
            } returns Response.error(
                404,
                Utils.emptyJsonResponse()
            )

            sut.performSync(areaCode)
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
                covidApi.soaData(
                    any(),
                    any()
                )
            } returns Response.success(null)

            sut.performSync(areaCode)
        }

    @Test
    fun `GIVEN api succeeds with non-null response WHEN performSync THEN area data is updated`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetadataIds.areaCodeId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )
            val rollingChangeModel = RollingChangeModel(
                date = LocalDate.now(),
                rollingSum = 11,
                rollingRate = 1.0,
                change = 1,
                direction = "",
                changePercentage = 2.0
            )
            val emptyRollingChangeModel = RollingChangeModel(
                date = LocalDate.now(),
                rollingSum = null,
                rollingRate = null,
                change = null,
                direction = null,
                changePercentage = null
            )
            val soaData = SoaDataModel(
                areaCode = areaCode,
                areaName = "Westminister",
                areaType = areaType.value,
                latest = LatestChangeModel(
                    newCasesBySpecimenDate = rollingChangeModel
                ),
                newCasesBySpecimenDate = listOf(rollingChangeModel, emptyRollingChangeModel)
            )
            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
            val metadataId = MetadataIds.areaCodeId(areaCode)

            every { metadataDao.metadata(MetadataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.soaData(
                    any(),
                    any()
                )
            } returns Response.success(soaData)

            sut.performSync(areaCode)

            verify(exactly = 1) { soaDataDao.deleteAllByAreaCode(areaCode) }
            verify(exactly = 1) {
                soaDataDao.insertAll(
                    listOf(
                        SoaDataEntity(
                            areaCode = soaData.areaCode,
                            metadataId = metadataId,
                            rollingChangeModel.date,
                            rollingChangeModel.rollingSum!!,
                            rollingChangeModel.rollingRate!!,
                            rollingChangeModel.change!!,
                            rollingChangeModel.changePercentage!!
                        )
                    )
                )
            }
            verify(exactly = 1) {
                metadataDao.insert(
                    MetadataEntity(
                        id = metadataId,
                        lastSyncTime = syncTime,
                        lastUpdatedAt = syncTime
                    )
                )
            }
        }
}
