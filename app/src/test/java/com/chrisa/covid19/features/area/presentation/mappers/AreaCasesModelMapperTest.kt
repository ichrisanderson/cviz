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
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.presentation.widgets.chart.ChartData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AreaCasesModelMapperTest {

    private val context = mockk<Context>()
    private val sut = AreaCasesModelMapper(context)
    private val formatter = DateTimeFormatter
        .ofPattern("dd-MMM")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    @Test
    fun `WHEN mapAreaDetailModel called THEN ui model is mapped correctly`() {
        val caseModels = listOf(
            CaseModel(
                baseRate = 0.0,
                cumulativeCases = 0,
                newCases = 123,
                date = LocalDate.ofEpochDay(0),
                rollingAverage = 1.1
            )
        )
        val now = LocalDateTime.now()
        val areaDetailModel = AreaDetailModel(
            cumulativeCases = 0,
            changeInCases = 0,
            weeklyCases = 0,
            changeInInfectionRate = 0.0,
            weeklyInfectionRate = 0.0,
            lastUpdatedAt = now.minusDays(1),
            lastSyncedAt = now,
            allCases = caseModels
        )
        val dailyLabConfirmedCasesChartData = caseModels.map {
            BarChartItem(
                value = it.newCases.toFloat(),
                label = it.date.format(formatter)
            )
        }
        val rollingAverageChartData = caseModels.map {
            LineChartItem(
                value = it.rollingAverage.toFloat(),
                label = it.date.format(formatter)
            )
        }
        val allCasesLabel = "All cases"
        val latestCasesLabel = "Latest cases"
        val rollingAverageLabel = "Rolling average"

        every { context.getString(R.string.latest_cases_chart_label) } returns latestCasesLabel
        every { context.getString(R.string.all_cases_chart_label) } returns allCasesLabel
        every { context.getString(R.string.rolling_average_chart_label) } returns rollingAverageLabel

        val mappedModel = sut.mapAreaDetailModel(areaDetailModel)

        assertThat(mappedModel.lastUpdatedAt).isEqualTo(areaDetailModel.lastUpdatedAt)
        assertThat(mappedModel.caseChartData).isEqualTo(
            listOf(
                ChartData(
                    title = allCasesLabel,
                    barChartData = BarChartData(
                        label = allCasesLabel,
                        values = dailyLabConfirmedCasesChartData

                    ),
                    lineChartData = LineChartData(
                        label = rollingAverageLabel,
                        values = rollingAverageChartData
                    )
                ),
                ChartData(
                    title = latestCasesLabel,
                    barChartData = BarChartData(
                        label = latestCasesLabel,
                        values = dailyLabConfirmedCasesChartData

                    ),
                    lineChartData = LineChartData(
                        label = rollingAverageLabel,
                        values = rollingAverageChartData
                    )
                )
            )
        )
    }
}
