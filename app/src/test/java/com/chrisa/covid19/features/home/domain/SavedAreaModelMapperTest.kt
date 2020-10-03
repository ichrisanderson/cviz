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

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class SavedAreaModelMapperTest {

    val sut = SavedAreaModelMapper()

    @Test
    fun `GIVEN data exists WHEN mapSavedAreaModel called THEN correct calculations are applied`() {

        val day0 = SavedAreaCaseDto(
            areaCode = "A002",
            areaName = "Woking",
            areaType = AreaType.LTLA.value,
            date = LocalDate.of(2020, 5, 6),
            newCases = 0,
            cumulativeCases = 0,
            infectionRate = 0.0
        )

        val day3 = day0.copy(
            newCases = 30,
            cumulativeCases = 30,
            infectionRate = 28.0
        )

        val day10 = day0.copy(
            newCases = 30,
            cumulativeCases = 60,
            infectionRate = 28.0
        )

        val day17 = day0.copy(
            newCases = 30,
            cumulativeCases = 100,
            infectionRate = 30.0
        )

        val cases = (0 until 20).map {
            when (it) {
                3 -> day3
                10 -> day10
                17 -> day17
                else -> day0
            }
        }

        val result = sut.mapSavedAreaModel(
            day0.areaCode,
            day0.areaName,
            cases
        )

        val baseRate = day17.infectionRate / day17.cumulativeCases

        val casesThisWeek = (day17.cumulativeCases - day10.cumulativeCases)
        val casesLastWeek = (day10.cumulativeCases - day3.cumulativeCases)

        val currentInfectionRate = casesThisWeek * baseRate
        val previousInfectionRate = casesLastWeek * baseRate

        assertThat(result).isEqualTo(
            SavedAreaModel(
                areaCode = day0.areaCode,
                areaName = day0.areaName,
                areaType = day0.areaType,
                currentNewCases = casesThisWeek,
                changeInCases = casesThisWeek - casesLastWeek,
                currentInfectionRate = currentInfectionRate,
                changeInInfectionRate = currentInfectionRate - previousInfectionRate
            )
        )
    }
}
