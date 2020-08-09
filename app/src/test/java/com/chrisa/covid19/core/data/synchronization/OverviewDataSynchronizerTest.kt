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

package com.chrisa.covid19.core.data.synchronization

import com.chrisa.covid19.core.data.OfflineDataSource
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.MetadataModel
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.NetworkUtils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response
import timber.log.Timber

@ExperimentalCoroutinesApi
class OverviewDataSynchronizerTest {

    private val offlineDataSource = mockk<OfflineDataSource>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = OverviewDataSynchronizer(networkUtils, offlineDataSource, covidApi)

    @Test
    fun `GIVEN no metadata WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            every { offlineDataSource.areaDataOverviewMetadata() } returns null
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            coVerify(exactly = 0) { covidApi.pagedOverviewAreaDataResponse(any()) }
        }

    @Test
    fun `GIVEN metadata last updated less than an hour ago WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            val now = LocalDateTime.now()

            val metadata = MetadataModel(
                lastUpdatedAt = now.minusMinutes(1)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } returns Response.success(null)

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            coVerify(exactly = 0) { covidApi.pagedOverviewAreaDataResponse(date) }
        }

    @Test
    fun `GIVEN metadata last updated more than an hour ago WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt.formatAsGmt()

            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } returns Response.success(null)

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            coVerify(exactly = 1) { covidApi.pagedOverviewAreaDataResponse(date) }
        }

    @Test
    fun `GIVEN no internet connection and metadata last updated more than an hour ago WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt.formatAsGmt()

            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } returns Response.success(null)

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync()

            coVerify(exactly = 0) { covidApi.pagedOverviewAreaDataResponse(date) }
        }

    @Test
    fun `GIVEN api call throws WHEN performSync called THEN error is logged`() =
        testDispatcher.runBlockingTest {

            mockkStatic(Timber::class)

            val metadata = MetadataModel(
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt.formatAsGmt()

            val error = IOException()

            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } throws error

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            verify(exactly = 0) { offlineDataSource.insertAreaDataOverviewMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertAreaData(any()) }
            verify(exactly = 1) { Timber.e(error, "Error synchronizing areas") }
        }

    @Test
    fun `GIVEN api call fails WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt
                .formatAsGmt()

            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } returns Response.error(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            verify(exactly = 0) { offlineDataSource.insertAreaDataOverviewMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertAreaData(any()) }
        }

    @Test
    fun `GIVEN api call succeeds with null body WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt
                .formatAsGmt()

            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } returns Response.success(null)

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            verify(exactly = 0) { offlineDataSource.insertAreaDataOverviewMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertAreaData(any()) }
        }

    @Test
    fun `GIVEN api call succeeds WHEN performSync called THEN database is updated`() =
        testDispatcher.runBlockingTest {

            mockkStatic(LocalDateTime::class)

            val metadata = MetadataModel(
                lastUpdatedAt = LocalDateTime.of(2020, 2, 2, 0, 0)
            )

            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

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

            val date = metadata.lastUpdatedAt
                .formatAsGmt()

            every { LocalDateTime.now() } returns syncTime
            coEvery { covidApi.pagedOverviewAreaDataResponse(date) } returns Response.success(pageModel)
            every { networkUtils.hasNetworkConnection() } returns true

            val transactionLambda = slot<suspend () -> Unit>()
            coEvery { offlineDataSource.withTransaction(capture(transactionLambda)) } coAnswers {
                transactionLambda.captured.invoke()
            }

            every { offlineDataSource.areaDataOverviewMetadata() } returns metadata
            every { offlineDataSource.insertAreaDataOverviewMetadata(any()) } just Runs
            every { offlineDataSource.insertAreaData(any()) } just Runs

            sut.performSync()

            verify(exactly = 1) { offlineDataSource.insertAreaDataOverviewMetadata(MetadataModel(lastUpdatedAt = syncTime)) }
            verify(exactly = 1) { offlineDataSource.insertAreaData(pageModel.data) }
        }
}
