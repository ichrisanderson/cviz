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

class BarChartDataBuilder @Inject constructor() {
    fun build(
        label: String,
        values: List<BarChartItem>
    ): BarChartData =
        BarChartData(
            label = label,
            values = values
        )
}

class BarChartTabBuilder @Inject constructor() {
    fun build(
        tabTitle: String,
        barChartLabel: String,
        values: List<BarChartItem>
    ): BarChartTab =
        BarChartTab(
            tabTitle,
            BarChartData(
                label = barChartLabel,
                values = values
            )
        )
}
