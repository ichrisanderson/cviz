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
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
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
    ): List<CombinedChartData> {
        val dailyData = soaDailyDataMapper.mapToDailyData(data)
        return chartBuilder.allCombinedChartData(
            context.getString(R.string.all_cases_chart_label),
            context.getString(R.string.latest_cases_chart_label),
            context.getString(R.string.rolling_average_chart_label),
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyData)
        )
    }
}

class SoaDailyDataMapper @Inject constructor() {
    fun mapToDailyData(soaDataList: List<SoaData>): List<DailyData> =
        soaDataList.map { soaData ->
            DailyData(
                newValue = soaData.rollingSum,
                cumulativeValue = 0,
                rate = soaData.rollingRate,
                date = soaData.date
            )
        }
}
