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

package com.chrisa.cviz.features.area.domain.arealookup

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaLookupCodeResolverTest {
    private val sut = AreaLookupCodeResolver()

    @Test
    fun `GIVEN msoa area WHEN areaLookupCode called THEN lookup utla code returned`() {
        val utlaLookup =
            areaLookupDto.copy(utlaCode = "E001")
        val lookupCode =
            sut.areaLookupCode(areaCode = "", areaType = AreaType.MSOA, utlaLookup)

        assertThat(lookupCode).isEqualTo(
            AreaLookupCode(utlaLookup.utlaCode, AreaType.UTLA)
        )
    }

    @Test
    fun `GIVEN non-msoa area WHEN areaLookupCode called THEN lookup utla code returned`() {
        val areaTypes = AreaType.values().filter { it != AreaType.MSOA }
        val areaCode = "E001"

        areaTypes.forEach { areaType ->
            val lookupCode =
                sut.areaLookupCode(areaCode, areaType, null)

            assertThat(lookupCode).isEqualTo(
                AreaLookupCode(areaCode, areaType)
            )
        }
    }

    companion object {

        private val areaLookupDto = AreaLookupDto(
            lsoaCode = "",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "",
            utlaName = "",
            nhsRegionCode = null,
            nhsRegionName = null,
            nhsTrustCode = null,
            nhsTrustName = null,
            regionCode = "",
            regionName = null,
            nationCode = "",
            nationName = ""
        )
    }
}
