/*
 * Copyright 2021 Chris Anderson.
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
import com.chrisa.cviz.core.data.db.AreaDao
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaLookupDao
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.network.AreaLookupData
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.util.NetworkUtils
import com.chrisa.cviz.core.util.mockTransaction
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class PostcodeLookupDataSynchroniserImplTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaLookupDao = mockk<AreaLookupDao>()
    private val areaDao = mockk<AreaDao>()
    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var sut: PostcodeLookupDataSynchroniserImpl

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { appDatabase.areaLookupDao() } returns areaLookupDao
        every { appDatabase.areaDao() } returns areaDao
        every { areaLookupDao.insert(any()) } just Runs
        every { areaDao.insert(any()) } just Runs

        appDatabase.mockTransaction()

        sut = PostcodeLookupDataSynchroniserImpl(
            covidApi,
            appDatabase,
            networkUtils
        )
    }

    @Test(expected = IOException::class)
    fun `GIVEN no internet WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byTrimmedPostcode("") } returns null
            every { networkUtils.hasNetworkConnection() } returns false

            sut.performSync("")

            coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
        }

    @Test
    fun `GIVEN postcode data exists WHEN performSync THEN api is not called`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byTrimmedPostcode("") } returns lookupEntity

            sut.performSync("")

            coVerify(exactly = 0) { covidApi.areaLookupData(any(), any()) }
        }

    @Test(expected = HttpException::class)
    fun `GIVEN api fails WHEN performSync is called THEN error is thrown`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byTrimmedPostcode("") } returns null
            coEvery { covidApi.areaLookupData(any(), any()) } throws HttpException(
                Response.error<AreaLookupData>(
                    500,
                    "".toResponseBody("application/json".toMediaType())
                )
            )

            sut.performSync("")

            coVerify(exactly = 1) {
                covidApi.areaLookupData(
                    category = "postcode",
                    search = ""
                )
            }
        }

    @Test
    fun `GIVEN api succeeds WHEN performSync is called THEN lookup is cached`() =
        testDispatcher.runBlockingTest {
            every { areaLookupDao.byTrimmedPostcode("") } returns null
            coEvery { covidApi.areaLookupData(any(), any()) } returns areaLookupData

            sut.performSync("")

            coVerify(exactly = 1) {
                areaLookupDao.insert(
                    AreaLookupEntity(
                        postcode = areaLookupData.postcode,
                        trimmedPostcode = areaLookupData.trimmedPostcode,
                        lsoaCode = areaLookupData.lsoa,
                        lsoaName = areaLookupData.lsoaName,
                        msoaName = areaLookupData.msoaName,
                        msoaCode = areaLookupData.msoa,
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
            coVerify(exactly = 1) {
                areaDao.insert(
                    AreaEntity(
                        areaCode = areaLookupData.msoa,
                        areaName = areaLookupData.msoaName!!,
                        areaType = AreaType.MSOA
                    )
                )
            }
        }

    companion object {
        val areaLookupData = AreaLookupData(
            postcode = "W1 1AA",
            trimmedPostcode = "W11AA",
            lsoa = "E11011",
            lsoaName = "Soho",
            msoa = "E11011",
            msoaName = "Soho",
            ltla = "E1101",
            ltlaName = "Westminster",
            utla = "E1101",
            utlaName = "Westminster",
            nhsRegion = "E111",
            nhsRegionName = "London11",
            nhsTrust = "GUYS",
            nhsTrustName = "St Guys",
            region = "E12000007",
            regionName = "London",
            nation = Constants.ENGLAND_AREA_CODE,
            nationName = Constants.ENGLAND_AREA_NAME
        )
        val lookupEntity = AreaLookupEntity(
            postcode = "",
            trimmedPostcode = "",
            lsoaCode = "",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "",
            utlaName = "",
            nhsTrustCode = null,
            nhsTrustName = null,
            nhsRegionCode = null,
            nhsRegionName = null,
            regionCode = "",
            regionName = null,
            nationCode = "",
            nationName = ""
        )
    }
}
