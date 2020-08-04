package com.chrisa.covid19.features.area.domain.helper

import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class RollingAverageHelperTest {

    @Test
    fun `WHEN previous case is seven days previous THEN average is calculated`() {
        val sut = RollingAverageHelper()

        val previousCase = CaseDto(
            date = LocalDate.of(2020, 1, 1),
            dailyLabConfirmedCases = 100,
            totalLabConfirmedCases = 1320
        )

        val currentCase = CaseDto(
            date = LocalDate.of(2020, 1, 7),
            dailyLabConfirmedCases = 170,
            totalLabConfirmedCases = 2420
        )

        val result = sut.average(currentCase, previousCase)

        assertThat(result).isEqualTo((currentCase.totalLabConfirmedCases - previousCase.totalLabConfirmedCases) / 7.0)
    }

    @Test
    fun `WHEN previous case is one day previous THEN average is calculated`() {
        val sut = RollingAverageHelper()

        val previousCase = CaseDto(
            date = LocalDate.of(2020, 1, 1),
            dailyLabConfirmedCases = 100,
            totalLabConfirmedCases = 1320
        )

        val currentCase = CaseDto(
            date = LocalDate.of(2020, 1, 2),
            dailyLabConfirmedCases = 170,
            totalLabConfirmedCases = 2420
        )

        val result = sut.average(currentCase, previousCase)

        assertThat(result).isEqualTo((currentCase.totalLabConfirmedCases - previousCase.totalLabConfirmedCases) / 2.0)
    }

    @Test
    fun `WHEN previous case on a different date THEN average is calculated`() {
        val sut = RollingAverageHelper()

        val previousCase = CaseDto(
            date = LocalDate.of(2020, 1, 1),
            dailyLabConfirmedCases = 100,
            totalLabConfirmedCases = 1320
        )

        val currentCase = CaseDto(
            date = LocalDate.of(2020, 1, 7),
            dailyLabConfirmedCases = 170,
            totalLabConfirmedCases = 2420
        )

        val result = sut.average(currentCase, previousCase)

        assertThat(result).isEqualTo((currentCase.totalLabConfirmedCases - previousCase.totalLabConfirmedCases) / 7.0)
    }

    @Test
    fun `WHEN no previous case THEN average is zero`() {
        val sut = RollingAverageHelper()

        val currentCase = CaseDto(
            date = LocalDate.of(2020, 1, 8),
            dailyLabConfirmedCases = 170,
            totalLabConfirmedCases = 2420
        )

        val result = sut.average(currentCase, null)

        assertThat(result).isEqualTo(0)
    }
}
