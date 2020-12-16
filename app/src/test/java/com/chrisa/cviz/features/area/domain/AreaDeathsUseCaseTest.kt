package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.AreaDeathsDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AreaDeathsUseCaseTest {

    private val areaLookupUseCase: AreaLookupUseCase = mockk()
    private val areaDeathsDataSource: AreaDeathsDataSource = mockk()

    private val sut = AreaDeathsUseCase(areaLookupUseCase, areaDeathsDataSource)

    @Before
    fun init() {
        every { areaLookupUseCase.areaLookup(any(), any()) } returns areaLookupDto
        every { areaDeathsDataSource.deaths(any()) } returns emptyList()
    }

    @Test
    fun `WHEN areaLookup not found THEN uk deaths returned`() {
        every { areaLookupUseCase.areaLookup(any(), any()) } returns null
        every { areaDeathsDataSource.deaths(Constants.UK_AREA_CODE) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto("United Kingdom", dailyData)
        )
    }

    @Test
    fun `WHEN nation has deaths THEN nation deaths returned`() {
        every { areaDeathsDataSource.deaths(areaLookupDto.nationCode) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA)

        assertThat(deaths).isEqualTo(
            AreaDailyDataDto(areaLookupDto.nationName, dailyData)
        )
    }

    @Test
    fun `WHEN region has deaths THEN region deaths returned`() {
        every { areaDeathsDataSource.deaths(areaLookupDto.regionCode) } returns dailyData

        val deaths = sut.deaths("E1", AreaType.UTLA)

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

        val deaths = sut.deaths("E1", AreaType.UTLA)

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

        val deaths = sut.deaths("E1", AreaType.LTLA)

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
            nhsRegionCode = "E110",
            nhsRegionName = "London",
            regionCode = Constants.ENGLAND_AREA_CODE,
            regionName = "England",
            nationCode = Constants.UK_AREA_CODE,
            nationName = "United Kingdom"
        )

    }
}
