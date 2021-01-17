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
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import com.chrisa.cviz.features.area.presentation.models.HospitalAdmissionsAreaModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AreaDataModelMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyDataWithRollingAverageBuilder: DailyDataWithRollingAverageBuilder,
    private val weeklySummaryBuilder: WeeklySummaryBuilder,
    private val chartBuilder: ChartBuilder,
    private val admissionsFilter: AdmissionsFilter
) {

    fun mapAreaDetailModel(
        areaDetailModel: AreaDetailModel,
        hospitalAdmissionFilter: Set<String>
    ): AreaDataModel {
        val filteredHospitalData =
            admissionsFilter.filterHospitalData(
                areaDetailModel.hospitalAdmissions,
                hospitalAdmissionFilter
            )
        val caseChartData = caseChartData(areaDetailModel.cases)
        val deathsByPublishedDateChartData =
            deathsChartData(areaDetailModel.deathsByPublishedDate)
        val onsDeathsChartData =
            onsDeathsChartData(areaDetailModel.onsDeathsByRegistrationDate)
        val hospitalAdmissionsChartData =
            hospitalAdmissionsChartData(filteredHospitalData)
        val hospitalAdmissionsAreas =
            hospitalAdmissionAreas(areaDetailModel.hospitalAdmissions, hospitalAdmissionFilter)

        val canDisplayHospitalAdmissions = filteredHospitalData.isNotEmpty()
        val canDisplayDeathsByPublishedDate = deathsByPublishedDateChartData.isNotEmpty()
        val canDisplayOnsDeaths = onsDeathsChartData.isNotEmpty()
        val canFilterHospitalAdmissionsAreas = hospitalAdmissionsAreas.size > 1

        return AreaDataModel(
            lastUpdatedDate = areaDetailModel.lastUpdatedAt,
            caseAreaName = areaDetailModel.casesAreaName,
            caseSummary = weeklySummary(areaDetailModel.cases),
            lastCaseDate = areaDetailModel.cases.lastOrNull()?.date,
            caseChartData = caseChartData,
            showDeathsByPublishedDate = canDisplayDeathsByPublishedDate,
            lastDeathPublishedDate = areaDetailModel.deathsByPublishedDate.lastOrNull()?.date,
            deathsByPublishedDateAreaName = areaDetailModel.deathsByPublishedDateAreaName,
            deathsByPublishedDateSummary = weeklySummary(areaDetailModel.deathsByPublishedDate),
            deathsByPublishedDateChartData = deathsByPublishedDateChartData,
            showOnsDeaths = canDisplayOnsDeaths,
            lastOnsDeathRegisteredDate = areaDetailModel.onsDeathsByRegistrationDate.lastOrNull()?.date,
            onsDeathsAreaName = areaDetailModel.onsDeathAreaName,
            onsDeathsByRegistrationDateChartData = onsDeathsChartData,
            showHospitalAdmissions = canDisplayHospitalAdmissions,
            lastHospitalAdmissionDate = filteredHospitalData.lastOrNull()?.date,
            hospitalAdmissionsRegionName = areaDetailModel.hospitalAdmissionsAreaName,
            hospitalAdmissions = areaDetailModel.hospitalAdmissions,
            hospitalAdmissionsSummary = weeklySummary(filteredHospitalData),
            hospitalAdmissionsChartData = hospitalAdmissionsChartData,
            canFilterHospitalAdmissionsAreas = canFilterHospitalAdmissionsAreas,
            hospitalAdmissionsAreas = hospitalAdmissionsAreas
        )
    }

    private fun hospitalAdmissionsAreaModel(
        areaDailyData: AreaDailyDataDto,
        hospitalAdmissionFilter: Set<String>
    ): HospitalAdmissionsAreaModel {
        return HospitalAdmissionsAreaModel(
            areaName = areaDailyData.name,
            isSelected = hospitalAdmissionFilter.isEmpty() || hospitalAdmissionFilter.contains(
                areaDailyData.name
            )
        )
    }

    fun updateHospitalAdmissionFilters(
        areaDataModel: AreaDataModel,
        hospitalAdmissionFilter: Set<String>
    ): AreaDataModel {

        val filteredHospitalData =
            admissionsFilter.filterHospitalData(
                areaDataModel.hospitalAdmissions,
                hospitalAdmissionFilter
            )
        val hospitalAdmissionsChartData =
            hospitalAdmissionsChartData(filteredHospitalData)
        val admissionAreas =
            hospitalAdmissionAreas(areaDataModel.hospitalAdmissions, hospitalAdmissionFilter)
        val weeklySummary =
            weeklySummary(filteredHospitalData)

        return areaDataModel.copy(
            lastHospitalAdmissionDate = filteredHospitalData.lastOrNull()?.date,
            hospitalAdmissionsSummary = weeklySummary,
            hospitalAdmissionsChartData = hospitalAdmissionsChartData,
            hospitalAdmissionsAreas = admissionAreas
        )
    }

    private fun hospitalAdmissionAreas(
        hospitalAdmissions: List<AreaDailyDataDto>,
        hospitalAdmissionFilter: Set<String>
    ): List<HospitalAdmissionsAreaModel> =
        hospitalAdmissions
            .sortedBy { it.name }
            .map {
                hospitalAdmissionsAreaModel(it, hospitalAdmissionFilter)
            }

    private fun weeklySummary(dailyData: List<DailyData>): WeeklySummary =
        weeklySummaryBuilder.buildWeeklySummary(dailyData)

    private fun caseChartData(dailyData: List<DailyData>): List<CombinedChartData> {
        return chartBuilder.allCombinedChartData(
            context.getString(R.string.all_cases_chart_label),
            context.getString(R.string.latest_cases_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }

    private fun deathsChartData(dailyData: List<DailyData>): List<CombinedChartData> {
        return chartBuilder.allCombinedChartData(
            context.getString(R.string.all_deaths_chart_label),
            context.getString(R.string.latest_deaths_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }

    private fun onsDeathsChartData(dailyData: List<DailyData>): List<BarChartData> {
        return chartBuilder.allBarChartData(
            context.getString(R.string.all_deaths_chart_label),
            context.getString(R.string.latest_deaths_chart_label),
            dailyData
        )
    }

    private fun hospitalAdmissionsChartData(dailyData: List<DailyData>): List<CombinedChartData> {
        return chartBuilder.allCombinedChartData(
            context.getString(R.string.all_hospital_admissions_chart_label),
            context.getString(R.string.latest_hospital_admissions_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }
}
