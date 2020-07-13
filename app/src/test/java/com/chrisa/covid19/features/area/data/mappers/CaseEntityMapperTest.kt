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

package com.chrisa.covid19.features.area.data.mappers

import com.chrisa.covid19.core.data.db.CaseEntity
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.mappers.CaseEntityMapper.toCaseDto
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class CaseEntityMapperTest {

    @Test
    fun `WHEN toCaseDto called THEN dto is created with correct details`() {
        val entity = CaseEntity(
            areaCode = "2234",
            areaName = "London",
            dailyLabConfirmedCases = 1,
            dailyTotalLabConfirmedCasesRate = 11.0,
            date = LocalDate.ofEpochDay(0),
            totalLabConfirmedCases = 111
        )

        assertThat(entity.toCaseDto()).isEqualTo(
            CaseDto(
                dailyLabConfirmedCases = entity.dailyLabConfirmedCases,
                date = entity.date
            )
        )
    }
}
