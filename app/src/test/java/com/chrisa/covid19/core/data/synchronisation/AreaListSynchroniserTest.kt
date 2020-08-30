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
import com.chrisa.covid19.core.data.db.AreaDao
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AREA_FILTER
import com.chrisa.covid19.core.data.network.AREA_MODEL_STRUCTURE
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
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
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import timber.log.Timber

@ExperimentalCoroutinesApi
class AreaListSynchroniserTest {

    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val appDatabase = mockk<AppDatabase>()
    private val areaDao = mockk<AreaDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: AreaListSynchroniser

    @Before
    fun setup() {
        every { appDatabase.areaDao() } returns areaDao
        every { appDatabase.metadataDao() } returns metadataDao

        sut = AreaListSynchroniser(networkUtils, appDatabase, covidApi)
    }

    @Test
    fun `GIVEN no metadata WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns null
            every { networkUtils.hasNetworkConnection() } returns true
            val onError: (error: Throwable) -> Unit = { }

            sut.performSync(onError)

            coVerify(exactly = 0) { covidApi.pagedAreaResponse(any(), any(), any()) }
        }

    @Test
    fun `GIVEN metadata last updated less than an hour ago WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            val now = LocalDateTime.now()
            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusMinutes(1),
                lastSyncTime = now.minusDays(1)
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.success(null)

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync(onError)

            coVerify(exactly = 0) {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            }
        }

    @Test
    fun `GIVEN metadata last synced less than an hour ago WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            val now = LocalDateTime.now()

            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusDays(1),
                lastSyncTime = now.minusMinutes(1)
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.success(null)

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync(onError)

            coVerify(exactly = 0) {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            }
        }

    @Test
    fun `GIVEN metadata last updated more than an hour ago WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {
            val now = LocalDateTime.now()

            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusMinutes(61),
                lastSyncTime = now.minusHours(1)
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt.formatAsGmt()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.success(null)

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync(onError)

            coVerify(exactly = 1) {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            }
        }

    @Test
    fun `GIVEN no internet connection and metadata last updated more than an hour ago WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {
            val now = LocalDateTime.now()

            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusMinutes(1),
                lastSyncTime = now
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt.formatAsGmt()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.success(null)

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync(onError)

            coVerify(exactly = 0) {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            }
        }

    @Test
    fun `GIVEN api call throws WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            mockkStatic(Timber::class)

            val now = LocalDateTime.now()
            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusDays(1),
                lastSyncTime = now.minusHours(1)
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt.formatAsGmt()

            val error = IOException()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } throws error

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync(onError)

            verify(exactly = 0) { metadataDao.insert(any()) }
            verify(exactly = 0) { areaDao.insertAll(any()) }
        }

    @Test
    fun `GIVEN api call fails WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val now = LocalDateTime.now()
            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusMinutes(1),
                lastSyncTime = now
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt
                .formatAsGmt()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.error(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync(onError)

            verify(exactly = 0) { metadataDao.insert(any()) }
            verify(exactly = 0) { areaDao.insertAll(any()) }
        }

    @Test
    fun `GIVEN api call succeeds with null body WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val now = LocalDateTime.now()
            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = now.minusMinutes(1),
                lastSyncTime = now
            )
            val onError: (error: Throwable) -> Unit = { }

            val date = metadata.lastUpdatedAt
                .formatAsGmt()

            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.success(null)

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync(onError)

            verify(exactly = 0) { metadataDao.insert(any()) }
            verify(exactly = 0) { areaDao.insertAll(any()) }
        }

    @Test
    fun `GIVEN api call succeeds WHEN performSync called THEN database is updated`() =
        testDispatcher.runBlockingTest {

            mockkStatic(LocalDateTime::class)
            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

            val metadata = MetadataEntity(
                id = MetaDataIds.areaListId(),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime.minusHours(1)
            )
            val onError: (error: Throwable) -> Unit = { }

            val areaModel = AreaModel(
                areaCode = "1234",
                areaName = "United Kingdom",
                areaType = "overview"
            )

            val pageModel = Page(
                length = 1,
                maxPageLimit = null,
                data = listOf(areaModel)
            )

            val date = metadata.lastUpdatedAt
                .formatAsGmt()

            every { LocalDateTime.now() } returns syncTime
            coEvery {
                covidApi.pagedAreaResponse(
                    date,
                    AREA_FILTER,
                    AREA_MODEL_STRUCTURE
                )
            } returns Response.success(pageModel)
            every { networkUtils.hasNetworkConnection() } returns true

            appDatabase.mockTransaction()

            every { metadataDao.metadata(MetaDataIds.areaListId()) } returns metadata
            every { metadataDao.insert(any()) } just Runs
            every { areaDao.insertAll(any()) } just Runs

            sut.performSync(onError)

            verify(exactly = 1) {
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.areaListId(),
                        lastUpdatedAt = syncTime,
                        lastSyncTime = syncTime
                    )
                )
            }
            verify(exactly = 1) {
                areaDao.insertAll(pageModel.data.map {
                    AreaEntity(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = AreaType.from(it.areaType)!!
                    )
                })
            }
        }
}
