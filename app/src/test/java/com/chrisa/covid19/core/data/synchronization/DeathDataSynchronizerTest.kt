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
import com.chrisa.covid19.core.data.TestData
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.MetadataModel
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.NetworkUtils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.IOException
import org.junit.Test
import retrofit2.Response
import timber.log.Timber

@ExperimentalCoroutinesApi
class DeathDataSynchronizerTest {

    private val offlineDataSource = mockk<OfflineDataSource>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = DeathDataSynchronizer(networkUtils, offlineDataSource, covidApi)

    @Test
    fun `GIVEN no metadata WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            every { offlineDataSource.deathsMetadata() } returns null
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            coVerify(exactly = 0) { covidApi.getDeaths(any()) }
        }

    @Test
    fun `GIVEN metadata last updated more than an hour ago WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getDeaths(date) } returns Response.success(null)
            every { offlineDataSource.deathsMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            coVerify(exactly = 1) { covidApi.getDeaths(date) }
        }

    @Test
    fun `GIVEN metadata last updated less than an hour ago WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {

            val now = LocalDateTime.now()

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = now.minusMinutes(1)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getDeaths(date) } returns Response.success(null)
            every { offlineDataSource.deathsMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            coVerify(exactly = 0) { covidApi.getDeaths(date) }
        }

    @Test
    fun `GIVEN no internet connection and metadata WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getDeaths(date) } returns Response.success(null)
            every { offlineDataSource.deathsMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync()

            coVerify(exactly = 0) { covidApi.getDeaths(date) }
        }

    @Test
    fun `GIVEN api call throws WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            mockkStatic(Timber::class)

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            val error = IOException()

            coEvery { covidApi.getDeaths(date) } throws error

            every { offlineDataSource.deathsMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            verify(exactly = 0) { offlineDataSource.insertDeathMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertDeaths(any()) }
            verify(exactly = 1) { Timber.e(error, "Error synchronizing deaths") }
        }

    @Test
    fun `GIVEN api call fails WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getDeaths(date) } returns Response.error(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )

            every { offlineDataSource.deathsMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            verify(exactly = 0) { offlineDataSource.insertDeathMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertDeaths(any()) }
        }

    @Test
    fun `GIVEN api call succeeds with null body WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getDeaths(date) } returns Response.success(null)
            every { offlineDataSource.deathsMetadata() } returns metadata
            every { networkUtils.hasNetworkConnection() } returns true

            sut.performSync()

            verify(exactly = 0) { offlineDataSource.insertDeathMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertDeaths(any()) }
        }

    @Test
    fun `GIVEN api call succeeds WHEN performSync called THEN database is updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
            )

            val deathsModel = TestData.TEST_DEATH_MODEL

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getDeaths(date) } returns Response.success(deathsModel)
            every { networkUtils.hasNetworkConnection() } returns true

            every { offlineDataSource.deathsMetadata() } returns metadata
            every { offlineDataSource.insertDeathMetadata(any()) } just Runs
            every { offlineDataSource.insertDeaths(any()) } just Runs

            sut.performSync()

            verify(exactly = 1) { offlineDataSource.insertDeathMetadata(deathsModel.metadata) }

            val allDeaths =
                deathsModel.countries.union(deathsModel.overview)

            verify(exactly = 1) { offlineDataSource.insertDeaths(allDeaths) }
        }
}
