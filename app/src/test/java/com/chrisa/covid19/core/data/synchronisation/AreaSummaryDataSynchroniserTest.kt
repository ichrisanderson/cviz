package com.chrisa.covid19.core.data.synchronisation

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataDao
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.network.AreaDataModelStructureMapper
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.util.NetworkUtils
import com.chrisa.covid19.core.util.mockTransaction
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AreaSummaryDataSynchroniserTest {


    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val metadataDao = mockk<MetadataDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val areaDataModelStructureMapper = mockk<AreaDataModelStructureMapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: AreaSummaryDataSynchroniser

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { areaDataDao.insertAll(any()) } just Runs
        every { metadataDao.insert(any()) } just Runs

        appDatabase.mockTransaction()

        sut =
            AreaSummaryDataSynchroniser(
                networkUtils,
                covidApi,
                appDatabase,
                areaDataModelStructureMapper
            )
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {

            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync()

            coVerify(exactly = 0) { covidApi.pagedAreaDataResponse(any(), any(), any()) }
        }

}
