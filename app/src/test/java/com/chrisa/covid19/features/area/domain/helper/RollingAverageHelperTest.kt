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

        val startDate = LocalDate.of(2020, 1, 1)
        val cases = mutableListOf<CaseDto>()
        for (i in 0 until 7) {
            cases.add(
                CaseDto(
                    newCases = 10 * (i + 1),
                    cumulativeCases = 0,
                    date = startDate.plusDays(i.toLong()),
                    infectionRate = 30.0,
                    baseRate = 0.8
                )
            )
        }

        // 10, 20, 30, 40, 50, 60, 70 = 280
        val result = sut.average(cases.lastIndex, cases)

        assertThat(result).isEqualTo(280 / 7.0)
    }
}
