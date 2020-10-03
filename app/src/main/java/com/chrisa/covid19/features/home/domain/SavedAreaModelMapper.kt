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
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import javax.inject.Inject

class SavedAreaModelMapper @Inject() constructor() {

    fun mapSavedAreaModel(
        areaCode: String,
        areaName: String,
        allCases: List<SavedAreaCaseDto>
    ): SavedAreaModel {

        val offset = 3

        val lastCase = allCases.getOrNull(allCases.size - offset)
        val prevCase = allCases.getOrNull(allCases.size - (offset + 7))
        val prevCase1 = allCases.getOrNull(allCases.size - (offset + 14))

        val lastTotalLabConfirmedCases = lastCase?.cumulativeCases ?: 0
        val prevTotalLabConfirmedCases = prevCase?.cumulativeCases ?: 0
        val prev1TotalLabConfirmedCases = prevCase1?.cumulativeCases ?: 0

        val baseRate = lastCase!!.infectionRate / lastCase!!.cumulativeCases
        val casesThisWeek = (lastTotalLabConfirmedCases - prevTotalLabConfirmedCases)
        val casesLastWeek = (prevTotalLabConfirmedCases - prev1TotalLabConfirmedCases)

        val currentInfectionRate = casesThisWeek * baseRate
        val previousInfectionRate = casesLastWeek * baseRate

        return SavedAreaModel(
            areaCode = areaCode,
            areaName = areaName,
            areaType = allCases.first().areaType,
            currentNewCases = casesThisWeek,
            changeInCases = casesThisWeek - casesLastWeek,
            currentInfectionRate = currentInfectionRate,
            changeInInfectionRate = currentInfectionRate - previousInfectionRate
        )
    }
}
