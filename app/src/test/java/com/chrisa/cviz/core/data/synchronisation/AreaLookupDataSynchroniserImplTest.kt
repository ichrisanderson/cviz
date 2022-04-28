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

package com.chrisa.cviz.core.data.synchronisation

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaLookupDao
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.network.AreaLookupData
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.Utils.emptyJsonResponse
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class AreaLookupDataSynchroniserImplTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaLookupDao = mockk<AreaLookupDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: AreaLookupDataSynchroniserImpl

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.areaLookupDao() } returns areaLookupDao
        every { areaLookupDao.insert(any()) } just Runs
        every { areaLookupDao.byUtla(any()) } returns null
        every { areaLookupDao.byLtla(any()) } returns null
        every { areaLookupDao.byRegion(any()) } returns null
        every { networkUtils.hasNetworkConnection() } returns true

        appDatabase.mockTransaction()

        sut = AreaLookupDataSynchroniserImpl(
            covidApi,
            appDatabase,
            networkUtils
        )
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {
            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync("", AreaType.UTLA)

            coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
        }

    @Test
    fun `GIVEN supported area WHEN performSync THEN api is called`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.areaLookupData(
                    any(),
                    any()
                )
            } returns areaLookupData
            val supportedAreas = setOf(AreaType.UTLA, AreaType.LTLA)
            val areaCode = "1234"

            supportedAreas.forEach { areaType ->
                sut.performSync(areaCode, areaType)

                coVerify(exactly = 1) { covidApi.areaLookupData(areaType.value, areaCode) }
            }
        }

    @Test
    fun `GIVEN non supported area WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {
            val supportedAreas = setOf(AreaType.UTLA, AreaType.LTLA)
            val nonSupportedAreas = AreaType.values().filterNot { supportedAreas.contains(it) }

            nonSupportedAreas.forEach { areaType ->
                sut.performSync("", areaType)

                coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
            }
        }

    @Test
    fun `GIVEN region lookup data exists WHEN performSync THEN api is not called AND association is update`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byRegion(oxfordCentralAreaLookup.regionCode!!) } returns oxfordCentralAreaLookup

            sut.performSync(oxfordCentralAreaLookup.regionCode!!, AreaType.REGION)

            coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
        }

    @Test
    fun `GIVEN utla lookup data exists WHEN performSync THEN api is not called AND association is update`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byUtla(oxfordCentralAreaLookup.utlaCode) } returns oxfordCentralAreaLookup

            sut.performSync(oxfordCentralAreaLookup.utlaCode, AreaType.UTLA)

            coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
        }

    @Test
    fun `GIVEN ltla lookup data exists WHEN performSync THEN api is not called AND association is update`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byLtla(oxfordCentralAreaLookup.ltlaCode) } returns oxfordCentralAreaLookup

            sut.performSync(oxfordCentralAreaLookup.ltlaCode, AreaType.LTLA)

            coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN api fails WHEN performSync THEN HttpException is thrown`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.areaLookupData(
                    any(),
                    any()
                )
            } throws HttpException(Response.error<AreaLookupData>(404, emptyJsonResponse()))

            sut.performSync(oxfordCentralAreaLookup.utlaCode, AreaType.UTLA)
        }

    @Test
    fun `GIVEN api succeeds response WHEN performSync THEN area data is updated`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.areaLookupData(
                    any(),
                    any()
                )
            } returns areaLookupData

            sut.performSync(oxfordCentralAreaLookup.utlaCode, AreaType.UTLA)

            verify(exactly = 1) {
                areaLookupDao.insert(
                    AreaLookupEntity(
                        postcode = areaLookupData.postcode.orEmpty(),
                        trimmedPostcode = areaLookupData.trimmedPostcode.orEmpty(),
                        lsoaCode = areaLookupData.lsoa.orEmpty(),
                        lsoaName = areaLookupData.lsoaName,
                        msoaName = areaLookupData.msoaName,
                        msoaCode = areaLookupData.msoa.orEmpty(),
                        ltlaCode = areaLookupData.ltla,
                        ltlaName = areaLookupData.ltlaName,
                        utlaCode = areaLookupData.utla,
                        utlaName = areaLookupData.utlaName,
                        nhsTrustCode = areaLookupData.nhsTrust,
                        nhsTrustName = areaLookupData.nhsTrustName,
                        nhsRegionCode = areaLookupData.nhsRegion,
                        nhsRegionName = areaLookupData.nhsRegionName,
                        regionCode = areaLookupData.region,
                        regionName = areaLookupData.regionName,
                        nationCode = areaLookupData.nation,
                        nationName = areaLookupData.nationName
                    )
                )
            }
        }

    companion object {
        val areaLookupData = AreaLookupData(
            postcode = "OX1 1AA",
            trimmedPostcode = "OX11AA",
            lsoa = "E01028522",
            lsoaName = "Oxford 008B",
            msoa = "E02005947",
            msoaName = "Oxford Central",
            ltla = "E07000178",
            ltlaName = "Oxford",
            utla = "E10000025",
            utlaName = "Oxfordshire",
            nhsRegion = "E40000005",
            nhsRegionName = "South East",
            nhsTrust = "RTH",
            nhsTrustName = "Oxford University Hospitals NHS Foundation Trust",
            region = "E12000008",
            regionName = "South East",
            nation = "E92000001",
            nationName = "England"
        )
        val oxfordCentralAreaLookup = AreaLookupEntity(
            postcode = "OX1 1AA",
            trimmedPostcode = "OX11AA",
            lsoaCode = "E01028522",
            lsoaName = "Oxford 008B",
            msoaCode = "E02005947",
            msoaName = "Oxford Central",
            ltlaCode = "E07000178",
            ltlaName = "Oxford",
            utlaCode = "E10000025",
            utlaName = "Oxfordshire",
            nhsRegionCode = "E40000005",
            nhsRegionName = "South East",
            nhsTrustCode = "RTH",
            nhsTrustName = "Oxford University Hospitals NHS Foundation Trust",
            regionCode = "E12000008",
            regionName = "South East",
            nationCode = "E92000001",
            nationName = "England"
        )
    }
}
