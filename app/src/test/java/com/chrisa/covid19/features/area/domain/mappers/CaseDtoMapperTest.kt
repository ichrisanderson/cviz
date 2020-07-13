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

package com.chrisa.covid19.features.area.domain.mappers

import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.domain.mappers.CaseDtoMapper.toCaseModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class CaseDtoMapperTest {
    @Test
    fun `WHEN toCaseModel called THEN model is created with correct details`() {

        val caseDto = CaseDto(
            dailyLabConfirmedCases = 1,
            date = LocalDate.ofEpochDay(0)
        )

        val model = caseDto.toCaseModel()

        assertThat(model).isEqualTo(
            CaseModel(
                dailyLabConfirmedCases = caseDto.dailyLabConfirmedCases,
                date = caseDto.date
            )
        )
    }
}
