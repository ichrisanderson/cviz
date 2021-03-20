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
import com.chrisa.cviz.features.area.domain.InsertAreaAssociationUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class NhsTrustDataSynchroniserTest {

    private val healthcareUseCaseFacade: HealthcareUseCaseFacade =
        mockk(relaxed = true)
    private val insertAreaAssociationUseCase: InsertAreaAssociationUseCase =
        mockk(relaxed = true)
    private val sut = NhsTrustDataSynchroniser(
        healthcareUseCaseFacade,
        insertAreaAssociationUseCase
    )

    @Test
    fun `WHEN execute called THEN nhs trust healthcare is synced`() = runBlocking {
        val areaCode = "area1"
        val nhsTrustCode = "nhsTrust1"

        sut.execute(areaCode, nhsTrustCode)

        coVerify {
            healthcareUseCaseFacade.syncHospitalData(
                nhsTrustCode,
                AreaType.NHS_TRUST
            )
        }
    }

    @Test
    fun `WHEN execute called THEN nhs trust association is created`() = runBlocking {
        val areaCode = "area1"
        val nhsTrustCode = "nhsTrust1"

        sut.execute(areaCode, nhsTrustCode)

        coVerify {
            insertAreaAssociationUseCase.execute(
                areaCode,
                nhsTrustCode,
                AreaAssociationTypeDto.HEALTHCARE_DATA
            )
        }
    }
}
