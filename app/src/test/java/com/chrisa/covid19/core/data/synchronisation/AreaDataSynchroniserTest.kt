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
import com.chrisa.covid19.core.data.db.AreaDataDao
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaDataModelStructureMapper
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.util.NetworkUtils
import com.chrisa.covid19.core.util.mockTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class AreaDataSynchroniserTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val areaDataModelStructureMapper = mockk<AreaDataModelStructureMapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: AreaDataSynchroniser
    private val areaCode = "1234"
    private val areaType = AreaType.OVERVIEW
    private val areaDataModel = "{}"

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { areaDataModelStructureMapper.mapAreaTypeToDataModel(any()) } returns areaDataModel
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { areaDataDao.deleteAllByAreaCode(areaCode) } just Runs
        every { areaDataDao.insertAll(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs

        appDatabase.mockTransaction()

        sut =
            AreaDataSynchroniser(networkUtils, appDatabase, areaDataModelStructureMapper, covidApi)
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {

            every { networkUtils.hasNetworkConnection() } returns false
            val onError: (error: Throwable) -> Unit = { throw it }

            sut.performSync(areaCode, areaType, onError)

            coVerify(exactly = 0) { covidApi.pagedAreaDataResponse(any(), any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN api fails WHEN performSync THEN HttpException is thrown`() =
        testDispatcher.runBlockingTest {
            val lastSyncedTime = LocalDateTime.of(2020, 3, 2, 0, 0)
            val lastUpdatedAt = lastSyncedTime.minusDays(1)
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.areaCodeId(areaCode),
                lastUpdatedAt = lastUpdatedAt,
                lastSyncTime = lastSyncedTime
            )
            val onError: (error: Throwable) -> Unit = { throw it }

            every { metadataDao.metadata(MetaDataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedAreaDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.error(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )

            sut.performSync(areaCode, areaType, onError)
        }

    @Test(expected = NullPointerException::class)
    fun `GIVEN api succeeds with null response WHEN performSync THEN area data is not updated`() =
        testDispatcher.runBlockingTest {
            val lastSyncedTime = LocalDateTime.of(2020, 3, 2, 0, 0)
            val lastUpdatedAt = lastSyncedTime.minusDays(1)
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.areaCodeId(areaCode),
                lastUpdatedAt = lastUpdatedAt,
                lastSyncTime = lastSyncedTime
            )
            val onError: (error: Throwable) -> Unit = { throw it }

            every { metadataDao.metadata(MetaDataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedAreaDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(null)

            sut.performSync(areaCode, areaType, onError)
        }

    @Test
    fun `GIVEN api succeeds with non-null response WHEN performSync THEN area data is updated`() =
        testDispatcher.runBlockingTest {
            val lastSyncedTime = LocalDateTime.of(2020, 3, 2, 0, 0)
            val lastUpdatedAt = lastSyncedTime.minusDays(1)
            val metadataEntity = MetadataEntity(
                id = MetaDataIds.areaCodeId(areaCode),
                lastUpdatedAt = lastUpdatedAt,
                lastSyncTime = lastSyncedTime
            )
            val areaModel = AreaDataModel(
                areaCode = "001",
                areaName = "UK",
                areaType = "overview",
                cumulativeCases = 100,
                date = LocalDate.now(),
                newCases = 10,
                infectionRate = 100.0
            )
            val pageModel = Page(
                length = 1,
                maxPageLimit = null,
                data = listOf(areaModel)
            )
            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
            val onError: (error: Throwable) -> Unit = { }

            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns syncTime

            every { metadataDao.metadata(MetaDataIds.areaCodeId(areaCode)) } returns metadataEntity
            coEvery {
                covidApi.pagedAreaDataResponse(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(pageModel)

            sut.performSync(areaCode, areaType, onError)

            verify(exactly = 1) { areaDataDao.deleteAllByAreaCode(areaCode) }
            verify(exactly = 1) {
                areaDataDao.insertAll(
                    listOf(
                        AreaDataEntity(
                            areaCode = areaModel.areaCode,
                            areaName = areaModel.areaName,
                            areaType = AreaType.from(areaModel.areaType)!!,
                            cumulativeCases = areaModel.cumulativeCases!!,
                            date = areaModel.date,
                            newCases = areaModel.newCases!!,
                            infectionRate = areaModel.infectionRate!!
                        )
                    )
                )
            }
            verify(exactly = 1) {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.areaCodeId(areaCode),
                        lastSyncTime = syncTime,
                        lastUpdatedAt = syncTime
                    )
                )
            }
        }
}
