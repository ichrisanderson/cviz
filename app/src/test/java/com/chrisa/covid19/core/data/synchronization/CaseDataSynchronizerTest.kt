package com.chrisa.covid19.core.data.synchronization

import com.chrisa.covid19.core.data.OfflineDataSource
import com.chrisa.covid19.core.data.TestData
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.MetadataModel
import com.chrisa.covid19.core.util.DateUtils.addHours
import com.chrisa.covid19.core.util.DateUtils.toGmtDate
import io.mockk.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response
import java.util.*

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
                lastUpdatedAt = Date(1)
            )

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

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
                lastUpdatedAt = Date(1)
            )

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

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
                lastUpdatedAt = Date(1)
            )

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

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
                lastUpdatedAt = Date(1)
            )

            val caseModel = TestData.TEST_CASE_MODEL

            val date = metadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()

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
                    caseModel.metadata.lastUpdatedAt
                )
            }

            val allCases =
                caseModel.countries.union(caseModel.ltlas).union(caseModel.utlas)
                    .union(caseModel.regions)

            coVerify(exactly = 1) { offlineDataSource.insertCases(allCases) }
        }
}
