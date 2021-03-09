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
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.domain.InsertAreaAssociationUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class NhsRegionDataSynchroniserTest {

    private val healthcareUseCaseFacade: HealthcareUseCaseFacade = mockk(relaxed = true)
    private val insertAreaAssociationUseCase: InsertAreaAssociationUseCase = mockk(relaxed = true)
    private val sut = NhsRegionDataSynchroniser(
        healthcareUseCaseFacade,
        insertAreaAssociationUseCase
    )

    @Test
    fun `WHEN execute called THEN region healthcare is synced`() = runBlocking {
        val areaCode = "area1"
        every {
            healthcareUseCaseFacade.nhsRegionArea(areaCode, areaLookupDto)
        } returns nhsRegion

        sut.execute(areaCode, areaLookupDto)

        coVerify {
            healthcareUseCaseFacade.syncHospitalData(
                nhsRegion.code,
                nhsRegion.areaType
            )
        }
    }

    @Test
    fun `WHEN execute called THEN region healthcare association is created`() = runBlocking {
        val areaCode = "area1"
        every {
            healthcareUseCaseFacade.nhsRegionArea(areaCode, areaLookupDto)
        } returns nhsRegion

        sut.execute(areaCode, areaLookupDto)

        coVerify {
            insertAreaAssociationUseCase.execute(
                areaCode,
                nhsRegion.code,
                AreaAssociationTypeDto.HEALTHCARE_DATA
            )
        }
    }

    companion object {

        private val nhsRegion = AreaDto("", "", AreaType.UTLA)

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
