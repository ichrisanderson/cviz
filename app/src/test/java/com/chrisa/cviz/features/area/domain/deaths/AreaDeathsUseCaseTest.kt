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

package com.chrisa.cviz.features.area.domain.deaths

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.AreaDeathsDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.domain.AreaLookupUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test

class AreaDeathsUseCaseTest {

    private val areaLookupUseCase: AreaLookupUseCase = mockk()
    private val areaDeathsDataSource: AreaDeathsDataSource = mockk()
    private val areaCodeResolver: AreaCodeResolver = mockk()

    private val sut = AreaDeathsUseCase(areaLookupUseCase, areaDeathsDataSource, areaCodeResolver)

    @Before
    fun init() {
        every { areaLookupUseCase.areaLookup(any(), any()) } returns areaLookupDto
        every { areaDeathsDataSource.deaths(any()) } returns emptyList()
        every { areaCodeResolver.defaultAreaDto(any()) } returns defaultAreaDto
    }

    @Test
    fun `WHEN areaLookup null AND default area has no deaths THEN uk deaths returned`() {
        every { areaDeathsDataSource.deaths(defaultAreaDto.code) } returns emptyList()
        every { areaDeathsDataSource.deaths(Constants.UK_AREA_CODE) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA, null)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(Constants.UK_AREA_NAME, dailyData)
        )
    }

    @Test
    fun `WHEN areaLookup null AND default area has deaths THEN default area deaths returned`() {
        every { areaDeathsDataSource.deaths(defaultAreaDto.code) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA, null)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(defaultAreaDto.name, dailyData)
        )
    }

    @Test
    fun `WHEN nation has deaths THEN nation deaths returned`() {
        every { areaDeathsDataSource.deaths(areaLookupDto.nationCode) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA, areaLookupDto)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(areaLookupDto.nationName, dailyData)
        )
    }

    @Test
    fun `WHEN nation has no deaths THEN default deaths returned`() {
        val defaultAreaDto = AreaDto(
            code = Constants.SCOTLAND_AREA_CODE,
            name = Constants.SCOTLAND_AREA_NAME,
            AreaType.NATION
        )
        every { areaDeathsDataSource.deaths(areaLookupDto.nationCode) } returns emptyList()
        every { areaDeathsDataSource.deaths(defaultAreaDto.code) } returns dailyData
        every { areaCodeResolver.defaultAreaDto("S1") } returns defaultAreaDto

        val deaths = sut.deaths("S1", AreaType.UTLA, areaLookupDto)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(defaultAreaDto.name, dailyData)
        )
    }

    @Test
    fun `WHEN nation no default deaths THEN uk deaths returned`() {
        val defaultAreaDto = AreaDto(
            code = Constants.WALES_AREA_CODE,
            name = Constants.WALES_AREA_NAME,
            AreaType.NATION
        )
        every { areaCodeResolver.defaultAreaDto("W1") } returns defaultAreaDto
        every { areaDeathsDataSource.deaths(areaLookupDto.nationCode) } returns emptyList()
        every { areaDeathsDataSource.deaths(defaultAreaDto.code) } returns emptyList()
        every { areaDeathsDataSource.deaths(Constants.UK_AREA_CODE) } returns dailyData

        val deaths = sut.deaths("W1", AreaType.UTLA, areaLookupDto)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(Constants.UK_AREA_NAME, dailyData)
        )
    }

    @Test
    fun `WHEN region has deaths THEN region deaths returned`() {
        every { areaDeathsDataSource.deaths(areaLookupDto.regionCode!!) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA, areaLookupDto)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(areaLookupDto.regionName!!, dailyData)
        )
    }

    @Test
    fun `WHEN utla area has deaths THEN utla deaths returned`() {
        every { areaDeathsDataSource.deaths("E1") } returns dailyData
        every {
            areaLookupUseCase.areaName(
                AreaType.UTLA,
                areaLookupDto
            )
        } returns areaLookupDto.utlaName

        val deaths = sut.deaths("E1", AreaType.UTLA, areaLookupDto)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(areaLookupDto.utlaName, dailyData)
        )
    }

    @Test
    fun `WHEN ltla area has deaths THEN ltla deaths returned`() {
        every { areaDeathsDataSource.deaths("E1") } returns dailyData
        every {
            areaLookupUseCase.areaName(
                AreaType.LTLA,
                areaLookupDto
            )
        } returns areaLookupDto.utlaName

        val deaths = sut.deaths("E1", AreaType.LTLA, areaLookupDto)

        assertThat(deaths).isEqualTo(
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
            nhsRegionCode = "E111",
            nhsRegionName = "London11",
            nhsTrustCode = "GUYS",
            nhsTrustName = "St Guys",
            regionCode = "E12000007",
            regionName = "London",
            nationCode = Constants.ENGLAND_AREA_CODE,
            nationName = Constants.ENGLAND_AREA_NAME
        )

        val defaultAreaDto = AreaDto(
            code = Constants.ENGLAND_AREA_CODE,
            name = Constants.ENGLAND_AREA_NAME,
            AreaType.NATION
        )
    }
}
