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
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class HealthcareRegionUseCaseTest {

    private val areaCodeResolver: AreaCodeResolver = mockk()
    private val sut = HealthcareRegionUseCase(areaCodeResolver)

    @Test
    fun `GIVEN lookup is null WHEN healthcareArea called THEN default area returned`() {
        val defaultArea = AreaDto("E1", Constants.ENGLAND_AREA_NAME, AreaType.NATION)
        every { areaCodeResolver.defaultAreaDto("") } returns defaultArea

        val areaTypes = AreaType.values()
        areaTypes.forEach { areaType ->
            val area = sut.healthcareArea(
                areaCode = "",
                areaType,
                areaLookup = null
            )

            assertThat(area).isEqualTo(defaultArea)
        }
    }

    @Test
    fun `GIVEN overview area type WHEN healthcareArea called THEN default area returned`() {
        val defaultArea = AreaDto("E1", Constants.ENGLAND_AREA_NAME, AreaType.OVERVIEW)
        every { areaCodeResolver.defaultAreaDto("") } returns defaultArea

        val area = sut.healthcareArea(
            areaCode = "",
            AreaType.OVERVIEW,
            areaLookup
        )

        assertThat(area).isEqualTo(defaultArea)
    }

    @Test
    fun `GIVEN nation area type WHEN healthcareArea called THEN default area returned`() {
        val defaultArea = AreaDto("E1", Constants.ENGLAND_AREA_NAME, AreaType.NATION)
        every { areaCodeResolver.defaultAreaDto("") } returns defaultArea

        val area = sut.healthcareArea(
            areaCode = "",
            AreaType.NATION,
            areaLookup
        )

        assertThat(area).isEqualTo(defaultArea)
    }

    @Test
    fun `GIVEN nhs region lookup is null WHEN healthcareArea called THEN default area returned`() {
        val defaultArea = AreaDto("E1", Constants.ENGLAND_AREA_NAME, AreaType.NATION)
        val lookup = areaLookup.copy(
            nhsTrustCode = null,
            nhsRegionCode = null
        )
        every { areaCodeResolver.defaultAreaDto("") } returns defaultArea

        val areaTypes = AreaType.values()
        areaTypes.forEach { areaType ->
            val area = sut.healthcareArea(
                areaCode = "",
                areaType,
                lookup
            )

            assertThat(area).isEqualTo(defaultArea)
        }
    }

    @Test
    fun `GIVEN nhs region lookup is present WHEN healthcareArea called THEN nhs region area returned`() {
        val lookup = areaLookup.copy(
            nhsTrustCode = null
        )
        val areaTypes = listOf(
            AreaType.UTLA,
            AreaType.LTLA,
            AreaType.NHS_TRUST,
            AreaType.NHS_REGION,
            AreaType.REGION
        )
        areaTypes.forEach { areaType ->
            val area = sut.healthcareArea(
                areaCode = "",
                areaType,
                lookup
            )

            assertThat(area).isEqualTo(
                AreaDto(
                    areaLookup.nhsRegionCode!!,
                    areaLookup.nhsRegionName.orEmpty(),
                    AreaType.NHS_REGION
                )
            )
        }
    }

    @Test
    fun `GIVEN nhs trust lookup is present WHEN healthcareArea called THEN nhs region area returned`() {
        val areaTypes = listOf(
            AreaType.UTLA,
            AreaType.LTLA,
            AreaType.NHS_TRUST
        )
        areaTypes.forEach { areaType ->
            val area = sut.healthcareArea(
                areaCode = "",
                areaType,
                areaLookup
            )

            assertThat(area).isEqualTo(
                AreaDto(
                    areaLookup.nhsTrustCode!!,
                    areaLookup.nhsTrustName.orEmpty(),
                    AreaType.NHS_TRUST
                )
            )
        }
    }

    companion object {
        val areaLookup = AreaLookupDto(
            lsoaCode = "E11011",
            lsoaName = "Soho",
            msoaCode = "E11011",
            msoaName = "Soho",
            ltlaCode = "E1101",
            ltlaName = "Westminster",
            utlaCode = "E1101",
            utlaName = "Westminster",
            nhsRegionCode = "E110",
            nhsRegionName = "London",
            nhsTrustCode = "GUYS",
            nhsTrustName = "St Guys",
            regionCode = "London",
            regionName = "E12000007",
            nationCode = Constants.ENGLAND_AREA_CODE,
            nationName = Constants.ENGLAND_AREA_NAME
        )
    }
}
