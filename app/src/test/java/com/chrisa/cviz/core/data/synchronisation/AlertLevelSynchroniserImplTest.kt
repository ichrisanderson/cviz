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

import com.chrisa.cviz.core.data.db.AlertLevelDao
import com.chrisa.cviz.core.data.db.AlertLevelEntity
import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDao
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.network.AlertLevel
import com.chrisa.cviz.core.data.network.BodyPage
import com.chrisa.cviz.core.data.network.CovidApi
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
import java.time.LocalDateTime
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AlertLevelSynchroniserImplTest {

    private val covidApi: CovidApi = mockk()
    private val appDatabase: AppDatabase = mockk()
    private val alertLevelDao = mockk<AlertLevelDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val areaDao = mockk<AreaDao>()
    private val networkUtils: NetworkUtils = mockk()
    private val timeProvider: TimeProvider = mockk()
    private val areaCode = "1234"
    private val areaType = AreaType.LTLA
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private val testDispatcher = TestCoroutineDispatcher()
    private val filter = mapOf(
        "areaType" to areaType.value,
        "areaCode" to areaCode,
        "metric" to "alertLevel",
        "format" to "json"
    )

    private val sut = AlertLevelSynchroniserImpl(
        covidApi,
        appDatabase,
        networkUtils,
        timeProvider
    )

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.alertLevelDao() } returns alertLevelDao
        every { appDatabase.areaDao() } returns areaDao
        every { alertLevelDao.insert(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs
        every { areaDao.insert(any()) } just Runs
        every { timeProvider.currentTime() } returns syncTime
        every { metadataDao.metadata(any()) } returns null

        appDatabase.mockTransaction()
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {

            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 0) { covidApi.alertLevel(any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN no area metadata WHEN performSync THEN api is called with no modified date`() =
        testDispatcher.runBlockingTest {
            every { metadataDao.metadata(MetaDataIds.alertLevelId(areaCode)) } returns null
            coEvery { covidApi.alertLevel(any(), any()) } returns
                Response.error(500, Utils.emptyJsonResponse())

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 1) {
                covidApi.alertLevel(
                    null,
                    filter
                )
            }
        }

    @Test
    fun `GIVEN recent area metadata WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.alertLevelId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusSeconds(1)
            )
            every { metadataDao.metadata(MetaDataIds.alertLevelId(areaCode)) } returns metadataEntity

            sut.performSync(areaCode, areaType)

            coVerify(exactly = 0) {
                covidApi.alertLevel(any(), any())
            }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN api fails WHEN performSync THEN HttpException is thrown`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.alertLevelId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusSeconds(301)
            )

            every { metadataDao.metadata(MetaDataIds.alertLevelId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.alertLevel(
                    any(),
                    any()
                )
            } returns Response.error(404, Utils.emptyJsonResponse())

            sut.performSync(areaCode, areaType)
        }

    @Test(expected = NullPointerException::class)
    fun `GIVEN api succeeds with null response WHEN performSync THEN area data is not updated`() =
        testDispatcher.runBlockingTest {
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.alertLevelId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )

            every { metadataDao.metadata(MetaDataIds.alertLevelId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.alertLevel(
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
                id = MetaDataIds.alertLevelId(areaCode),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusMinutes(6)
            )
            val alertLevelData = AlertLevel(
                areaCode = areaCode,
                areaName = "London",
                areaType = areaType.value,
                date = syncTime.toLocalDate(),
                alertLevelValue = 5,
                alertLevelName = "National restrctions",
                alertLevelUrl = "https://www.acme.com",
                alertLevel = 5
            )
            val pageModel = BodyPage(
                length = 1,
                body = listOf(alertLevelData)
            )

            every { metadataDao.metadata(MetaDataIds.alertLevelId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.alertLevel(
                    any(),
                    any()
                )
            } returns Response.success(pageModel)

            sut.performSync(areaCode, areaType)

            verify(exactly = 1) {
                alertLevelDao.insert(
                    AlertLevelEntity(
                        areaCode = alertLevelData.areaCode,
                        date = alertLevelData.date,
                        alertLevel = alertLevelData.alertLevel,
                        alertLevelUrl = alertLevelData.alertLevelUrl,
                        alertLevelName = alertLevelData.alertLevelName,
                        alertLevelValue = alertLevelData.alertLevelValue
                    )
                )
            }
            verify(exactly = 1) {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.alertLevelId(areaCode),
                        lastSyncTime = syncTime,
                        lastUpdatedAt = syncTime
                    )
                )
            }
        }
}
