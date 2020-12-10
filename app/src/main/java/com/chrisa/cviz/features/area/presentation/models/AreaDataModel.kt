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
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData

data class AreaDataModel(
    val areaMetadata: AreaMetadata,
    val caseSummary: WeeklySummary,
    val deathsByPublishedDateSummary: WeeklySummary,
    val caseChartData: List<CombinedChartData>,
    val canDisplayDeathsByPublishedDate: Boolean,
    val deathsByPublishedDateChartData: List<CombinedChartData>,
    val canDisplayOnsDeathsByRegistrationDate: Boolean,
    val onsDeathsByRegistrationDateChartData: List<CombinedChartData>,
    val showHospitalAdmissions: Boolean,
    val hospitalAdmissionsRegion: String,
    val hospitalAdmissionsSummary: WeeklySummary,
    val hospitalAdmissionsChartData: List<CombinedChartData>
)
