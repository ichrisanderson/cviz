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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.AreaCasesDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test

class AreaCasesUseCaseTest {

    private val areaLookupUseCase: AreaLookupUseCase = mockk()
    private val areaCasesDataSource: AreaCasesDataSource = mockk()

    private val sut = AreaCasesUseCase(areaLookupUseCase, areaCasesDataSource)

    @Before
    fun init() {
        every { areaLookupUseCase.areaLookup(any(), any()) } returns areaLookupDto
        every { areaCasesDataSource.cases(any()) } returns emptyList()
    }

    @Test
    fun `WHEN areaLookup not found THEN uk cases returned`() {
        every { areaLookupUseCase.areaLookup(any(), any()) } returns null
        every { areaCasesDataSource.cases(Constants.UK_AREA_CODE) } returns dailyData

        val cases = sut.cases("E1", AreaType.UTLA)

        assertThat(cases).isEqualTo(
            AreaDailyDataDto("United Kingdom", dailyData)
        )
    }

    @Test
    fun `WHEN nation has cases THEN nation cases returned`() {
        every { areaCasesDataSource.cases(areaLookupDto.nationCode) } returns dailyData

        val cases = sut.cases("E1", AreaType.UTLA)

        assertThat(cases).isEqualTo(
            AreaDailyDataDto(areaLookupDto.nationName, dailyData)
        )
    }

    @Test
    fun `WHEN region has cases THEN region cases returned`() {
        every { areaCasesDataSource.cases(areaLookupDto.regionCode!!) } returns dailyData

        val cases = sut.cases("E1", AreaType.UTLA)

        assertThat(cases).isEqualTo(
            AreaDailyDataDto(areaLookupDto.regionName!!, dailyData)
        )
    }

    @Test
    fun `WHEN utla area has cases THEN utla cases returned`() {
        every { areaCasesDataSource.cases("E1") } returns dailyData
        every {
            areaLookupUseCase.areaName(
                AreaType.UTLA,
                areaLookupDto
            )
        } returns areaLookupDto.utlaName

        val cases = sut.cases("E1", AreaType.UTLA)

        assertThat(cases).isEqualTo(
            AreaDailyDataDto(areaLookupDto.utlaName, dailyData)
        )
    }

    @Test
    fun `WHEN ltla area has cases THEN ltla cases returned`() {
        every { areaCasesDataSource.cases("E1") } returns dailyData
        every {
            areaLookupUseCase.areaName(
                AreaType.LTLA,
                areaLookupDto
            )
        } returns areaLookupDto.utlaName

        val cases = sut.cases("E1", AreaType.LTLA)

        assertThat(cases).isEqualTo(
            AreaDailyDataDto(areaLookupDto.utlaName, dailyData)
        )
    }

    companion object {
        val dailyData = listOf(
            DailyData(
                10,
                100,
                30.0,
                LocalDate.of(2020, 1, 1)
            )
        )
        val areaLookupDto = AreaLookupDto(
            lsoaCode = "E11011",
            lsoaName = "Soho",
            msoaCode = "E11011",
            msoaName = "Soho",
            ltlaCode = "E1101",
            ltlaName = "Westminster",
            utlaCode = "E1101",
            utlaName = "Westminster",
            nhsTrustCode = "GUYS",
            nhsTrustName = "St Guys",
            nhsRegionCode = "E110",
            nhsRegionName = "London",
            regionCode = Constants.ENGLAND_AREA_CODE,
            regionName = "England",
            nationCode = Constants.UK_AREA_CODE,
            nationName = "United Kingdom"
        )
    }
}
