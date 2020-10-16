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

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.synchronisation.WeeklySummary
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.BarChartItem
import com.chrisa.covid19.core.ui.widgets.charts.LineChartData
import com.chrisa.covid19.core.ui.widgets.charts.LineChartItem
import com.chrisa.covid19.core.ui.widgets.recyclerview.chart.ChartData
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AreaDetailTestData(
    val metadata: MetadataDto,
    val areaCode: String,
    val areaName: String,
    val areaType: AreaType,
    val cases: List<CaseModel>,
    val caseSummary: WeeklySummary,
    val deaths: List<DeathModel>,
    val deathSummary: WeeklySummary
) {
    private val lastCase = cases.lastOrNull()

    val cumulativeCases: Int
        get() = lastCase?.cumulativeCases ?: 0
    val newCases: Int
        get() = lastCase?.newCases ?: 0
    val latestCases: List<CaseModel>
        get() = cases.takeLast(14)

    val latestDeaths: List<DeathModel>
        get() = deaths.takeLast(14)

    val areaDetailModel: AreaDetailModel
        get() = AreaDetailModel(
            areaType = areaType.value,
            lastSyncedAt = metadata.lastSyncTime,
            allCases = cases,
            caseSummary = caseSummary,
            allDeaths = deaths,
            deathSummary = deathSummary
        )

    val caseChartData: List<ChartData>
        get() = listOf(
            ChartData(
                title = allCasesLabel,
                barChartData = BarChartData(
                    label = allCasesLabel,
                    values = cases.map {
                        BarChartItem(
                            value = it.newCases.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = cases.map {
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
                    values = latestCases.map {
                        BarChartItem(
                            value = it.newCases.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }

                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = latestCases.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            )
        )

    val deathChartData: List<ChartData>
        get() = listOf(
            ChartData(
                title = allDeathsLabel,
                barChartData = BarChartData(
                    label = allDeathsLabel,
                    values = deaths.map {
                        BarChartItem(
                            value = it.newDeaths.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = deaths.map {
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
                    values = latestDeaths.map {
                        BarChartItem(
                            value = it.newDeaths.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }

                ),
                lineChartData = LineChartData(
                    label = rollingAverageLabel,
                    values = latestDeaths.map {
                        LineChartItem(
                            value = it.rollingAverage.toFloat(),
                            label = it.date.format(formatter)
                        )
                    }
                )
            )
        )

    companion object {
        const val allCasesLabel = "All cases"
        const val latestCasesLabel = "Latest cases"
        const val allDeathsLabel = "All deaths"
        const val latestDeathsLabel = "Latest deaths"
        const val rollingAverageLabel = "Rolling average"

        private val formatter = DateTimeFormatter.ofPattern("dd-MMM")
            .withLocale(Locale.UK)
            .withZone(ZoneId.of("GMT"))
    }
}
