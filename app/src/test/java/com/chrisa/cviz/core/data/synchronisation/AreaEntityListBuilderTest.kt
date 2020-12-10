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

import com.chrisa.cviz.core.data.db.AreaSummaryEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.Page
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import org.junit.Test

class AreaEntityListBuilderTest {

    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private val syncDate = syncTime.toLocalDate()
    private val lastDate = syncDate.minusDays(3)
    private val week1Data = AreaDataModel(
        areaCode = "LDN",
        areaName = "London",
        areaType = AreaType.REGION.value,
        date = lastDate,
        cumulativeCases = 100,
        newCases = 10,
        infectionRate = 100.0,
        newDeathsByPublishedDate = 15,
        cumulativeDeathsByPublishedDate = 20,
        cumulativeDeathsByPublishedDateRate = 30.0,
        newDeathsByDeathDate = 40,
        cumulativeDeathsByDeathDate = 50,
        cumulativeDeathsByDeathDateRate = 60.0,
        newOnsDeathsByRegistrationDate = 10,
        cumulativeOnsDeathsByRegistrationDate = 53,
        cumulativeOnsDeathsByRegistrationDateRate = 62.0
    )
    private val week2Data = week1Data.copy(
        date = week1Data.date.minusDays(7),
        cumulativeCases = 85,
        newCases = 8,
        infectionRate = 90.0
    )
    private val week3Data = week2Data.copy(
        date = week2Data.date.minusDays(7),
        cumulativeCases = 70,
        newCases = 7,
        infectionRate = 82.0
    )
    private val week4Data = week3Data.copy(
        date = week3Data.date.minusDays(7),
        cumulativeCases = 64,
        newCases = 8,
        infectionRate = 85.0
    )
    private val baseInfectionRate = week1Data.infectionRate!! / week1Data.cumulativeCases!!
    private val newCasesWeek1 = week1Data.cumulativeCases!! - week2Data.cumulativeCases!!
    private val newCasesWeek2 = week2Data.cumulativeCases!! - week3Data.cumulativeCases!!
    private val newCasesWeek3 = week3Data.cumulativeCases!! - week4Data.cumulativeCases!!
    private val sut = AreaEntityListBuilder()

    @Test
    fun `WHEN monthly data provided THEN area entity list returned`() {

        val monthlyData = MonthlyData(
            lastDate = lastDate,
            areaType = AreaType.LTLA,
            week1 = Page(length = 1, maxPageLimit = null, data = listOf(week1Data)),
            week2 = Page(length = 1, maxPageLimit = null, data = listOf(week2Data)),
            week3 = Page(length = 1, maxPageLimit = null, data = listOf(week3Data)),
            week4 = Page(length = 1, maxPageLimit = null, data = listOf(week4Data))
        )

        val result = sut.build(monthlyData)

        assertThat(result).isEqualTo(
            listOf(
                AreaSummaryEntity(
                    areaCode = week1Data.areaCode,
                    areaType = AreaType.from(week1Data.areaType)!!,
                    areaName = week1Data.areaName,
                    date = week1Data.date,
                    baseInfectionRate = baseInfectionRate,
                    cumulativeCasesWeek1 = week1Data.cumulativeCases!!,
                    cumulativeCaseInfectionRateWeek1 = week1Data.infectionRate!!,
                    newCaseInfectionRateWeek1 = baseInfectionRate * newCasesWeek1,
                    newCasesWeek1 = newCasesWeek1,
                    cumulativeCasesWeek2 = week2Data.cumulativeCases!!,
                    cumulativeCaseInfectionRateWeek2 = week2Data.infectionRate!!,
                    newCaseInfectionRateWeek2 = baseInfectionRate * newCasesWeek2,
                    newCasesWeek2 = newCasesWeek2,
                    cumulativeCasesWeek3 = week3Data.cumulativeCases!!,
                    cumulativeCaseInfectionRateWeek3 = week3Data.infectionRate!!,
                    newCaseInfectionRateWeek3 = baseInfectionRate * newCasesWeek3,
                    newCasesWeek3 = newCasesWeek3,
                    cumulativeCasesWeek4 = week4Data.cumulativeCases!!,
                    cumulativeCaseInfectionRateWeek4 = week4Data.infectionRate!!
                )
            )
        )
    }
}
