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

package com.chrisa.covid19.features.area.domain.helper

import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

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
