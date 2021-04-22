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
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.BarChartTab
import com.chrisa.cviz.core.ui.widgets.charts.BarChartTabBuilder
import com.chrisa.cviz.core.ui.widgets.charts.ChartTab
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTab
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTabBuilder
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetColumnHeaders
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetItem
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetTab
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ChartBuilder @Inject constructor(
    private val formatter: DateTimeFormatter,
    private val combinedChartTabBuilder: CombinedChartTabBuilder,
    private val barChartTabBuilder: BarChartTabBuilder,
    private val dataSheetTabBuilder: DataSheetTabBuilder
) {

    fun allCombinedChartData(
        allChartLabel: String,
        latestChartLabel: String,
        rollingAverageChartLabel: String,
        dataTabLabel: String,
        dataTabColumnHeaders: DataSheetColumnHeaders,
        data: List<DailyDataWithRollingAverage>
    ): List<ChartTab> {
        return when {
            data.isEmpty() -> emptyList()
            else -> listOf(
                combinedChartData(
                    tabTitle = allChartLabel,
                    barChartLabel = allChartLabel,
                    lineChartLabel = rollingAverageChartLabel,
                    data = data
                ),
                combinedChartData(
                    tabTitle = latestChartLabel,
                    barChartLabel = latestChartLabel,
                    lineChartLabel = rollingAverageChartLabel,
                    data = data.takeLast(14)
                ),
                dataSheetTab(
                    tabTitle = dataTabLabel,
                    columnHeaders = dataTabColumnHeaders,
                    data = data.sortedByDescending { it.date }.map(::mapDailyDataWithRollingAverageToDailyChartDataItem)
                )
            )
        }
    }

    private fun combinedChartData(
        tabTitle: String,
        barChartLabel: String,
        lineChartLabel: String,
        data: List<DailyDataWithRollingAverage>
    ): CombinedChartTab {
        return combinedChartTabBuilder.build(
            tabTitle,
            barChartLabel,
            data.map(::mapDailyDataToBarChartItem),
            lineChartLabel,
            data.map(::mapDailyDataToLineChartItem)
        )
    }

    private fun mapDailyDataToBarChartItem(dailyData: DailyDataWithRollingAverage): BarChartItem =
        BarChartItem(
            dailyData.newValue.toFloat(),
            dailyData.date.format(formatter)
        )

    private fun mapDailyDataToLineChartItem(dailyData: DailyDataWithRollingAverage): LineChartItem =
        LineChartItem(
            dailyData.rollingAverage.toFloat(),
            dailyData.date.format(formatter)
        )

    fun allBarChartData(
        allChartLabel: String,
        latestChartLabel: String,
        dataTabLabel: String,
        dataTabColumnHeaders: DataSheetColumnHeaders,
        data: List<DailyData>
    ): List<ChartTab> {
        return when {
            data.isEmpty() -> emptyList()
            else -> listOf(
                barChartData(
                    tabTitle = allChartLabel,
                    barChartLabel = allChartLabel,
                    data = data
                ),
                barChartData(
                    tabTitle = latestChartLabel,
                    barChartLabel = latestChartLabel,
                    data = data.takeLast(14)
                ),
                dataSheetTab(
                    tabTitle = dataTabLabel,
                    columnHeaders = dataTabColumnHeaders,
                    data = data.sortedByDescending { it.date }.map(::mapDailyDataToDailyChartDataItem)
                )
            )
        }
    }

    private fun barChartData(
        tabTitle: String,
        barChartLabel: String,
        data: List<DailyData>
    ): BarChartTab =
        barChartTabBuilder.build(
            tabTitle,
            barChartLabel,
            values = data.map(::mapDailyDataToBarChartItem)
        )

    private fun mapDailyDataToBarChartItem(dailyData: DailyData): BarChartItem {
        return BarChartItem(
            dailyData.newValue.toFloat(),
            dailyData.date.format(formatter)
        )
    }

    private fun dataSheetTab(
        tabTitle: String,
        columnHeaders: DataSheetColumnHeaders,
        data: List<DataSheetItem>
    ): DataSheetTab =
        dataSheetTabBuilder.build(
            tabTitle,
            columnHeaders,
            data
        )

    private fun mapDailyDataWithRollingAverageToDailyChartDataItem(dailyData: DailyDataWithRollingAverage): DataSheetItem {
        return DataSheetItem(
            dailyData.date.format(formatter),
            dailyData.newValue,
            dailyData.cumulativeValue
        )
    }

    private fun mapDailyDataToDailyChartDataItem(dailyData: DailyData): DataSheetItem {
        return DataSheetItem(
            dailyData.date.format(formatter),
            dailyData.newValue,
            dailyData.cumulativeValue
        )
    }
}
