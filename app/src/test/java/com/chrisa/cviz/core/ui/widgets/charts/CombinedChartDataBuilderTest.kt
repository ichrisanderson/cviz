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

package com.chrisa.cviz.core.ui.widgets.charts

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CombinedChartDataBuilderTest {

    private val combinedChartBuilder = CombinedChartDataBuilder()

    @Test
    fun `WHEN chart data provided THEN bar and line charts are combined`() {
        val chartData = combinedChartBuilder.build(
            barChartLabel,
            listOf(barChartItem),
            lineChartLabel,
            listOf(lineChartItem)
        )

        assertThat(chartData).isEqualTo(
            CombinedChartData(
                title = barChartLabel,
                barChartData = BarChartData(
                    label = barChartLabel,
                    values = listOf(barChartItem)
                ),
                lineChartData = LineChartData(
                    label = lineChartLabel,
                    values = listOf(lineChartItem)
                )
            )
        )
    }

    companion object {

        private const val barChartLabel = "bar chart"
        private const val lineChartLabel = "line chart"

        private val barChartItem =
            BarChartItem(
                value = 10.0f,
                label = "Foo"
            )
        private val lineChartItem =
            LineChartItem(
                value = 10.0f,
                label = "Foo"
            )
    }
}
