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

package com.chrisa.cviz.features.area.presentation.models

import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.ui.widgets.charts.ChartTab
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import java.time.LocalDate
import java.time.LocalDateTime

data class AreaDataModel(
    val lastUpdatedDate: LocalDateTime?,
    val lastCaseDate: LocalDate?,
    val caseAreaName: String,
    val caseSummary: WeeklySummary,
    val caseChartData: List<ChartTab>,
    val showDeathsByPublishedDate: Boolean,
    val lastDeathPublishedDate: LocalDate?,
    val deathsByPublishedDateAreaName: String,
    val deathsByPublishedDateSummary: WeeklySummary,
    val deathsByPublishedDateChartData: List<ChartTab>,
    val showOnsDeaths: Boolean,
    val lastOnsDeathRegisteredDate: LocalDate?,
    val onsDeathsAreaName: String,
    val onsDeathsByRegistrationDateChartData: List<ChartTab>,
    val showHospitalAdmissions: Boolean,
    val lastHospitalAdmissionDate: LocalDate?,
    val hospitalAdmissionsRegionName: String,
    val hospitalAdmissionsSummary: WeeklySummary,
    val hospitalAdmissions: List<AreaDailyDataDto>,
    val hospitalAdmissionsChartData: List<ChartTab>,
    val canFilterHospitalAdmissionsAreas: Boolean,
    val hospitalAdmissionsAreas: List<HospitalAdmissionsAreaModel>,
    val areaTransmissionRate: AreaTransmissionRateModel?,
    val alertLevel: AlertLevelModel?,
    val soaData: SoaDataModel?
)

data class HospitalAdmissionsAreaModel(
    val areaName: String,
    val isSelected: Boolean
)

data class AreaTransmissionRateModel(
    val areaName: String,
    val lastUpdatedDate: LocalDateTime?,
    val lastRateDate: LocalDate?,
    val minRate: Double,
    val maxRate: Double,
    val minGrowthRate: Double,
    val maxGrowthRate: Double
)

data class AlertLevelModel(
    val alertLevelUrl: String
)

data class SoaDataModel(
    val areaName: String,
    val lastDate: LocalDate?,
    val weeklyCases: Int,
    val changeInCases: Int,
    val weeklyRate: Int,
    val changeInRate: Int,
    val chartData: List<ChartTab>
)
