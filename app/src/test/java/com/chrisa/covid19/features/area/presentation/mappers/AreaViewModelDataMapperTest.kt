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

package com.chrisa.covid19.features.area.presentation.mappers

import android.content.Context
import com.chrisa.covid19.R
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.synchronisation.WeeklySummary
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel
import com.chrisa.covid19.features.area.presentation.mappers.AreaDetailTestData.Companion.allCasesLabel
import com.chrisa.covid19.features.area.presentation.mappers.AreaDetailTestData.Companion.allDeathsLabel
import com.chrisa.covid19.features.area.presentation.mappers.AreaDetailTestData.Companion.latestCasesLabel
import com.chrisa.covid19.features.area.presentation.mappers.AreaDetailTestData.Companion.latestDeathsLabel
import com.chrisa.covid19.features.area.presentation.mappers.AreaDetailTestData.Companion.rollingAverageLabel
import com.chrisa.covid19.features.area.presentation.models.AreaViewModelData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

class AreaViewModelDataMapperTest {

    private val context = mockk<Context>()
    private val sut = AreaViewModelDataMapper(context)

    @Before
    fun setup() {
        every { context.getString(R.string.all_cases_chart_label) } returns allCasesLabel
        every { context.getString(R.string.latest_cases_chart_label) } returns latestCasesLabel
        every { context.getString(R.string.all_deaths_chart_label) } returns allDeathsLabel
        every { context.getString(R.string.latest_deaths_chart_label) } returns latestDeathsLabel
        every { context.getString(R.string.rolling_average_chart_label) } returns rollingAverageLabel
    }

    @Test
    fun `WHEN mapAreaDetailModel called THEN ui model is mapped correctly`() {
        with(areaDetail) {
            val mappedModel = sut.mapAreaDetailModel(areaDetailModel)

            assertThat(mappedModel).isEqualTo(
                AreaViewModelData(
                    totalCases = areaDetailModel.cumulativeCases,
                    lastUpdatedAt = areaDetailModel.lastUpdatedAt,
                    caseChartData = caseChartData,
                    weeklyCaseSummary = areaDetailModel.weeklyCaseSummary,
                    showDeathsByPublishedDateChartData = true,
                    deathsByPublishedDateChartData = deathChartData,
                    weeklyDeathSummary = areaDetailModel.weeklyDeathSummary
                )
            )
        }
    }

    companion object {

        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val areaDetail =
            AreaDetailTestData(
                metadata = MetadataDto(
                    lastUpdatedAt = syncDate.minusDays(1),
                    lastSyncTime = syncDate
                ),
                areaCode = Constants.UK_AREA_CODE,
                areaName = "United Kingdom",
                areaType = AreaType.OVERVIEW,
                cases = cases(),
                weeklyCaseSummary = WeeklySummary(
                    weeklyTotal = 1000,
                    changeInTotal = 100,
                    weeklyRate = 120.0,
                    changeInRate = 20.0
                ),
                deaths = deaths(),
                weeklyDeathSummary = WeeklySummary(
                    weeklyTotal = 2321,
                    changeInTotal = 212,
                    weeklyRate = 32.0,
                    changeInRate = 12.0
                )
            )

        private fun cases(): List<CaseModel> {
            var cumulativeCases = 0
            return (1 until 100).map {
                cumulativeCases += it
                CaseModel(
                    newCases = it,
                    cumulativeCases = cumulativeCases,
                    date = LocalDate.ofEpochDay(it.toLong()),
                    rollingAverage = 1.0,
                    baseRate = 0.0
                )
            }
        }

        private fun deaths(): List<DeathModel> {
            var cumulativeDeaths = 0
            return (1 until 100).map {
                cumulativeDeaths += it
                DeathModel(
                    newDeaths = it,
                    cumulativeDeaths = cumulativeDeaths,
                    date = LocalDate.ofEpochDay(it.toLong()),
                    rollingAverage = 3.0,
                    baseRate = 0.0
                )
            }
        }
    }
}
