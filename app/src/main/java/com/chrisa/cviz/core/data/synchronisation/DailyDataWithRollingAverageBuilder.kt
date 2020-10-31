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

package com.chrisa.cviz.core.data.synchronisation

import javax.inject.Inject

class DailyDataWithRollingAverageBuilder @Inject constructor(
    private val rollingAverageHelper: RollingAverageHelper
) {
    fun buildDailyDataWithRollingAverage(dailyData: List<DailyData>): List<DailyDataWithRollingAverage> {
        val values = dailyData.map { it.newValue }
        return dailyData.mapIndexed { index, data ->
            DailyDataWithRollingAverage(
                newValue = data.newValue,
                cumulativeValue = data.cumulativeValue,
                rate = data.rate,
                rollingAverage = rollingAverageHelper.average(index, values),
                date = data.date
            )
        }
    }
}
