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
import com.chrisa.covid19.core.util.DateUtils.addHours
import com.chrisa.covid19.core.util.DateUtils.toGmtDate
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

class DeathDataSynchronizerTest {

    private val offlineDataSource = mockk<OfflineDataSource>()
    private val covidApi = mockk<CovidApi>()
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = DeathDataSynchronizer(offlineDataSource, covidApi)

    @Test
    fun `GIVEN no metadata WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            every { offlineDataSource.deathsMetadata() } returns null

            sut.performSync()

            coVerify(exactly = 0) { covidApi.getDeaths(any()) }
        }

    @Test
    fun `GIVEN metadata WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = Date(1)
            )

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

            coEvery { covidApi.getDeaths(date) } returns Response.success(null)
            every { offlineDataSource.deathsMetadata() } returns metadata

            sut.performSync()

            coVerify(exactly = 1) { covidApi.getDeaths(date) }
        }

    @Test
    fun `GIVEN api call fails WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = Date(1)
            )

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

            coEvery { covidApi.getDeaths(date) } returns Response.error(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )

            every { offlineDataSource.deathsMetadata() } returns metadata

            sut.performSync()

            coVerify(exactly = 0) { offlineDataSource.insertDeathMetadata(any()) }
            coVerify(exactly = 0) { offlineDataSource.insertDeaths(any()) }
        }

    @Test
    fun `GIVEN api call succeeds with null body WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = Date(1)
            )

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

            coEvery { covidApi.getDeaths(date) } returns Response.success(null)
            every { offlineDataSource.deathsMetadata() } returns metadata

            sut.performSync()

            coVerify(exactly = 0) { offlineDataSource.insertDeathMetadata(any()) }
            coVerify(exactly = 0) { offlineDataSource.insertDeaths(any()) }
        }

    @Test
    fun `GIVEN api call succeeds WHEN performSync called THEN database is updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = Date(1)
            )

            val deathsModel = TestData.TEST_DEATH_MODEL

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

            coEvery { covidApi.getDeaths(date) } returns Response.success(deathsModel)

            every { offlineDataSource.deathsMetadata() } returns metadata
            every { offlineDataSource.insertDeathMetadata(any()) } just Runs
            every { offlineDataSource.insertDeaths(any()) } just Runs

            sut.performSync()

            coVerify(exactly = 1) { offlineDataSource.insertDeathMetadata(deathsModel.metadata) }

            val allDeaths =
                deathsModel.countries.union(deathsModel.overview)

            coVerify(exactly = 1) { offlineDataSource.insertDeaths(allDeaths) }
        }
}
