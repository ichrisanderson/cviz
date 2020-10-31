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

import com.chrisa.cviz.core.data.time.TimeProvider
import javax.inject.Inject

class WeeklySummaryBuilder @Inject constructor(
    val timeProvider: TimeProvider
) {

    fun buildWeeklySummary(dailyData: List<DailyData>): WeeklySummary {

        val offset = findOffset(dailyData)

        val lastData = dailyData.lastOrNull()

        val week1 = dailyData.getOrNull(dailyData.size - offset)
        val week2 = dailyData.getOrNull(dailyData.size - (offset + 7))
        val week3 = dailyData.getOrNull(dailyData.size - (offset + 14))

        val week1Total = week1?.cumulativeValue ?: 0
        val week2Total = week2?.cumulativeValue ?: 0
        val week3Total = week3?.cumulativeValue ?: 0
        val week1Rate = week1?.rate

        val baseRate = week1Rate?.let { week1Rate / week1Total } ?: 0.0

        val totalThisWeek = (week1Total - week2Total)
        val totalLastWeek = (week2Total - week3Total)

        val rateThisWeek = totalThisWeek * baseRate
        val rateLastWeek = totalLastWeek * baseRate

        return WeeklySummary(
            lastDate = lastData?.date,
            currentTotal = lastData?.cumulativeValue ?: 0,
            dailyTotal = lastData?.newValue ?: 0,
            weeklyTotal = totalThisWeek,
            changeInTotal = totalThisWeek - totalLastWeek,
            weeklyRate = rateThisWeek,
            changeInRate = rateThisWeek - rateLastWeek
        )
    }

    private fun findOffset(allCases: List<DailyData>): Int {
        var offset = 3
        val offsetDate = timeProvider.currentDate().minusDays(offset.toLong())
        for (i in 0..offset) {
            val case = allCases.getOrNull(allCases.size - i)
            if (case?.date == offsetDate) {
                offset = i
                break
            }
        }
        return offset
    }
}
