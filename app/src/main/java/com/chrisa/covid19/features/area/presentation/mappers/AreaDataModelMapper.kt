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
import com.chrisa.covid19.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.covid19.core.ui.widgets.charts.CombinedChartData
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.presentation.models.AreaDataModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AreaDataModelMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyDataWithRollingAverageBuilder: DailyDataWithRollingAverageBuilder,
    private val chartBuilder: ChartBuilder
) {

    private val supportedAreaTypesForDeaths =
        setOf(AreaType.OVERVIEW.value, AreaType.REGION.value, AreaType.NATION.value)

    fun mapAreaDetailModel(areaDetailModel: AreaDetailModel): AreaDataModel {

        val caseChartData = caseChartData(areaDetailModel)
        val deathChartData = deathChartData(areaDetailModel)

        val canDisplayDeaths =
            canDisplayDeaths(areaDetailModel.areaType) && deathChartData.isNotEmpty()

        return AreaDataModel(
            caseSummary = areaDetailModel.caseSummary,
            caseChartData = caseChartData,
            showDeaths = canDisplayDeaths,
            deathSummary = areaDetailModel.deathSummary,
            deathsChartData = deathChartData
        )
    }

    private fun caseChartData(areaDetailModel: AreaDetailModel): List<CombinedChartData> {
        return chartBuilder.allChartData(
            context.getString(R.string.all_cases_chart_label),
            context.getString(R.string.latest_cases_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(areaDetailModel.allCases)
        )
    }

    private fun deathChartData(areaDetailModel: AreaDetailModel): List<CombinedChartData> {
        return chartBuilder.allChartData(
            context.getString(R.string.all_deaths_chart_label),
            context.getString(R.string.latest_deaths_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(areaDetailModel.allDeaths)
        )
    }

    private fun canDisplayDeaths(areaType: String?): Boolean =
        supportedAreaTypesForDeaths.contains(areaType)
}
