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

package com.chrisa.cviz.features.area.presentation.mappers

import android.content.Context
import com.chrisa.cviz.R
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import com.chrisa.cviz.features.area.presentation.models.AreaMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AreaDataModelMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyDataWithRollingAverageBuilder: DailyDataWithRollingAverageBuilder,
    private val weeklySummaryBuilder: WeeklySummaryBuilder,
    private val chartBuilder: ChartBuilder
) {

    fun mapAreaDetailModel(areaDetailModel: AreaDetailModel): AreaDataModel {
        val areaMetadata = AreaMetadata(
            lastUpdatedDate = areaDetailModel.lastUpdatedAt,
            lastCaseDate = areaDetailModel.cases.lastOrNull()?.date,
            lastDeathDate = areaDetailModel.deaths.lastOrNull()?.date,
            lastHospitalAdmissionDate = areaDetailModel.hospitalAdmissions.lastOrNull()?.date
        )
        val caseChartData = caseChartData(areaDetailModel.cases)
        val deathChartData = deathChartData(areaDetailModel.deaths)
        val hospitalAdmissionsChartData =
            hospitalAdmissionsChartData(areaDetailModel.hospitalAdmissions)

        val canDisplayDeaths = deathChartData.isNotEmpty()
        val canDisplayHospitalAdmissions = areaDetailModel.hospitalAdmissions.isNotEmpty()

        return AreaDataModel(
            areaMetadata = areaMetadata,
            caseSummary = weeklySummary(areaDetailModel.cases),
            caseChartData = caseChartData,
            showDeaths = canDisplayDeaths,
            deathSummary = weeklySummary(areaDetailModel.deaths),
            deathsChartData = deathChartData,
            showHospitalAdmissions = canDisplayHospitalAdmissions,
            hospitalAdmissionsRegion = areaDetailModel.hospitalAdmissionsRegion,
            hospitalAdmissionsSummary = weeklySummary(areaDetailModel.hospitalAdmissions),
            hospitalAdmissionsChartData = hospitalAdmissionsChartData
        )
    }

    private fun weeklySummary(dailyData: List<DailyData>): WeeklySummary =
        weeklySummaryBuilder.buildWeeklySummary(dailyData)

    private fun caseChartData(dailyData: List<DailyData>): List<CombinedChartData> {
        return chartBuilder.allChartData(
            context.getString(R.string.all_cases_chart_label),
            context.getString(R.string.latest_cases_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }

    private fun deathChartData(dailyData: List<DailyData>): List<CombinedChartData> {
        return chartBuilder.allChartData(
            context.getString(R.string.all_deaths_chart_label),
            context.getString(R.string.latest_deaths_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }

    private fun hospitalAdmissionsChartData(dailyData: List<DailyData>): List<CombinedChartData> {
        return chartBuilder.allChartData(
            context.getString(R.string.all_hospital_admissions_chart_label),
            context.getString(R.string.latest_hospital_admissions_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }
}
