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
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

class CaseDataSynchronizerTest {

    private val offlineDataSource = mockk<OfflineDataSource>()
    private val covidApi = mockk<CovidApi>()
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = CaseDataSynchronizer(offlineDataSource, covidApi)

    @Test
    fun `GIVEN no metadata WHEN performSync called THEN api is not hit`() =
        testDispatcher.runBlockingTest {

            every { offlineDataSource.casesMetadata() } returns null

            sut.performSync()

            coVerify(exactly = 0) { covidApi.getCases(any()) }
        }

    @Test
    fun `GIVEN metadata WHEN performSync called THEN api is hit`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getCases(date) } returns Response.success(null)
            every { offlineDataSource.casesMetadata() } returns metadata

            sut.performSync()

            coVerify(exactly = 1) { covidApi.getCases(date) }
        }

    @Test
    fun `GIVEN api call fails WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getCases(date) } returns Response.error(
                404,
                ResponseBody.create(MediaType.get("application/json"), "")
            )

            every { offlineDataSource.casesMetadata() } returns metadata

            sut.performSync()

            coVerify(exactly = 0) { offlineDataSource.insertCaseMetadata(any()) }
            coVerify(exactly = 0) { offlineDataSource.insertDailyRecord(any(), any()) }
            coVerify(exactly = 0) { offlineDataSource.insertCases(any()) }
        }

    @Test
    fun `GIVEN api call succeeds with null body WHEN performSync called THEN database is not updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getCases(date) } returns Response.success(null)
            every { offlineDataSource.casesMetadata() } returns metadata

            sut.performSync()

            coVerify(exactly = 0) { offlineDataSource.insertCaseMetadata(any()) }
            coVerify(exactly = 0) { offlineDataSource.insertDailyRecord(any(), any()) }
            coVerify(exactly = 0) { offlineDataSource.insertCases(any()) }
        }

    @Test
    fun `GIVEN api call succeeds WHEN performSync called THEN database is updated`() =
        testDispatcher.runBlockingTest {

            val metadata = MetadataModel(
                disclaimer = "Test disclaimer",
                lastUpdatedAt = LocalDateTime.ofEpochSecond(1, 1, ZoneOffset.ofHours(0))
            )

            val caseModel = TestData.TEST_CASE_MODEL

            val date = metadata.lastUpdatedAt
                .plusHours(1)
                .formatAsGmt()

            coEvery { covidApi.getCases(date) } returns Response.success(caseModel)

            every { offlineDataSource.casesMetadata() } returns metadata
            every { offlineDataSource.insertCaseMetadata(any()) } just Runs
            every { offlineDataSource.insertDailyRecord(any(), any()) } just Runs
            every { offlineDataSource.insertCases(any()) } just Runs

            sut.performSync()

            coVerify(exactly = 1) { offlineDataSource.insertCaseMetadata(caseModel.metadata) }
            coVerify(exactly = 1) {
                offlineDataSource.insertDailyRecord(
                    caseModel.dailyRecords,
                    caseModel.metadata.lastUpdatedAt.toLocalDate()
                )
            }

            val allCases =
                caseModel.countries.union(caseModel.ltlas).union(caseModel.utlas)
                    .union(caseModel.regions)

            coVerify(exactly = 1) { offlineDataSource.insertCases(allCases) }
        }
}
