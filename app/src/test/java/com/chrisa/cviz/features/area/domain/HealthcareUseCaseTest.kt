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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.HealthcareDataSource
import com.chrisa.cviz.features.area.data.HealthcareLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class HealthcareUseCaseTest {

    private val healthcareDataSynchroniser: HealthcareDataSynchroniser = mockk()
    private val healthcareDataSource: HealthcareDataSource = mockk()
    private val healthcareLookupDataSource: HealthcareLookupDataSource = mockk()
    private val areaCodeResolver: AreaCodeResolver = mockk()
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = HealthcareUseCase(
        healthcareDataSynchroniser,
        healthcareDataSource,
        areaCodeResolver,
        healthcareLookupDataSource
    )

    @Test
    fun `GIVEN nhsTrustCode present WHEN healthCareRegion called THEN nhs region returned`() {
        val lookup = areaLookupDto
        val result = sut.healthCareRegion("E1", AreaType.UTLA, lookup)

        assertThat(result).isEqualTo(
            AreaDto(
                areaLookupDto.nhsTrustCode!!,
                areaLookupDto.nhsTrustName!!,
                AreaType.NHS_TRUST
            )
        )
    }

    @Test
    fun `GIVEN nhsRegionCode present WHEN healthCareRegion called THEN nhs region returned`() {
        val lookup = areaLookupDto.copy(nhsTrustCode = null)
        val result = sut.healthCareRegion("E1", AreaType.UTLA, lookup)

        assertThat(result).isEqualTo(
            AreaDto(
                areaLookupDto.nhsRegionCode!!,
                areaLookupDto.nhsRegionName!!,
                AreaType.NHS_REGION
            )
        )
    }

    @Test
    fun `GIVEN nhsRegionCode not present WHEN healthCareRegion called THEN default area data returned`() {
        val lookup = areaLookupDto.copy(nhsTrustCode = null, nhsRegionCode = null)
        val defaultArea = AreaDto("E1", "England", AreaType.NATION)
        every { areaCodeResolver.defaultAreaDto("E1") } returns defaultArea

        val result = sut.healthCareRegion("E1", AreaType.UTLA, lookup)

        assertThat(result).isEqualTo(defaultArea)
    }

    @Test
    fun `WHEN healthcare data synchroniser throws THEN sync succeeds`() =
        testDispatcher.runBlockingTest {
            coEvery {
                healthcareDataSynchroniser.performSync(
                    "E1",
                    AreaType.UTLA
                )
            } throws IOException()

            val result = sut.syncHospitalData("E1", AreaType.UTLA)

            assertThat(result).isEqualTo(Unit)
        }

    @Test
    fun `WHEN healthcare data synchroniser runs THEN sync succeeds`() =
        testDispatcher.runBlockingTest {
            coEvery {
                healthcareDataSynchroniser.performSync(
                    "E1",
                    AreaType.UTLA
                )
            } just Runs

            val result = sut.syncHospitalData("E1", AreaType.UTLA)

            assertThat(result).isEqualTo(Unit)
        }

    companion object {
        val areaLookupDto = AreaLookupDto(
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
    }
}
