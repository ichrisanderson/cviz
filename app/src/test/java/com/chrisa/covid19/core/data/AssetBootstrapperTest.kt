package com.chrisa.covid19.core.data

import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.data.network.DeathsModel
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class AssetBootstrapperTest {

    private val assetDataSource = mockk<AssetDataSource>(relaxed = true)
    private val offlineDataSource = mockk<OfflineDataSource>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)
    private val casesModel = mockk<CasesModel>(relaxed = true)
    private val deathsModel = mockk<DeathsModel>(relaxed = true)

    private lateinit var sut: AssetBootstrapper

    @Before
    fun setup() {

        coEvery { assetDataSource.getCases() } returns casesModel
        coEvery { assetDataSource.getDeaths() } returns deathsModel

        sut = AssetBootstrapper(
            assetDataSource,
            offlineDataSource,
            TestCoroutineDispatchersImpl(testDispatcher)
        )
    }

    @Test
    fun `GIVEN no offline cases WHEN bootstrap data THEN case data is updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.casesCount() } returns 0

            sut.bootstrapData()

            val allCases = casesModel.countries.union(casesModel.ltlas).union(casesModel.utlas)
                .union(casesModel.regions)

            verify { offlineDataSource.insertCaseMetadata(casesModel.metadata) }
            verify {
                offlineDataSource.insertDailyRecord(
                    casesModel.dailyRecords,
                    casesModel.metadata.lastUpdatedAt
                )
            }
            verify { offlineDataSource.insertCases(allCases) }
        }

    @Test
    fun `GIVEN offline cases WHEN bootstrap data THEN case data is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.casesCount() } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                offlineDataSource.insertCaseMetadata(any())
            }

            verify(exactly = 0) {
                offlineDataSource.insertDailyRecord(any(), any())
            }
            verify(exactly = 0) {
                offlineDataSource.insertCases(any())
            }
        }

    @Test
    fun `GIVEN no offline deaths WHEN bootstrap data THEN offline deaths are updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.deathsCount() } returns 0

            sut.bootstrapData()

            val allDeaths = deathsModel.countries.union(deathsModel.overview)

            verify(exactly = 1) {
                offlineDataSource.insertDeathMetadata(deathsModel.metadata)
            }
            verify(exactly = 1) {
                offlineDataSource.insertDeaths(allDeaths)
            }
        }

    @Test
    fun `GIVEN offline deaths WHEN bootstrap data THEN offline deaths not are updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.deathsCount() } returns 1

            sut.bootstrapData()

            verify(exactly = 0) { offlineDataSource.insertDeathMetadata(any()) }
            verify(exactly = 0) { offlineDataSource.insertDeaths(any()) }
        }
}
