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

package com.chrisa.cron19.features.home.domain

import com.chrisa.cron19.core.data.db.AreaType
import com.chrisa.cron19.core.data.db.Constants
import com.chrisa.cron19.features.home.data.HomeDataSource
import com.chrisa.cron19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cron19.features.home.data.dtos.DailyRecordDto
import com.chrisa.cron19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.cron19.features.home.domain.models.DashboardDataModel
import com.chrisa.cron19.features.home.domain.models.LatestUkDataModel
import com.chrisa.cron19.features.home.domain.models.SummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
class LoadDashboardDataUseCaseTest {

    private val homeDataSource = mockk<HomeDataSource>()
    private val sut = LoadDashboardDataUseCase(homeDataSource)

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {

            val dailyRecordDto = DailyRecordDto(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW.value,
                areaName = "United Kingdom",
                cumulativeCases = 122,
                newCases = 22,
                lastUpdated = LocalDateTime.of(2020, 5, 6, 1, 1)
            )

            val dailyRecords = listOf(dailyRecordDto)

            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()
            every { homeDataSource.areaSummaries() } returns listOf(emptyList<AreaSummaryDto>()).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(emptyList<SavedAreaCaseDto>()).asFlow()

            val emittedItems = mutableListOf<DashboardDataModel>()

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
    fun `WHEN execute called THEN new cases list is emitted`() =
        runBlockingTest {

            val newCaseDto = AreaSummaryDto(
                areaName = "UK",
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW.value,
                changeInCases = 10,
                currentNewCases = 100,
                changeInInfectionRate = 10.0,
                currentInfectionRate = 100.0
            )

            val newCases = listOf(newCaseDto)

            every { homeDataSource.areaSummaries() } returns listOf(newCases).asFlow()
            every { homeDataSource.ukOverview() } returns listOf(emptyList<DailyRecordDto>()).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(emptyList<SavedAreaCaseDto>()).asFlow()

            val emittedItems = mutableListOf<DashboardDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            val dashboardDataModel = emittedItems.first()

            assertThat(dashboardDataModel.topNewCases).isEqualTo(newCases.mapIndexed { index, areaSummary ->
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
            })
        }
}
