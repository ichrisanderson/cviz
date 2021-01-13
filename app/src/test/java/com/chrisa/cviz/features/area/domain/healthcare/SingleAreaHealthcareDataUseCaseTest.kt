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
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.HealthcareDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Test

class SingleAreaHealthcareDataUseCaseTest {

    private val healthcareDataSource: HealthcareDataSource = mockk()
    private val sut = SingleAreaHealthcareDataUseCase(healthcareDataSource)

    @Test
    fun `WHEN trustData called THEN data for area returned`() {
        every { healthcareDataSource.healthcareData(area.code) } returns dailyDataList

        val data = sut.trustData(area.name, area.code)

        assertThat(data).isEqualTo(
            AreaDailyDataCollection(
                name = area.name,
                data = listOf(
                    AreaDailyDataDto(
                        name = area.name,
                        data = dailyDataList
                    )
                )
            )
        )
    }

    companion object {
        val area = AreaDto("E1", "London", AreaType.UTLA)
        val dailyData = DailyData(
            newValue = 0,
            cumulativeValue = 0,
            rate = 0.0,
            date = LocalDate.of(2020, 1, 2)
        )
        val dailyDataList = listOf(dailyData)
    }
}
