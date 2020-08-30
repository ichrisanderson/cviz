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

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaDataModelStructureMapper
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.DAILY_AREA_DATA_FILTER
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.util.DateUtils.formatAsIso8601
import com.chrisa.covid19.core.util.NetworkUtils
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MonthlyDataLoaderTest {

    private val covidApi = mockk<CovidApi>()
    private val networkUtils = mockk<NetworkUtils>()
    private val areaDataModelStructureMapper = mockk<AreaDataModelStructureMapper>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val areaDataModel = "{}"
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private val syncDate = syncTime.toLocalDate()
    private val lastDate = syncDate.minusDays(3)
    private val emptyPage = Page<AreaDataModel>(length = 0, maxPageLimit = null, data = emptyList())
    private val week1Data = AreaDataModel(
        areaCode = "LDN",
        areaName = "London",
        areaType = AreaType.REGION.value,
        date = lastDate,
        cumulativeCases = 100,
        newCases = 10,
        infectionRate = 100.0
    )
    private val week2Data = week1Data.copy(
        date = week1Data.date.minusDays(7),
        cumulativeCases = 85,
        newCases = 8,
        infectionRate = 90.0
    )
    private val week3Data = week2Data.copy(
        date = week2Data.date.minusDays(7),
        cumulativeCases = 70,
        newCases = 7,
        infectionRate = 82.0
    )
    private val week4Data = week3Data.copy(
        date = week3Data.date.minusDays(7),
        cumulativeCases = 64,
        newCases = 8,
        infectionRate = 85.0
    )

    private lateinit var sut: MonthlyDataLoader

    @Before
    fun setup() {
        every { networkUtils.hasNetworkConnection() } returns true
        every { areaDataModelStructureMapper.mapAreaTypeToDataModel(any()) } returns areaDataModel

        sut = MonthlyDataLoader(covidApi, areaDataModelStructureMapper)
    }

    @Test(expected = IOException::class)
    fun `GIVEN api fails WHEN performSync THEN http exception is thrown`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), AreaType.LTLA.value),
                    areaDataModel
                )
            } throws IOException()

            sut.load(lastDate, AreaType.LTLA)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN api returns empty list WHEN performSync THEN http exception is thrown`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), AreaType.LTLA.value),
                    areaDataModel
                )
            } returns emptyPage

            sut.load(lastDate, AreaType.LTLA)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN api returns list with different week 2 items WHEN performSync THEN http exception is thrown`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), AreaType.LTLA.value),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week1Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(7).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 0, maxPageLimit = null, data = listOf(week1Data))

            sut.load(lastDate, AreaType.LTLA)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN api returns list with different week 3 items WHEN performSync THEN http exception is thrown`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), AreaType.LTLA.value),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week1Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(7).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week1Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(14).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 0, maxPageLimit = null, data = listOf(week2Data))

            sut.load(lastDate, AreaType.LTLA)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `GIVEN api returns list with different week 4 items WHEN performSync THEN http exception is thrown`() =
        testDispatcher.runBlockingTest {
            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), AreaType.LTLA.value),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week1Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(7).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week2Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(14).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week3Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(21).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 0, maxPageLimit = null, data = listOf(week4Data))

            sut.load(lastDate, AreaType.LTLA)
        }

    @Test
    fun `GIVEN api returns list with valid week data WHEN performSync THEN http exception is thrown`() =
        testDispatcher.runBlockingTest {

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), AreaType.LTLA.value),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week1Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(7).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week2Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(14).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week3Data))

            coEvery {
                covidApi.pagedAreaData(
                    null,
                    DAILY_AREA_DATA_FILTER(
                        lastDate.minusDays(21).formatAsIso8601(),
                        AreaType.LTLA.value
                    ),
                    areaDataModel
                )
            } returns Page(length = 1, maxPageLimit = null, data = listOf(week4Data))

            val monthlyData = sut.load(lastDate, AreaType.LTLA)
            assertThat(monthlyData).isEqualTo(
                MonthlyData(
                    lastDate = lastDate,
                    areaType = AreaType.LTLA,
                    week1 = Page(length = 1, maxPageLimit = null, data = listOf(week1Data)),
                    week2 = Page(length = 1, maxPageLimit = null, data = listOf(week2Data)),
                    week3 = Page(length = 1, maxPageLimit = null, data = listOf(week3Data)),
                    week4 = Page(length = 1, maxPageLimit = null, data = listOf(week4Data))
                )
            )
        }
}
