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

package com.chrisa.cviz.features.home.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.home.data.HomeDataSource
import com.chrisa.cviz.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cviz.features.home.data.dtos.DailyRecordDto
import com.chrisa.cviz.features.home.domain.models.DashboardDataModel
import com.chrisa.cviz.features.home.domain.models.LatestUkDataModel
import com.chrisa.cviz.features.home.domain.models.SummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
class LoadDashboardDataUseCaseTest {

    private val homeDataSource = mockk<HomeDataSource>()
    private val sut = LoadDashboardDataUseCase(homeDataSource)

    @Before
    fun setup() {
        every { homeDataSource.ukOverview() } returns listOf(emptyList<DailyRecordDto>()).asFlow()
        every { homeDataSource.areaSummaries() } returns listOf(emptyList<AreaSummaryDto>()).asFlow()
    }

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {
            val emittedItems = mutableListOf<DashboardDataModel>()
            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()

            sut.execute().collect { emittedItems.add(it) }

            val dashboardDataModel = emittedItems.first()
            val expectedItems = dailyRecords.map {
                LatestUkDataModel(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = it.areaType,
                    newCases = it.newCases,
                    cumulativeCases = it.cumulativeCases,
                    lastUpdated = it.lastUpdated
                )
            }

            assertThat(dashboardDataModel.latestUkData).isEqualTo(expectedItems)
        }

    @Test
    fun `WHEN execute called THEN topNewCases emitted`() =
        runBlockingTest {
            val dailyData = dailyData()
            val emittedItems = mutableListOf<DashboardDataModel>()
            every { homeDataSource.areaSummaries() } returns listOf(dailyData).asFlow()

            sut.execute().collect { emittedItems.add(it) }

            val dashboardDataModel = emittedItems.first()
            val sortedDailyData = dailyData.summaryBy { it.currentNewCases }
            assertThat(dashboardDataModel.topNewCases).isEqualTo(sortedDailyData)
        }

    @Test
    fun `WHEN execute called THEN changeInCases emitted`() =
        runBlockingTest {
            val dailyData = dailyData()
            val emittedItems = mutableListOf<DashboardDataModel>()
            every { homeDataSource.areaSummaries() } returns listOf(dailyData).asFlow()

            sut.execute().collect { emittedItems.add(it) }

            val dashboardDataModel = emittedItems.first()
            val sortedDailyData = dailyData.summaryBy { it.changeInCases }
            assertThat(dashboardDataModel.risingNewCases).isEqualTo(sortedDailyData)
        }

    @Test
    fun `WHEN execute called THEN topInfectionRates emitted`() =
        runBlockingTest {
            val dailyData = dailyData()
            val emittedItems = mutableListOf<DashboardDataModel>()
            every { homeDataSource.areaSummaries() } returns listOf(dailyData).asFlow()

            sut.execute().collect { emittedItems.add(it) }

            val dashboardDataModel = emittedItems.first()
            val sortedDailyData = dailyData.summaryBy { it.currentInfectionRate }
            assertThat(dashboardDataModel.topInfectionRates).isEqualTo(sortedDailyData)
        }

    @Test
    fun `WHEN execute called THEN risingInfectionRates emitted`() =
        runBlockingTest {
            val dailyData = dailyData()
            val emittedItems = mutableListOf<DashboardDataModel>()
            every { homeDataSource.areaSummaries() } returns listOf(dailyData).asFlow()

            sut.execute().collect { emittedItems.add(it) }

            val dashboardDataModel = emittedItems.first()
            val sortedDailyData = dailyData.summaryBy { it.changeInInfectionRate }
            assertThat(dashboardDataModel.risingInfectionRates).isEqualTo(sortedDailyData)
        }

    companion object {

        private val dailyRecordDto = DailyRecordDto(
            areaCode = Constants.UK_AREA_CODE,
            areaType = AreaType.OVERVIEW.value,
            areaName = Constants.UK_AREA_NAME,
            cumulativeCases = 122,
            newCases = 22,
            lastUpdated = LocalDateTime.of(2020, 5, 6, 1, 1)
        )

        private val dailyRecords = listOf(dailyRecordDto)

        private val random = Random(0)
        private fun dailyData(start: Int = 1, end: Int = 100): List<AreaSummaryDto> {
            var cumulativeCases = 0
            var infectionRate = 0.0
            return (start..end).map {
                val newCases = random.nextInt(100)
                val currentInfectionRate = random.nextDouble(100.0, 150.0)
                val changeInInfectionRate = currentInfectionRate - infectionRate
                infectionRate = currentInfectionRate
                cumulativeCases += newCases
                AreaSummaryDto(
                    areaCode = Constants.UK_AREA_CODE,
                    areaName = Constants.UK_AREA_NAME,
                    areaType = AreaType.OVERVIEW.value,
                    changeInCases = newCases,
                    currentNewCases = cumulativeCases,
                    changeInInfectionRate = changeInInfectionRate,
                    currentInfectionRate = infectionRate
                )
            }
        }

        inline fun <R : Comparable<R>> List<AreaSummaryDto>.summaryBy(crossinline selector: (AreaSummaryDto) -> R?): List<SummaryModel> =
            this.sortedByDescending(selector)
                .take(10)
                .mapIndexed { index, areaSummary ->
                    SummaryModel(
                        position = index + 1,
                        areaCode = areaSummary.areaCode,
                        areaName = areaSummary.areaName,
                        areaType = areaSummary.areaType,
                        changeInCases = areaSummary.changeInCases,
                        currentNewCases = areaSummary.currentNewCases,
                        changeInInfectionRate = areaSummary.changeInInfectionRate,
                        currentInfectionRate = areaSummary.currentInfectionRate
                    )
                }
    }
}
