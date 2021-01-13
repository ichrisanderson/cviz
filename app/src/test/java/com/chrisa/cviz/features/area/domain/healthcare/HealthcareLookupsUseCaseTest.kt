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
import com.chrisa.cviz.features.area.data.HealthcareLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.data.dtos.HealthcareLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class HealthcareLookupsUseCaseTest {

    private val healthcareLookupDataSource: HealthcareLookupDataSource = mockk()
    private val sut = HealthcareLookupsUseCase(healthcareLookupDataSource)

    @Test
    fun `GIVEN area lookup is null WHEN healthcareLookups called THEN empty list returned`() {
        val areaTypes = AreaType.values()
        areaTypes.forEach { areaType ->
            val lookups = sut.healthcareLookups(areaType, null)

            assertThat(lookups).isEmpty()
        }
    }

    @Test
    fun `GIVEN lookups not supported WHEN healthcareLookups called THEN empty list returned`() {
        val areaTypes = AreaType.values().filter { !supportedLookups.contains(it) }
        areaTypes.forEach { areaType ->
            val lookups = sut.healthcareLookups(areaType, areaLookup)

            assertThat(lookups).isEmpty()
        }
    }

    @Test
    fun `GIVEN lookups supported WHEN healthcareLookups called THEN empty list returned`() {
        every { healthcareLookupDataSource.healthcareLookups(areaLookup.utlaCode) } returns healthcareLookups
        val areaTypes = AreaType.values().filter { supportedLookups.contains(it) }
        areaTypes.forEach { areaType ->
            val lookups = sut.healthcareLookups(areaType, areaLookup)

            assertThat(lookups).isEqualTo(healthcareLookups.map { it.nhsTrustCode })
        }
    }

    companion object {
        val supportedLookups = setOf(AreaType.LTLA, AreaType.UTLA)
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
            regionCode = Constants.ENGLAND_AREA_CODE,
            regionName = "England",
            nationCode = Constants.UK_AREA_CODE,
            nationName = "United Kingdom"
        )
        val healthcareLookups = listOf(
            HealthcareLookupDto("1", "Area 1"),
            HealthcareLookupDto("2", "Area 2"),
            HealthcareLookupDto("3", "Area 3"),
            HealthcareLookupDto("4", "Area 4")
        )
    }
}
