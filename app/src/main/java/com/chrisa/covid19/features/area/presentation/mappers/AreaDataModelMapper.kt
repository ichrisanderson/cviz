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
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.BarChartItem
import com.chrisa.covid19.core.ui.widgets.charts.LineChartData
import com.chrisa.covid19.core.ui.widgets.charts.LineChartItem
import com.chrisa.covid19.core.ui.widgets.recyclerview.chart.ChartData
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel
import com.chrisa.covid19.features.area.presentation.models.AreaDataModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class AreaDataModelMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val formatter = DateTimeFormatter
        .ofPattern("dd-MMM")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    private val supportedAreaTypesForDeaths =
        setOf(AreaType.OVERVIEW.value, AreaType.REGION.value, AreaType.NATION.value)

    fun mapAreaDetailModel(areaDetailModel: AreaDetailModel): AreaDataModel {
        val caseChartData = caseChartData(areaDetailModel.allCases)
        val deathChartData = deathsChartData(areaDetailModel.allDeaths)
        val canDisplayDeaths = canDisplayDeaths(areaDetailModel.areaType) &&
            deathChartData.isNotEmpty()

        return AreaDataModel(
            caseSummary = areaDetailModel.caseSummary,
            caseChartData = caseChartData,
            showDeaths = canDisplayDeaths,
            deathSummary = areaDetailModel.deathSummary,
            deathsChartData = deathChartData
        )
    }

    private fun canDisplayDeaths(areaType: String?): Boolean =
        supportedAreaTypesForDeaths.contains(areaType)

    private fun caseChartData(
        allCases: List<CaseModel>
    ): List<ChartData> {
        val latestCases = allCases.takeLast(14)
        val allCasesChartLabel = context.getString(R.string.all_cases_chart_label)
        val latestCasesChartLabel = context.getString(R.string.latest_cases_chart_label)
        val rollingAverageChartLabel = context.getString(R.string.rolling_average_chart_label)
        return listOf(
            ChartData(
                title = allCasesChartLabel,
                barChartData = BarChartData(
                    label = allCasesChartLabel,
                    values = allCases.map(this::mapCaseModelToBarChartItem)
                ),
                lineChartData = LineChartData(
                    label = rollingAverageChartLabel,
                    values = allCases.map(this::mapCaseModelToLineChartItem)
                )
            ),
            ChartData(
                title = latestCasesChartLabel,
                barChartData = BarChartData(
                    label = latestCasesChartLabel,
                    values = latestCases.map(this::mapCaseModelToBarChartItem)
                ),
                lineChartData = LineChartData(
                    label = rollingAverageChartLabel,
                    values = latestCases.map(this::mapCaseModelToLineChartItem)
                )
            )
        )
    }

    private fun deathsChartData(
        allDeaths: List<DeathModel>
    ): List<ChartData> {
        if (allDeaths.isEmpty()) return emptyList()
        val latestDeaths = allDeaths.takeLast(14)
        val allDeathsChartLabel = context.getString(R.string.all_deaths_chart_label)
        val latestDeathsChartLabel = context.getString(R.string.latest_deaths_chart_label)
        val rollingAverageChartLabel = context.getString(R.string.rolling_average_chart_label)
        return listOf(
            ChartData(
                title = allDeathsChartLabel,
                barChartData = BarChartData(
                    label = allDeathsChartLabel,
                    values = allDeaths.map(this::mapDeathModelToBarChartItem)
                ),
                lineChartData = LineChartData(
                    label = rollingAverageChartLabel,
                    values = allDeaths.map(this::mapDeathModelToLineChartItem)
                )
            ),
            ChartData(
                title = latestDeathsChartLabel,
                barChartData = BarChartData(
                    label = latestDeathsChartLabel,
                    values = latestDeaths.map(this::mapDeathModelToBarChartItem)
                ),
                lineChartData = LineChartData(
                    label = rollingAverageChartLabel,
                    values = latestDeaths.map(this::mapDeathModelToLineChartItem)
                )
            )
        )
    }

    private fun mapCaseModelToBarChartItem(caseModel: CaseModel): BarChartItem {
        return BarChartItem(
            caseModel.newCases.toFloat(),
            caseModel.date.format(formatter)
        )
    }

    private fun mapCaseModelToLineChartItem(caseModel: CaseModel): LineChartItem {
        return LineChartItem(
            caseModel.rollingAverage.toFloat(),
            caseModel.date.format(formatter)
        )
    }

    private fun mapDeathModelToBarChartItem(deathModel: DeathModel): BarChartItem {
        return BarChartItem(
            deathModel.newDeaths.toFloat(),
            deathModel.date.format(formatter)
        )
    }

    private fun mapDeathModelToLineChartItem(deathModel: DeathModel): LineChartItem {
        return LineChartItem(
            deathModel.rollingAverage.toFloat(),
            deathModel.date.format(formatter)
        )
    }
}
