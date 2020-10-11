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
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.BarChartItem
import com.chrisa.covid19.core.ui.widgets.charts.LineChartData
import com.chrisa.covid19.core.ui.widgets.charts.LineChartItem
import com.chrisa.covid19.core.ui.widgets.recyclerview.chart.ChartData
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Before
import org.junit.Test

class AreaCasesModelMapperTest {

    private val context = mockk<Context>()
    private val sut = AreaCasesModelMapper(context)
    private val allCasesLabel = "All cases"
    private val latestCasesLabel = "Latest cases"
    private val allDeathsLabel = "All deaths"
    private val latestDeathsLabel = "Latest deaths"
    private val rollingAverageLabel = "Rolling average"
    private val formatter = DateTimeFormatter
        .ofPattern("dd-MMM")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

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
        val areaDetailModel = areaDetailModel()

        val mappedModel = sut.mapAreaDetailModel(areaDetailModel)

        assertThat(mappedModel.lastUpdatedAt).isEqualTo(areaDetailModel.lastUpdatedAt)
        assertThat(mappedModel.caseChartData).isEqualTo(caseChartData(areaDetailModel))
        assertThat(mappedModel.deathsByPublishedDateChartData).isEqualTo(
            deathsByPublishedDateChartData(areaDetailModel)
        )
        assertThat(mappedModel.deathsByDeathDateChartData).isEqualTo(
            deathsByDeathDateChartData(areaDetailModel)
        )
    }

    private fun areaDetailModel(): AreaDetailModel {
        val now = LocalDateTime.now()
        return AreaDetailModel(
            lastUpdatedAt = now.minusDays(1),
            weeklyInfectionRate = 0.0,
            changeInInfectionRate = 0.0,
            weeklyCases = 0,
            changeInCases = 0,
            cumulativeCases = 0,
            lastSyncedAt = now,
            allCases = caseModels(),
            deathsByPublishedDate = deathModels(),
            deathsByDeathDate = deathModels()
        )
    }

    private fun caseModels(): List<CaseModel> {
        return listOf(
            CaseModel(
                baseRate = 0.0,
                cumulativeCases = 0,
                newCases = 123,
                date = LocalDate.ofEpochDay(0),
                rollingAverage = 1.1
            )
        )
    }

    private fun deathModels(): List<DeathModel> {
        return listOf(
            DeathModel(
                baseRate = 0.0,
                cumulativeDeaths = 0,
                newDeaths = 123,
                date = LocalDate.ofEpochDay(0),
                rollingAverage = 1.1
            )
        )
    }

    private fun caseChartData(areaDetailModel: AreaDetailModel): List<ChartData> {
        return listOf(
            ChartData(
                title = allCasesLabel,
                barChartData = BarChartData(
                    label = allCasesLabel,
                    values = areaDetailModel.allCases.map {
                        BarChartItem(
                            value = it.newCases.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = areaDetailModel.allCases.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            ),
            ChartData(
                title = latestCasesLabel,
                barChartData = BarChartData(
                    label = latestCasesLabel,
                    values = areaDetailModel.allCases.map {
                        BarChartItem(
                            value = it.newCases.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }

                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = areaDetailModel.allCases.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            )
        )
    }

    private fun deathsByPublishedDateChartData(areaDetailModel: AreaDetailModel): List<ChartData> {
        return listOf(
            ChartData(
                title = allDeathsLabel,
                barChartData = BarChartData(
                    label = allDeathsLabel,
                    values = areaDetailModel.deathsByPublishedDate.map {
                        BarChartItem(
                            value = it.newDeaths.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = areaDetailModel.deathsByPublishedDate.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            ),
            ChartData(
                title = latestDeathsLabel,
                barChartData = BarChartData(
                    label = latestDeathsLabel,
                    values = areaDetailModel.deathsByPublishedDate.map {
                        BarChartItem(
                            value = it.newDeaths.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }

                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = areaDetailModel.deathsByPublishedDate.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            )
        )
    }

    private fun deathsByDeathDateChartData(areaDetailModel: AreaDetailModel): List<ChartData> {
        return listOf(
            ChartData(
                title = allDeathsLabel,
                barChartData = BarChartData(
                    label = allDeathsLabel,
                    values = areaDetailModel.deathsByDeathDate.map {
                        BarChartItem(
                            value = it.newDeaths.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = areaDetailModel.deathsByDeathDate.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            ),
            ChartData(
                title = latestDeathsLabel,
                barChartData = BarChartData(
                    label = latestDeathsLabel,
                    values = areaDetailModel.deathsByDeathDate.map {
                        BarChartItem(
                            value = it.newDeaths.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }

                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = areaDetailModel.deathsByDeathDate.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            )
        )
    }
}
