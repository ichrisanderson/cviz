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

package com.chrisa.cviz.features.area.domain.synchronisers

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.domain.AlertLevelUseCase
import com.chrisa.cviz.features.area.domain.AreaDataSynchroniserWrapper
import com.chrisa.cviz.features.area.domain.AreaDetailSynchroniser
import com.chrisa.cviz.features.area.domain.AreaLookupUseCase
import com.chrisa.cviz.features.area.domain.InsertAreaAssociationUseCase
import com.chrisa.cviz.features.area.domain.arealookup.AreaLookupCodeResolver
import com.chrisa.cviz.features.area.domain.healthcare.HealthcareDataSynchroniserFacade
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AreaDetailSynchroniserTest {

    private val areaLookupUseCase: AreaLookupUseCase = mockk(relaxed = true)
    private val insertAreaAssociationUseCase: InsertAreaAssociationUseCase = mockk(relaxed = true)
    private val areaLookupCodeResolver: AreaLookupCodeResolver = AreaLookupCodeResolver()
    private val areaDataSynchroniser: AreaDataSynchroniserWrapper = mockk(relaxed = true)
    private val healthcareDataSynchroniserFacade: HealthcareDataSynchroniserFacade =
        mockk(relaxed = true)

    private val sut = AreaDetailSynchroniser(
        areaLookupUseCase,
        insertAreaAssociationUseCase,
        areaLookupCodeResolver,
        areaDataSynchroniser,
        healthcareDataSynchroniserFacade
    )

    @Test
    fun `WHEN performSync called THEN area data sync triggered`() = runBlocking {
        val areaCode = "area1"
        val areaType = AreaType.UTLA
        every { areaLookupUseCase.areaLookup(areaCode, areaType) } returns areaLookupDto

        sut.performSync(areaCode, areaType)

        coVerify {
            areaDataSynchroniser.execute(areaCode, AreaType.UTLA)
        }
    }

    @Test
    fun `WHEN performSync called THEN healthcare data sync triggered`() = runBlocking {
        val areaCode = "area1"
        val areaType = AreaType.UTLA
        every { areaLookupUseCase.areaLookup(areaCode, areaType) } returns areaLookupDto

        sut.performSync(areaCode, areaType)

        coVerify {
            healthcareDataSynchroniserFacade.syncHealthcare(
                areaCode,
                areaCode,
                AreaType.UTLA,
                areaLookupDto
            )
        }
    }

    @Test
    fun `WHEN performSync called THEN area lookup association created`() = runBlocking {
        val areaCode = "area1"
        val areaType = AreaType.UTLA
        every { areaLookupUseCase.areaLookup(areaCode, areaType) } returns areaLookupDto

        sut.performSync(areaCode, areaType)

        verify {
            insertAreaAssociationUseCase.execute(
                areaCode,
                areaLookupDto.lsoaCode,
                AreaAssociationTypeDto.AREA_LOOKUP
            )
        }
    }

    @Test
    fun `WHEN performSync called THEN area data association created`() =
        runBlocking {
            val areaCode = "area1"
            val areaType = AreaType.UTLA
            every { areaLookupUseCase.areaLookup(areaCode, areaType) } returns areaLookupDto

            sut.performSync(areaCode, areaType)

            verify {
                insertAreaAssociationUseCase.execute(
                    areaCode,
                    areaCode,
                    AreaAssociationTypeDto.AREA_DATA
                )
            }
        }

    companion object {

        private val areaLookupDto = AreaLookupDto(
            lsoaCode = "lsoa1",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "utla1",
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
