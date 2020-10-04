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
import java.time.LocalDate
import javax.inject.Inject

class AreaSummaryMapper @Inject constructor(
    val timeProvider: TimeProvider
) {

    fun mapAreaDataToAreaSummary(
        areaCode: String,
        areaName: String,
        areaType: String,
        allCases: List<AreaData>
    ): AreaSummary {

        val offset = findOffset(allCases)

        val lastCase = allCases.getOrNull(allCases.size - offset)
        val prevCase = allCases.getOrNull(allCases.size - (offset + 7))
        val prevCase1 = allCases.getOrNull(allCases.size - (offset + 14))

        val lastTotalLabConfirmedCases = lastCase?.cumulativeCases ?: 0
        val prevTotalLabConfirmedCases = prevCase?.cumulativeCases ?: 0
        val prev1TotalLabConfirmedCases = prevCase1?.cumulativeCases ?: 0

        val baseRate = lastCase!!.infectionRate / lastCase.cumulativeCases
        val casesThisWeek = (lastTotalLabConfirmedCases - prevTotalLabConfirmedCases)
        val casesLastWeek = (prevTotalLabConfirmedCases - prev1TotalLabConfirmedCases)

        val currentInfectionRate = casesThisWeek * baseRate
        val previousInfectionRate = casesLastWeek * baseRate

        return AreaSummary(
            areaCode = areaCode,
            areaName = areaName,
            areaType = areaType,
            currentNewCases = casesThisWeek,
            changeInCases = casesThisWeek - casesLastWeek,
            currentInfectionRate = currentInfectionRate,
            changeInInfectionRate = currentInfectionRate - previousInfectionRate
        )
    }

    private fun findOffset(allCases: List<AreaData>): Int {
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

data class AreaData(
    val newCases: Int,
    val cumulativeCases: Int,
    val infectionRate: Double,
    val date: LocalDate
)

data class AreaSummary(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val currentNewCases: Int,
    val changeInCases: Int,
    val currentInfectionRate: Double,
    val changeInInfectionRate: Double
)
