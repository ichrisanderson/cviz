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

import com.chrisa.covid19.core.data.time.TimeProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Test

class WeeklySummaryBuilderTest {

    private val timeProvider = mockk<TimeProvider>()
    private val sut = WeeklySummaryBuilder(timeProvider)

    @Test
    fun `GIVEN data exists WHEN mapSavedAreaModel called THEN correct calculations are applied`() {

        val date = LocalDate.of(2020, 5, 6)
        every { timeProvider.currentDate() } returns date.plusDays(20)

        val day0 = DailyData(
            date = LocalDate.of(2020, 5, 6),
            newValue = 0,
            cumulativeValue = 0,
            rate = 0.0
        )

        val day3 = day0.copy(
            newValue = 30,
            cumulativeValue = 30,
            rate = 28.0
        )

        val day10 = day0.copy(
            newValue = 30,
            cumulativeValue = 60,
            rate = 28.0
        )

        val day17 = day0.copy(
            newValue = 30,
            cumulativeValue = 100,
            rate = 30.0
        )

        val values = (0 until 20).map {
            when (it) {
                3 -> day3
                10 -> day10
                17 -> day17
                else -> day0
            }
        }

        val result = sut.buildWeeklySummary(values)

        val baseRate = day17.rate / day17.cumulativeValue

        val valueThisWeek = (day17.cumulativeValue - day10.cumulativeValue)
        val valueLastWeek = (day10.cumulativeValue - day3.cumulativeValue)

        val currentRate = valueThisWeek * baseRate
        val previousRate = valueLastWeek * baseRate

        assertThat(result).isEqualTo(
            WeeklySummary(
                weeklyTotal = valueThisWeek,
                changeInTotal = valueThisWeek - valueLastWeek,
                weeklyRate = currentRate,
                changeInRate = currentRate - previousRate
            )
        )
    }
}
