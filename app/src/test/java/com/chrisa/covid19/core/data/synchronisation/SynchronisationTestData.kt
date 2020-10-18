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

package com.chrisa.covid19.core.data.synchronisation

import java.time.LocalDate
import java.time.LocalDateTime

object SynchronisationTestData {

    private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

    val emptyWeeklySummary =
        WeeklySummary(
            lastDate = null,
            currentTotal = 0,
            dailyTotal = 0,
            weeklyTotal = 0,
            changeInTotal = 0,
            weeklyRate = 0.0,
            changeInRate = 0.0
        )

    val bigWeeklySummary =
        WeeklySummary(
            lastDate = syncDate.toLocalDate(),
            currentTotal = 12220,
            dailyTotal = 320,
            weeklyTotal = 1000,
            changeInTotal = 10,
            weeklyRate = 100.0,
            changeInRate = 20.0
        )

    val smallWeeklySummary =
        WeeklySummary(
            lastDate = syncDate.toLocalDate(),
            currentTotal = 30,
            dailyTotal = 3,
            weeklyTotal = 10,
            changeInTotal = 1,
            weeklyRate = 4.0,
            changeInRate = 2.0
        )

    fun dailyData(): List<DailyData> {
        var cumulativeCases = 0
        return (1 until 100).map {
            cumulativeCases += it
            DailyData(
                newValue = it,
                cumulativeValue = cumulativeCases,
                date = LocalDate.ofEpochDay(it.toLong()),
                rate = 0.0
            )
        }
    }
}
