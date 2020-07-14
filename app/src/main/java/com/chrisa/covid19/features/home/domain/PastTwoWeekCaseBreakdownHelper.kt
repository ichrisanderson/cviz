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

package com.chrisa.covid19.features.home.domain

import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.PastTwoWeekCaseBreakdownModel
import com.chrisa.covid19.features.home.domain.models.WeeklyCaseBreakdownModel
import java.time.LocalDate
import javax.inject.Inject

class PastTwoWeekCaseBreakdownHelper @Inject constructor() {

    fun pastTwoWeekCaseBreakdown(
        dateNow: LocalDate,
        allCases: List<SavedAreaCaseDto>
    ): PastTwoWeekCaseBreakdownModel {

        val dateMinusOneWeek = dateNow.minusWeeks(1)
        val dateMinusTwoWeeks = dateNow.minusWeeks(2)

        val weekMinusOneCases = allCases
            .filter { it.date >= dateMinusOneWeek }

        val weekMinusTwoCases = allCases
            .filter { it.date >= dateMinusTwoWeeks && it.date < dateMinusOneWeek }

        return PastTwoWeekCaseBreakdownModel(
            weekOneData = WeeklyCaseBreakdownModel(
                casesInWeek = weekMinusTwoCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = weekMinusTwoCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = weekMinusTwoCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            ),
            weekTwoData = WeeklyCaseBreakdownModel(
                casesInWeek = weekMinusOneCases.sumBy { it.dailyLabConfirmedCases },
                totalLabConfirmedCases = weekMinusOneCases.lastOrNull()?.totalLabConfirmedCases
                    ?: 0,
                totalLabConfirmedCasesRate = weekMinusOneCases.lastOrNull()?.dailyTotalLabConfirmedCasesRate
                    ?: 0.0
            )
        )
    }
}
