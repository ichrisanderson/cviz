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
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import java.time.LocalDate
import java.time.LocalDateTime

data class AreaDataModel(
    val lastUpdatedDate: LocalDateTime?,
    val lastCaseDate: LocalDate?,
    val caseAreaName: String,
    val caseSummary: WeeklySummary,
    val caseChartData: List<CombinedChartData>,
    val showDeathsByPublishedDate: Boolean,
    val lastDeathPublishedDate: LocalDate?,
    val deathsByPublishedDateAreaName: String,
    val deathsByPublishedDateSummary: WeeklySummary,
    val deathsByPublishedDateChartData: List<CombinedChartData>,
    val showOnsDeaths: Boolean,
    val lastOnsDeathRegisteredDate: LocalDate?,
    val onsDeathsAreaName: String,
    val onsDeathsByRegistrationDateChartData: List<BarChartData>,
    val showHospitalAdmissions: Boolean,
    val lastHospitalAdmissionDate: LocalDate?,
    val hospitalAdmissionsRegionName: String,
    val hospitalAdmissionsSummary: WeeklySummary,
    val hospitalAdmissions: List<AreaDailyDataDto>,
    val hospitalAdmissionsChartData: List<CombinedChartData>,
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
    val date: LocalDate,
    val totalCases: Int,
    val changeInCases: Int,
    val changeInCasesPercentage: Double,
    val rollingRate: Double
)
