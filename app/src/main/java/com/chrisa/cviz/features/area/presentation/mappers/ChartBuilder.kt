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

import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverage
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartDataBuilder
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartDataBuilder
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ChartBuilder @Inject constructor(
    private val formatter: DateTimeFormatter,
    private val combinedChartDataBuilder: CombinedChartDataBuilder,
    private val barChartDataBuilder: BarChartDataBuilder
) {

    fun allCombinedChartData(
        allChartLabel: String,
        latestChartLabel: String,
        rollingAverageChartLabel: String,
        data: List<DailyDataWithRollingAverage>
    ): List<CombinedChartData> {
        return when {
            data.isEmpty() -> emptyList()
            else -> listOf(
                combinedChartData(allChartLabel, rollingAverageChartLabel, data),
                combinedChartData(latestChartLabel, rollingAverageChartLabel, data.takeLast(14))
            )
        }
    }

    private fun combinedChartData(
        barChartLabel: String,
        lineChartLabel: String,
        data: List<DailyDataWithRollingAverage>
    ): CombinedChartData {
        return combinedChartDataBuilder.build(
            barChartLabel,
            data.map(::mapDailyDataToBarChartItem),
            lineChartLabel,
            data.map(::mapDailyDataToLineChartItem)
        )
    }

    private fun mapDailyDataToBarChartItem(dailyData: DailyDataWithRollingAverage): BarChartItem {
        return BarChartItem(
            dailyData.newValue.toFloat(),
            dailyData.date.format(formatter)
        )
    }

    private fun mapDailyDataToLineChartItem(dailyData: DailyDataWithRollingAverage): LineChartItem {
        return LineChartItem(
            dailyData.rollingAverage.toFloat(),
            dailyData.date.format(formatter)
        )
    }

    fun allBarChartData(
        allChartLabel: String,
        latestChartLabel: String,
        data: List<DailyData>
    ): List<BarChartData> {
        return when {
            data.isEmpty() -> emptyList()
            else -> listOf(
                barChartData(allChartLabel, data),
                barChartData(latestChartLabel, data.takeLast(14))
            )
        }
    }

    private fun barChartData(
        label: String,
        data: List<DailyData>
    ): BarChartData =
        barChartDataBuilder.build(
            label,
            values = data.map(::mapDailyDataToBarChartItem)
        )

    private fun mapDailyDataToBarChartItem(dailyData: DailyData): BarChartItem {
        return BarChartItem(
            dailyData.newValue.toFloat(),
            dailyData.date.format(formatter)
        )
    }
}
