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

import javax.inject.Inject

class CombinedChartTabBuilder @Inject constructor() {

    fun build(
        tabTitle: String,
        barChartLabel: String,
        barChartValues: List<BarChartItem>,
        lineChartLabel: String,
        lineChartValues: List<LineChartItem>
    ): CombinedChartTab {
        return CombinedChartTab(
            title = tabTitle,
            barChartData = BarChartData(
                label = barChartLabel,
                values = barChartValues
            ),
            lineChartData = LineChartData(
                label = lineChartLabel,
                values = lineChartValues
            )
        )
    }
}
