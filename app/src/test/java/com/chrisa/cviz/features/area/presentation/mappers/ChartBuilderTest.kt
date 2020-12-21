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
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartDataBuilder
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

    private val combinedChartDataBuilder = mockk<CombinedChartDataBuilder>()
    private val sut = ChartBuilder(formatter, combinedChartDataBuilder)
    private val monthlyData = dailyData(30)
    private val monthlyDataWithRollingAverage = dailyDataWithRollingAverage(30)

    @Before
    fun setup() {
        every {
            combinedChartDataBuilder.combinedChartData(
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
            combinedChartDataBuilder.combinedChartData(
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
    }

    @Test
    fun `WHEN data is empty THEN empty chart data returned`() {
        val data = sut.allChartData(
            allChartLabel,
            latestChartLabel,
            rollingAverageChartLabel,
            emptyList()
        )

        assertThat(data).isEmpty()
    }

    @Test
    fun `WHEN data is not empty THEN bar chart data is built`() {
        val data = sut.allChartData(
            allChartLabel,
            latestChartLabel,
            rollingAverageChartLabel,
            monthlyDataWithRollingAverage
        )

        assertThat(data).isEqualTo(
            listOf(
                allChartData,
                latestChartData
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
        val data = sut.barChartData(
            listOf(
                DailyData(
                    newValue = 100,
                    cumulativeValue = 100,
                    rate = 10.0,
                    date = LocalDate.of(2020, 2, 2)
                )
            )
        )

        assertThat(data).isEqualTo(
            listOf(
                BarChartItem(
                    100.0f,
                    "02-Feb"
                )
            )
        )
    }

    companion object {
        private const val allChartLabel = "All data"
        private const val latestChartLabel = "Latest data"
        private const val rollingAverageChartLabel = "Rolling average data"

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
    }
}
