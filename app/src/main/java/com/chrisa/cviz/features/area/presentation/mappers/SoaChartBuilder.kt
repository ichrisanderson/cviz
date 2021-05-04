/*
 * Copyright 2021 Chris Anderson.
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
import com.chrisa.cviz.core.ui.widgets.charts.ChartTab
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetColumnHeaders
import com.chrisa.cviz.features.area.domain.models.SoaData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SoaChartBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyDataWithRollingAverageBuilder: DailyDataWithRollingAverageBuilder,
    private val soaDailyDataMapper: SoaDailyDataMapper,
    private val chartBuilder: ChartBuilder
) {

    fun caseChartData(
        data: List<SoaData>
    ): List<ChartTab> {
        val dailyData = soaDailyDataMapper.mapToDailyData(data)
        return chartBuilder.allCombinedChartData(
            context.getString(R.string.all_cases_chart_label),
            context.getString(R.string.latest_cases_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            context.getString(R.string.data_tab_label),
            DataSheetColumnHeaders(
                context.getString(R.string.date_column_header),
                context.getString(R.string.new_cases_column_header),
                context.getString(R.string.total_cases_column_header)
            ),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }
}

class SoaDailyDataMapper @Inject constructor() {
    fun mapToDailyData(soaDataList: List<SoaData>): List<DailyData> {
        val dateOrder = soaDataList.sortedBy { it.date }
        var cumulativeValue = 0
        val data = dateOrder.map { soaData ->
            cumulativeValue += soaData.rollingSum
            DailyData(
                newValue = soaData.rollingSum,
                cumulativeValue = cumulativeValue,
                rate = soaData.rollingRate,
                date = soaData.date
            )
        }
        return data.sortedByDescending { it.date }
    }
}
