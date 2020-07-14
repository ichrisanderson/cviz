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
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.presentation.models.AreaCasesModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class AreaCasesModelMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val formatter = DateTimeFormatter
        .ofPattern("dd-MMM")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    fun mapAreaDetailModel(areaDetailModel: AreaDetailModel): AreaCasesModel {
        return AreaCasesModel(
            lastUpdatedAt = areaDetailModel.lastUpdatedAt,
            latestCasesChartData = BarChartData(
                label = context.getString(R.string.latest_cases_chart_label),
                values = areaDetailModel.latestCases.map(this::mapCaseModelToBarChartItem)
            ),
            allCasesChartData = BarChartData(
                label = context.getString(R.string.all_cases_chart_label),
                values = areaDetailModel.allCases.map(this::mapCaseModelToBarChartItem)
            )
        )
    }

    private fun mapCaseModelToBarChartItem(caseModel: CaseModel): BarChartItem {
        return BarChartItem(
            caseModel.dailyLabConfirmedCases.toFloat(),
            caseModel.date.format(formatter)
        )
    }
}
