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

import java.time.LocalDate
import java.time.LocalDateTime

object SynchronisationTestData {

    private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

    fun weeklySummary(
        currentTotal: Int = 12220,
        dailyTotal: Int = 320,
        weeklyTotal: Int = 1000,
        changeInTotal: Int = 10,
        weeklyRate: Double = 100.0,
        changeInRate: Double = 20.0
    ) =
        WeeklySummary(
            lastDate = syncDate.toLocalDate(),
            currentTotal = currentTotal,
            dailyTotal = dailyTotal,
            weeklyTotal = weeklyTotal,
            changeInTotal = changeInTotal,
            weeklyRate = weeklyRate,
            changeInRate = changeInRate
        )

    fun dailyData(start: Int = 1, end: Int = 100): List<DailyData> {
        var cumulativeCases = 0
        return (start until end).map {
            cumulativeCases += it
            DailyData(
                newValue = it,
                cumulativeValue = cumulativeCases,
                date = LocalDate.ofEpochDay(it.toLong()),
                rate = 0.0
            )
        }
    }

    fun dailyDataWithRollingAverage(
        start: Int = 1,
        end: Int = 100
    ): List<DailyDataWithRollingAverage> {
        var cumulativeCases = 0
        return (1 until 100).map {
            DailyDataWithRollingAverage(
                newValue = it,
                cumulativeValue = cumulativeCases,
                rollingAverage = 1.0,
                rate = 100.0,
                date = LocalDate.of(2020, 1, 1)
            )
        }
    }
}
