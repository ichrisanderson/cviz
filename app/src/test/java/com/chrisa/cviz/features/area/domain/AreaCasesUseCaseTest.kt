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

import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.AreaCasesDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test

class AreaCasesUseCaseTest {

    private val areaCasesDataSource: AreaCasesDataSource = mockk()
    private val sut = AreaCasesUseCase(areaCasesDataSource)

    @Before
    fun init() {
        every { areaCasesDataSource.cases(any()) } returns emptyList()
    }

    @Test
    fun `WHEN areaLookup null THEN uk cases returned`() {
        val areaName = "London"
        val areaCode = "E1"
        every { areaCasesDataSource.areaName(areaCode) } returns areaName
        every { areaCasesDataSource.cases(areaCode) } returns dailyData

        val cases = sut.cases(areaCode)

        assertThat(cases).isEqualTo(AreaDailyDataDto(areaName, dailyData))
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
    }
}
