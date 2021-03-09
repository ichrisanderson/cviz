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
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.data.dtos.HealthcareLookupDto
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class HealthcareLookupDataSynchroniserTest {

    private val healthcareUseCaseFacade: HealthcareUseCaseFacade =
        mockk(relaxed = true)
    private val healthcareAreaDataSynchroniser: HealthcareAreaDataSynchroniser =
        mockk(relaxed = true)
    private val nhsTrustDataSynchroniser: NhsTrustDataSynchroniser =
        mockk(relaxed = true)

    val sut = HealthcareLookupDataSynchroniser(
        healthcareUseCaseFacade,
        healthcareAreaDataSynchroniser,
        nhsTrustDataSynchroniser
    )

    @Test
    fun `GIVEN healthcare lookups are empty WHEN execute called THEN area healthcare synced`() =
        runBlocking {
            val areaCode = "area1"
            val areaType = AreaType.UTLA
            every {
                healthcareUseCaseFacade.healthcareLookups(areaCode)
            } returns emptyList()

            sut.execute(areaCode, areaType, areaLookupDto)

            coVerify {
                healthcareAreaDataSynchroniser.execute(areaCode, areaType, areaLookupDto)
            }
        }

    @Test
    fun `GIVEN healthcare lookups are not-empty WHEN execute called THEN area healthcare synced`() =
        runBlocking {
            val areaCode = "area1"
            val areaType = AreaType.UTLA
            every {
                healthcareUseCaseFacade.healthcareLookups(areaCode)
            } returns listOf(healthcareLookup)

            sut.execute(areaCode, areaType, areaLookupDto)

            coVerify {
                nhsTrustDataSynchroniser.execute(areaCode, healthcareLookup.nhsTrustCode)
            }
        }

    companion object {

        val healthcareLookup = HealthcareLookupDto(
            "area1",
            "trust1"
        )

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
