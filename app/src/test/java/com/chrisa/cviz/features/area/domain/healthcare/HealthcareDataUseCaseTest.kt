/*
 * Copyright 2021 Chris Anderson.
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

package com.chrisa.cviz.features.area.domain.healthcare

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Test

class HealthcareDataUseCaseTest {

    private val healthcareLookupsUseCase: HealthcareLookupsUseCase = mockk()
    private val healthcareRegionUseCase: HealthcareRegionUseCase = mockk()
    private val multiAreaHealthcareDataUseCase: MultiAreaHealthcareDataUseCase = mockk()
    private val singleAreaHealthcareDataUseCase: SingleAreaHealthcareDataUseCase = mockk()
    private val sut = HealthcareDataUseCase(
        healthcareLookupsUseCase,
        healthcareRegionUseCase,
        multiAreaHealthcareDataUseCase,
        singleAreaHealthcareDataUseCase
    )

    @Test
    fun `GIVEN no lookups WHEN healthcareData called THEN area data returned`() {
        val areaCode = "E1"
        val areaType = AreaType.UTLA
        val areaDto = AreaDto(areaLookup.nationCode, areaLookup.nationName, AreaType.NATION)
        every {
            healthcareLookupsUseCase.healthcareLookups(
                areaType,
                areaLookup
            )
        } returns emptyList()
        every {
            healthcareRegionUseCase.healthcareArea(
                areaCode,
                areaType,
                areaLookup
            )
        } returns areaDto
        every {
            singleAreaHealthcareDataUseCase.trustData(
                areaDto.name,
                areaDto.code
            )
        } returns singleTrustData

        val data = sut.healthcareData("E1", AreaType.UTLA, areaLookup)

        assertThat(data).isEqualTo(singleTrustData)
    }

    @Test
    fun `GIVEN areaLookup is null WHEN healthcareData called THEN area data returned`() {
        val areaCode = "E1"
        val areaType = AreaType.UTLA
        val areaDto = AreaDto(areaLookup.nationCode, areaLookup.nationName, AreaType.NATION)
        every {
            healthcareLookupsUseCase.healthcareLookups(
                areaType,
                null
            )
        } returns healthcareLookups
        every { healthcareRegionUseCase.healthcareArea(areaCode, areaType, null) } returns areaDto
        every {
            singleAreaHealthcareDataUseCase.trustData(
                areaDto.name,
                areaDto.code
            )
        } returns singleTrustData

        val data = sut.healthcareData("E1", AreaType.UTLA, null)

        assertThat(data).isEqualTo(singleTrustData)
    }

    @Test
    fun `GIVEN healthcare lookups WHEN healthcareData called THEN lookups data returned`() {
        val areaCode = "E1"
        val areaType = AreaType.UTLA
        val areaDto = AreaDto(areaLookup.nationCode, areaLookup.nationName, AreaType.NATION)
        every {
            healthcareLookupsUseCase.healthcareLookups(
                areaType,
                areaLookup
            )
        } returns healthcareLookups
        every {
            healthcareRegionUseCase.healthcareArea(
                areaCode,
                areaType,
                areaLookup
            )
        } returns areaDto
        every {
            multiAreaHealthcareDataUseCase.trustData(
                areaLookup.utlaName,
                healthcareLookups
            )
        } returns multiTrustData

        val data = sut.healthcareData("E1", AreaType.UTLA, areaLookup)

        assertThat(data).isEqualTo(multiTrustData)
    }

    companion object {
        val healthcareLookups = listOf("T1", "T2", "T3")
        val areaLookup = AreaLookupDto(
            lsoaCode = "E11011",
            lsoaName = "Soho",
            msoaCode = "E11011",
            msoaName = "Soho",
            ltlaCode = "E1101",
            ltlaName = "Westminster",
            utlaCode = "E1101",
            utlaName = "Westminster",
            nhsRegionCode = "E110",
            nhsRegionName = "London",
            nhsTrustCode = "GUYS",
            nhsTrustName = "St Guys",
            regionCode = Constants.ENGLAND_AREA_CODE,
            regionName = "England",
            nationCode = Constants.UK_AREA_CODE,
            nationName = "United Kingdom"
        )
        val dailyData = DailyData(
            newValue = 0,
            cumulativeValue = 0,
            rate = 0.0,
            date = LocalDate.of(2020, 1, 2)
        )
        val singleTrustData = AreaDailyDataCollection(
            name = "London",
            data = listOf(
                AreaDailyDataDto(
                    name = "London",
                    data = listOf(dailyData)
                )
            )
        )
        val multiTrustData = AreaDailyDataCollection(
            name = "London",
            data = listOf(
                AreaDailyDataDto(
                    name = "T1",
                    data = listOf(dailyData)
                ),
                AreaDailyDataDto(
                    name = "T2",
                    data = listOf(dailyData)
                )
            )
        )
    }
}
