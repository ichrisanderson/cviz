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

import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Test

class MultiAreaHealthcareDataUseCaseTest {

    private val healthcareAreaCodesUseCase: HealthcareAreaCodesUseCase = mockk(relaxed = true)
    private val sut = MultiAreaHealthcareDataUseCase(healthcareAreaCodesUseCase)

    @Test
    fun `WHEN trustData called THEN data for area codes returned`() {
        every { healthcareAreaCodesUseCase.healthcareDataFoAreaCodes(areaCodes) } returns nhsAreaData

        val data = sut.trustData(areaName, areaCodes)

        assertThat(data).isEqualTo(
            AreaDailyDataCollection(
                name = areaName,
                data = nhsAreaData
            )
        )
    }

    companion object {
        val areaName = "London"
        val areaCodes = listOf("1", "2", "3")
        val dailyData = DailyData(
            newValue = 0,
            cumulativeValue = 0,
            rate = 0.0,
            date = LocalDate.of(2020, 1, 2)
        )
        val nhsAreaData = listOf(
            AreaDailyDataDto(
                name = "Trust1",
                data = listOf(dailyData)
            ),
            AreaDailyDataDto(
                name = "Trust2",
                data = listOf(dailyData)
            ),
            AreaDailyDataDto(
                name = "Trust3",
                data = listOf(dailyData)
            )
        )
    }
}
