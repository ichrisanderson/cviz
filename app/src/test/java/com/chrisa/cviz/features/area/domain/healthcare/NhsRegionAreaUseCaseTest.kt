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
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class NhsRegionAreaUseCaseTest {
    private val areaCodeResolver: AreaCodeResolver = mockk(relaxed = true)
    private val sut = NhsRegionAreaUseCase(areaCodeResolver)

    @Test
    fun `GIVEN lookup is null WHEN nhsRegion called THEN default area data returned`() {
        every { areaCodeResolver.defaultAreaDto("") } returns defaultArea

        val region = sut.nhsRegion("", null)

        assertThat(region).isEqualTo(defaultArea)
    }

    @Test
    fun `GIVEN areaLookup nhs region is null WHEN nhsRegion called THEN default area data returned`() {
        every { areaCodeResolver.defaultAreaDto("") } returns defaultArea

        val region = sut.nhsRegion("", areaLookupDto)

        assertThat(region).isEqualTo(defaultArea)
    }

    @Test
    fun `GIVEN areaLookup nhs region is not null WHEN nhsRegion called THEN default area data returned`() {
        val nhsArea = areaLookupDto.copy(
            nhsRegionName = "NhsRegion",
            nhsRegionCode = "1234"
        )
        val region = sut.nhsRegion("", nhsArea)

        assertThat(region).isEqualTo(
            AreaDto(
                nhsArea.nhsRegionCode!!,
                nhsArea.nhsRegionName!!,
                AreaType.NHS_REGION
            )
        )
    }

    companion object {
        private val defaultArea = AreaDto("", "", AreaType.NATION)

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
