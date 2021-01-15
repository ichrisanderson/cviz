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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaCodeResolverTest {

    private val sut = AreaCodeResolver()

    @Test
    fun `GIVEN england area code WHEN defaultAreaDto called THEN england area data returned`() {
        val areaDto = sut.defaultAreaDto("E1")

        assertThat(areaDto).isEqualTo(
            AreaDto(Constants.ENGLAND_AREA_CODE, "England", AreaType.NATION)
        )
    }

    @Test
    fun `GIVEN northern ireland area code WHEN defaultAreaDto called THEN northern ireland area data returned`() {
        val areaDto = sut.defaultAreaDto("N1")

        assertThat(areaDto).isEqualTo(
            AreaDto(Constants.NORTHERN_IRELAND_AREA_CODE, "Northern Ireland", AreaType.NATION)
        )
    }

    @Test
    fun `GIVEN scotland area code WHEN defaultAreaDto called THEN scotland area data returned`() {
        val areaDto = sut.defaultAreaDto("S1")

        assertThat(areaDto).isEqualTo(
            AreaDto(Constants.SCOTLAND_AREA_CODE, "Scotland", AreaType.NATION)
        )
    }

    @Test
    fun `GIVEN wales area code WHEN defaultAreaDto called THEN wales area data returned`() {
        val areaDto = sut.defaultAreaDto("W1")

        assertThat(areaDto).isEqualTo(
            AreaDto(Constants.WALES_AREA_CODE, "Wales", AreaType.NATION)
        )
    }

    @Test
    fun `GIVEN overview area code WHEN defaultAreaDto called THEN overview area data returned`() {
        val areaDto = sut.defaultAreaDto("K1")

        assertThat(areaDto).isEqualTo(
            AreaDto(Constants.UK_AREA_CODE, "United Kingdom", AreaType.OVERVIEW)
        )
    }
}
