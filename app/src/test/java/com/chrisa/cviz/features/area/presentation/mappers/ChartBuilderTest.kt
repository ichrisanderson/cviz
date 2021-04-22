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
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetItem
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetTab
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ChartBuilderTest {

    private val formatter = DateTimeFormatter
        .ofPattern("dd-MMM")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    private val combinedChartDataBuilder = mockk<CombinedChartDataBuilder>()
    private val barChartDataBuilder = mockk<BarChartDataBuilder>()
    private val dataSheetTabBuilder = mockk<DataSheetTabBuilder>()
    private val monthlyData = dailyData(10)
    private val monthlyDataWithRollingAverage = dailyDataWithRollingAverage(30)
    private val sut = ChartBuilder(
        formatter,
        combinedChartDataBuilder,
        barChartDataBuilder,
        dataSheetTabBuilder
    )

    @Before
    fun setup() {
        every {
            combinedChartDataBuilder.build(
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
            combinedChartDataBuilder.build(
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
            barChartDataBuilder.build(allChartLabel,
                monthlyData.map {
                    BarChartItem(
                        value = it.newValue.toFloat(),
                        label = it.date.format(formatter)
                    )
                })
        } returns allBarChartData

        every {
            barChartDataBuilder.build(
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
                monthlyDataWithRollingAverage.sortedByDescending { it.date }.map {
                    DataSheetItem(
                        it.newValue,
                        it.cumulativeValue,
                        it.date.format(formatter)
                    )
                }
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
            monthlyData
        )

        assertThat(data).isEqualTo(
            listOf(
                allBarChartData,
                latestBarChartData
            )
        )
    }

    companion object {
        private const val allChartLabel = "All data"
        private const val latestChartLabel = "Latest data"
        private const val rollingAverageChartLabel = "Rolling average data"
        private const val dataTabLabel = "Data"

        private val allChartData = CombinedChartData(
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

        private val latestChartData = CombinedChartData(
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

        val allBarChartData = BarChartData(
            label = allChartLabel,
            values = listOf(
                BarChartItem(
                    value = 100.0f,
                    label = "$allChartLabel bar chart value"
                )
            )
        )

        val latestBarChartData = BarChartData(
            label = latestChartLabel,
            values = listOf(
                BarChartItem(
                    value = 100.0f,
                    label = "$latestChartLabel bar chart value"
                )
            )
        )
    }

    private val dataSheetTab = DataSheetTab(
        title = dataTabLabel,
        data = listOf(
            DataSheetItem(
                value = 100,
                cumulativeValue = 1000,
                label = "12-APR"
            )
        )
    )
}
