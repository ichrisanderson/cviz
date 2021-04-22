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
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTab
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetColumnHeaders
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import com.chrisa.cviz.features.area.domain.models.SoaData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test

class SoaChartBuilderTest {

    private val context: Context = mockk()
    private val dailyDataWithRollingAverageBuilder: DailyDataWithRollingAverageBuilder = mockk()
    private val soaDailyDataMapper: SoaDailyDataMapper = mockk()
    private val chartBuilder: ChartBuilder = mockk()

    private val sut = SoaChartBuilder(
        context,
        dailyDataWithRollingAverageBuilder,
        soaDailyDataMapper,
        chartBuilder
    )

    @Before
    fun setup() {
        every { context.getString(R.string.all_cases_chart_label) } returns allCasesLabel
        every { context.getString(R.string.latest_cases_chart_label) } returns latestCasesLabel
        every { context.getString(R.string.rolling_average_chart_label) } returns rollingAverageLabel
        every { context.getString(R.string.data_tab_label) } returns dataTabLabel
        every { context.getString(R.string.date_column_header) } returns dateColumnHeader
        every { context.getString(R.string.new_cases_column_header) } returns newValueColumnHeader
        every { context.getString(R.string.total_cases_column_header) } returns totalValueColumnHeader
    }

    @Test
    fun `WHEN caseChartData THEN chart data built`() {
        val chartData = combinedChartData("cases")
        val dailyCaseData = SynchronisationTestData.dailyData(1, 100)
        val casesWithRollingAverage = SynchronisationTestData.dailyDataWithRollingAverage(1, 100)
        every {
            soaDailyDataMapper.mapToDailyData(soaData)
        } returns
            dailyCaseData
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(dailyCaseData)
        } returns
            casesWithRollingAverage
        every {
            chartBuilder.allCombinedChartData(
                allCasesLabel,
                latestCasesLabel,
                rollingAverageLabel,
                dataTabLabel,
                columnHeaders,
                casesWithRollingAverage
            )
        } returns
            listOf(chartData)

        val caseChartData = sut.caseChartData(soaData)

        assertThat(caseChartData).isEqualTo(listOf(chartData))
    }

    companion object {

        private const val allCasesLabel = "All cases"
        private const val latestCasesLabel = "Latest cases"
        private const val rollingAverageLabel = "Rolling average"
        private const val dataTabLabel = "Data"
        private const val barChartLabel = "bar chart"
        private const val lineChartLabel = "line chart"

        private const val dateColumnHeader = "date"
        private const val newValueColumnHeader = "new value"
        private const val totalValueColumnHeader = "total value"

        private val columnHeaders = DataSheetColumnHeaders(
            labelHeader = dateColumnHeader,
            valueHeader = newValueColumnHeader,
            cumulativeValueHeader = totalValueColumnHeader
        )

        private val soaData = listOf(
            SoaData(
                date = LocalDate.of(2020, 1, 1),
                rollingSum = 11,
                rollingRate = 12.0
            )
        )

        private fun combinedChartData(labelPrefix: String) =
            CombinedChartTab(
                title = barChartLabel,
                barChartData = BarChartData(
                    label = barChartLabel,
                    values = listOf(
                        BarChartItem(
                            value = 10.0f,
                            label = "${labelPrefix}_BarChartItem"
                        )
                    )
                ),
                lineChartData = LineChartData(
                    label = lineChartLabel,
                    values = listOf(
                        LineChartItem(
                            value = 10.0f,
                            label = "${labelPrefix}_LineChartItem"
                        )
                    )
                )
            )
    }
}
