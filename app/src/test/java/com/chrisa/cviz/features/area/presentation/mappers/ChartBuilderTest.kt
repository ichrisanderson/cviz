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
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.BarChartTab
import com.chrisa.cviz.core.ui.widgets.charts.BarChartTabBuilder
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTab
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTabBuilder
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetColumnHeaders
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetItem
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetTab
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.junit.Before
import org.junit.Test

class ChartBuilderTest {

    private val formatter = DateTimeFormatter
        .ofPattern("dd-MMM")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    private val combinedChartTabBuilder = mockk<CombinedChartTabBuilder>()
    private val barChartTabBuilder = mockk<BarChartTabBuilder>()
    private val dataSheetTabBuilder = mockk<DataSheetTabBuilder>()
    private val monthlyData = dailyData(10)
    private val monthlyDataWithRollingAverage = dailyDataWithRollingAverage(30)
    private val sut = ChartBuilder(
        formatter,
        combinedChartTabBuilder,
        barChartTabBuilder,
        dataSheetTabBuilder
    )

    @Before
    fun setup() {
        every {
            combinedChartTabBuilder.build(
                allChartLabel,
                allChartLabel,
                monthlyDataWithRollingAverage.map {
                    BarChartItem(
                        value = it.newValue.toFloat(),
                        label = it.date.format(formatter)
                    )
                },
                rollingAverageChartLabel,
                monthlyDataWithRollingAverage.map {
                    LineChartItem(
                        value = it.rollingAverage.toFloat(),
                        label = it.date.format(formatter)
                    )
                }

            )
        } returns allChartData

        every {
            combinedChartTabBuilder.build(
                latestChartLabel,
                latestChartLabel,
                monthlyDataWithRollingAverage.takeLast(14).map {
                    BarChartItem(
                        value = it.newValue.toFloat(),
                        label = it.date.format(formatter)
                    )
                },
                rollingAverageChartLabel,
                monthlyDataWithRollingAverage.takeLast(14).map {
                    LineChartItem(
                        value = it.rollingAverage.toFloat(),
                        label = it.date.format(formatter)
                    )
                }

            )
        } returns latestChartData

        every {
            barChartTabBuilder.build(
                allChartLabel,
                allChartLabel,
                monthlyData.map {
                    BarChartItem(
                        value = it.newValue.toFloat(),
                        label = it.date.format(formatter)
                    )
                })
        } returns allBarChartData

        every {
            barChartTabBuilder.build(
                latestChartLabel,
                latestChartLabel,
                monthlyData.takeLast(14).map {
                    BarChartItem(
                        value = it.newValue.toFloat(),
                        label = it.date.format(formatter)
                    )
                })
        } returns latestBarChartData

        every {
            dataSheetTabBuilder.build(
                dataTabLabel,
                columnHeaders,
                any()
            )
        } returns dataSheetTab
    }

    @Test
    fun `WHEN data is empty THEN empty chart data returned`() {
        val data = sut.allCombinedChartData(
            allChartLabel,
            latestChartLabel,
            rollingAverageChartLabel,
            dataTabLabel,
            columnHeaders,
            emptyList()
        )

        assertThat(data).isEmpty()
    }

    @Test
    fun `WHEN data is not empty THEN bar chart data is built`() {
        val data = sut.allCombinedChartData(
            allChartLabel,
            latestChartLabel,
            rollingAverageChartLabel,
            dataTabLabel,
            columnHeaders,
            monthlyDataWithRollingAverage
        )

        assertThat(data).isEqualTo(
            listOf(
                allChartData,
                latestChartData,
                dataSheetTab
            )
        )
    }

    private fun dailyData(total: Int): List<DailyData> {
        var cumulativeValue = 0
        val date = LocalDate.of(2020, 1, 1)
        return (1..total).map {
            cumulativeValue += it
            DailyData(
                newValue = it,
                cumulativeValue = cumulativeValue,
                rate = 10.0 + it.toDouble(),
                date = date.plusDays(it.toLong())
            )
        }
    }

    private fun dailyDataWithRollingAverage(total: Int): List<DailyDataWithRollingAverage> {
        var cumulativeValue = 0
        val date = LocalDate.of(2020, 1, 1)
        return (1..total).map {
            cumulativeValue += it
            DailyDataWithRollingAverage(
                newValue = it,
                cumulativeValue = cumulativeValue,
                rollingAverage = cumulativeValue / it.toDouble(),
                rate = 10.0 + it.toDouble(),
                date = date.plusDays(it.toLong())
            )
        }
    }

    @Test
    fun `WHEN data is not empty THEN chart data is built`() {
        val data = sut.allBarChartData(
            allChartLabel,
            latestChartLabel,
            dataTabLabel,
            columnHeaders,
            monthlyData
        )

        assertThat(data).isEqualTo(
            listOf(
                allBarChartData,
                latestBarChartData,
                dataSheetTab
            )
        )
    }

    companion object {
        private const val allChartLabel = "All data"
        private const val latestChartLabel = "Latest data"
        private const val rollingAverageChartLabel = "Rolling average data"
        private const val dataTabLabel = "Data"

        private const val dateColumnHeader = "date"
        private const val newValueColumnHeader = "new value"
        private const val cumulativeValueColumnHeader = "total value"

        private val columnHeaders = DataSheetColumnHeaders(
            labelHeader = dateColumnHeader,
            valueHeader = newValueColumnHeader,
            cumulativeValueHeader = cumulativeValueColumnHeader
        )

        private val allChartData = CombinedChartTab(
            title = allChartLabel,
            barChartData = BarChartData(
                label = allChartLabel,
                values = listOf(
                    BarChartItem(
                        value = 2.0f,
                        label = "$allChartLabel bar chart value"
                    )
                )
            ),
            lineChartData = LineChartData(
                label = rollingAverageChartLabel,
                values = listOf(
                    LineChartItem(
                        value = 1.0f,
                        label = "$allChartLabel line chart value"
                    )
                )
            )
        )

        private val latestChartData = CombinedChartTab(
            title = latestChartLabel,
            barChartData = BarChartData(
                label = latestChartLabel,
                values = listOf(
                    BarChartItem(
                        value = 4.0f,
                        label = "$latestChartLabel bar chart value"
                    )
                )
            ),
            lineChartData = LineChartData(
                label = rollingAverageChartLabel,
                values = listOf(
                    LineChartItem(
                        value = 3.0f,
                        label = "$latestChartLabel line chart value"
                    )
                )
            )
        )

        val allBarChartData = BarChartTab(
            title = allChartLabel,
            barChartData = BarChartData(
                label = allChartLabel,
                values = listOf(
                    BarChartItem(
                        value = 100.0f,
                        label = "$allChartLabel bar chart value"
                    )
                )
            )
        )

        val latestBarChartData = BarChartTab(
            title = latestChartLabel,
            barChartData = BarChartData(
                label = latestChartLabel,
                values = listOf(
                    BarChartItem(
                        value = 100.0f,
                        label = "$latestChartLabel bar chart value"
                    )
                )
            )
        )
    }

    private val dataSheetTab = DataSheetTab(
        title = dataTabLabel,
        columnHeaders = columnHeaders,
        data = listOf(
            DataSheetItem(
                label = "12-APR",
                value = 100,
                cumulativeValue = 1000
            )
        )
    )
}
