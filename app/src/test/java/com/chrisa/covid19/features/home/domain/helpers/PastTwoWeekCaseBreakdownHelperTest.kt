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

package com.chrisa.covid19.features.home.domain.helpers

import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.WeeklyCaseBreakdownModel
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlin.random.Random
import org.junit.Test

class PastTwoWeekCaseBreakdownHelperTest {

    private val sut =
        PastTwoWeekCaseBreakdownHelper()

    @Test
    fun `GIVEN no data set WHEN pastTwoWeekCaseBreakdown called THEN past 2 weeks of data is empty`() {

        val startDate = LocalDate.of(2020, 1, 1)
        val allCases = mutableListOf<SavedAreaCaseDto>()
        val result = sut.pastTwoWeekCaseBreakdown(
            startDate.plusDays(30),
            allCases
        )

        assertThat(result.weekOneData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = 0,
                totalLabConfirmedCases = 0,
                totalLabConfirmedCasesRate = 0.0
            )
        )
        assertThat(result.weekTwoData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = 0,
                totalLabConfirmedCases = 0,
                totalLabConfirmedCasesRate = 0.0
            )
        )
    }

    @Test
    fun `GIVEN no monthly data in range WHEN pastTwoWeekCaseBreakdown called THEN past 2 weeks of data is empty`() {

        var cumulativeTotal = 0
        var caseNumber = 1
        val random = Random(0)
        val startDate = LocalDate.of(2020, 1, 1)

        val areaCode = "001"
        val areaName = "UK"
        val areaType = "utla"
        val allCases = mutableListOf<SavedAreaCaseDto>()

        for (i in 0 until 30) {

            val dailyLabConfirmedCases = random.nextInt(15)
            cumulativeTotal += dailyLabConfirmedCases

            allCases.add(
                SavedAreaCaseDto(
                    areaCode = areaCode,
                    areaName = areaName,
                    areaType = areaType,
                    date = startDate.plusDays(i.toLong()),
                    dailyLabConfirmedCases = dailyLabConfirmedCases,
                    totalLabConfirmedCases = cumulativeTotal,
                    dailyTotalLabConfirmedCasesRate = cumulativeTotal.toDouble() / caseNumber++
                )
            )
        }

        val result = sut.pastTwoWeekCaseBreakdown(
            startDate.plusDays(60),
            allCases
        )

        assertThat(result.weekOneData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = 0,
                totalLabConfirmedCases = 0,
                totalLabConfirmedCasesRate = 0.0
            )
        )
        assertThat(result.weekTwoData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = 0,
                totalLabConfirmedCases = 0,
                totalLabConfirmedCasesRate = 0.0
            )
        )
    }

    @Test
    fun `GIVEN only a weeks data WHEN pastTwoWeekCaseBreakdown called THEN first week data is empty`() {

        var cumulativeTotal = 0
        var caseNumber = 1
        val random = Random(0)
        val startDate = LocalDate.of(2020, 1, 1)

        val areaCode = "001"
        val areaName = "UK"
        val areaType = "utla"
        val allCases = mutableListOf<SavedAreaCaseDto>()

        for (i in 0 until 7) {

            val dailyLabConfirmedCases = random.nextInt(15)
            cumulativeTotal += dailyLabConfirmedCases

            allCases.add(
                SavedAreaCaseDto(
                    areaCode = areaCode,
                    areaName = areaName,
                    areaType = areaType,
                    date = startDate.plusDays(i.toLong()),
                    dailyLabConfirmedCases = dailyLabConfirmedCases,
                    totalLabConfirmedCases = cumulativeTotal,
                    dailyTotalLabConfirmedCasesRate = cumulativeTotal.toDouble() / caseNumber++
                )
            )
        }

        val result = sut.pastTwoWeekCaseBreakdown(
            startDate.plusDays(7),
            allCases
        )

        val lastFortnightCases = allCases.takeLast(14)
        val thisWeekCases = lastFortnightCases.takeLast(7)

        assertThat(result.weekOneData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = 0,
                totalLabConfirmedCases = 0,
                totalLabConfirmedCasesRate = 0.0
            )
        )
        assertThat(result.weekTwoData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = thisWeekCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = thisWeekCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = thisWeekCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            )
        )
    }

    @Test
    fun `GIVEN weekly data intersects WHEN pastTwoWeekCaseBreakdown called THEN intersected data is returned`() {

        var cumulativeTotal = 0
        var caseNumber = 1
        val random = Random(0)
        val startDate = LocalDate.of(2020, 1, 1)

        val areaCode = "001"
        val areaName = "UK"
        val areaType = "utla"
        val allCases = mutableListOf<SavedAreaCaseDto>()

        for (i in 0 until 25) {

            val dailyLabConfirmedCases = random.nextInt(15)
            cumulativeTotal += dailyLabConfirmedCases

            allCases.add(
                SavedAreaCaseDto(
                    areaCode = areaCode,
                    areaName = areaName,
                    areaType = areaType,
                    date = startDate.plusDays(i.toLong()),
                    dailyLabConfirmedCases = dailyLabConfirmedCases,
                    totalLabConfirmedCases = cumulativeTotal,
                    dailyTotalLabConfirmedCasesRate = cumulativeTotal.toDouble() / caseNumber++
                )
            )
        }

        val result = sut.pastTwoWeekCaseBreakdown(
            startDate.plusDays(28),
            allCases
        )

        val previousWeekCases = allCases.takeLast(11).take(7)
        val thisWeekCases = allCases.takeLast(4)

        assertThat(result.weekOneData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = previousWeekCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = previousWeekCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = previousWeekCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            )
        )
        assertThat(result.weekTwoData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = thisWeekCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = thisWeekCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = thisWeekCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            )
        )
    }

    @Test
    fun `GIVEN monthly data set WHEN pastTwoWeekCaseBreakdown called THEN past 2 weeks of data is created`() {

        var cumulativeTotal = 0
        var caseNumber = 1
        val random = Random(0)
        val startDate = LocalDate.of(2020, 1, 1)

        val areaCode = "001"
        val areaName = "UK"
        val areaType = "utla"
        val allCases = mutableListOf<SavedAreaCaseDto>()

        for (i in 0 until 30) {

            val dailyLabConfirmedCases = random.nextInt(15)
            cumulativeTotal += dailyLabConfirmedCases

            allCases.add(
                SavedAreaCaseDto(
                    areaCode = areaCode,
                    areaName = areaName,
                    areaType = areaType,
                    date = startDate.plusDays(i.toLong()),
                    dailyLabConfirmedCases = dailyLabConfirmedCases,
                    totalLabConfirmedCases = cumulativeTotal,
                    dailyTotalLabConfirmedCasesRate = cumulativeTotal.toDouble() / caseNumber++
                )
            )
        }

        val result = sut.pastTwoWeekCaseBreakdown(
            startDate.plusDays(30),
            allCases
        )

        val lastFortnightCases = allCases.takeLast(14)
        val previousWeekCases = lastFortnightCases.take(7)
        val thisWeekCases = lastFortnightCases.takeLast(7)

        assertThat(result.weekOneData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = previousWeekCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = previousWeekCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = previousWeekCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            )
        )
        assertThat(result.weekTwoData).isEqualTo(
            WeeklyCaseBreakdownModel(
                casesInWeek = thisWeekCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = thisWeekCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = thisWeekCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            )
        )
    }
}
