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
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class HealthcareDataSynchroniserFacadeTest {
    private val localHealthcareDataSynchroniser: LocalHealthcareDataSynchroniser =
        mockk(relaxed = true)
    private val healthcareUseCaseFacade: HealthcareUseCaseFacade =
        mockk(relaxed = true)

    private val sut =
        HealthcareDataSynchroniserFacade(localHealthcareDataSynchroniser, healthcareUseCaseFacade)

    @Test
    fun `GIVEN non-local area type WHEN syncHealthcare called THEN healthcare usecase facade synced`() =
        runBlocking {
            val areaCode = "area1"
            val localAreaTypes = setOf(AreaType.MSOA, AreaType.UTLA, AreaType.LTLA, AreaType.REGION)
            val nonLocalAreaTypes = AreaType.values().filterNot { localAreaTypes.contains(it) }

            nonLocalAreaTypes.forEach { areaType ->
                sut.syncHealthcare(areaCode, areaType, areaLookupDto)

                coVerify { healthcareUseCaseFacade.syncHospitalData(areaCode, areaType) }
            }
        }

    @Test
    fun `GIVEN local area type WHEN syncHealthcare called THEN healthcare usecase facade synced`() =
        runBlocking {
            val areaCode = "area1"
            val localAreaTypes = setOf(AreaType.MSOA, AreaType.UTLA, AreaType.LTLA, AreaType.REGION)

            localAreaTypes.forEach { areaType ->
                sut.syncHealthcare(areaCode, areaType, areaLookupDto)

                coVerify { localHealthcareDataSynchroniser.execute(areaCode, areaType, areaLookupDto) }
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
