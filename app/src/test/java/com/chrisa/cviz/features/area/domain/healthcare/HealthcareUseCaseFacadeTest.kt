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
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class HealthcareUseCaseFacadeTest {
    private val healthcareDataUseCase: HealthcareDataUseCase = mockk(relaxed = true)
    private val healthcareLookupDataSource: HealthcareLookupDataSource = mockk(relaxed = true)
    private val healthcareAreaUseCase: HealthcareAreaUseCase = mockk(relaxed = true)
    private val healthcareSyncUseCase: HealthcareSyncUseCase = mockk(relaxed = true)
    private val nhsRegionAreaUseCase: NhsRegionAreaUseCase = mockk(relaxed = true)
    private val transmissionRateUseCase: TransmissionRateUseCase = mockk(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = HealthcareUseCaseFacade(
        healthcareDataUseCase,
        healthcareLookupDataSource,
        healthcareAreaUseCase,
        healthcareSyncUseCase,
        transmissionRateUseCase,
        nhsRegionAreaUseCase
    )

    @Test
    fun `WHEN healthcareLookups called THEN healthcareDataUseCase executed`() {
        sut.healthcareArea(areaCode, areaType, areaLookup)

        verify(exactly = 1) {
            healthcareAreaUseCase.healthcareArea(
                areaCode,
                areaType,
                areaLookup
            )
        }
    }

    @Test
    fun `WHEN healthcareLookups called THEN healthcareLookupDataSource queried`() {
        sut.healthcareLookups(areaCode)

        verify(exactly = 1) { healthcareLookupDataSource.healthcareLookups(areaCode) }
    }

    @Test
    fun `WHEN healthcareData called THEN healthcareRegionUseCase queried`() {
        sut.admissions(areaCode, areaType, areaLookup)

        verify(exactly = 1) {
            healthcareDataUseCase.admissions(
                areaCode,
                areaType,
                areaLookup
            )
        }
    }

    @Test
    fun `WHEN syncHospitalData called THEN healthcareRegionUseCase queried`() =
        testDispatcher.runBlockingTest {
            sut.syncHospitalData(areaCode, areaType)

            coVerify(exactly = 1) { healthcareSyncUseCase.syncHospitalData(areaCode, areaType) }
        }

    @Test
    fun `WHEN transmissionRate called THEN healthcareRegionUseCase queried`() =
        testDispatcher.runBlockingTest {
            val areaDto = AreaDto(areaCode, areaName, areaType)
            sut.transmissionRate(areaDto)

            verify(exactly = 1) { transmissionRateUseCase.transmissionRate(areaDto) }
        }

    @Test
    fun `WHEN nhsRegionArea called THEN healthcareRegionUseCase queried`() =
        testDispatcher.runBlockingTest {
            sut.nhsRegionArea(areaCode, areaLookup)

            verify(exactly = 1) { nhsRegionAreaUseCase.nhsRegion(areaCode, areaLookup) }
        }

    companion object {
        private val areaCode = "E1"
        private val areaName = "London"
        private val areaType = AreaType.REGION
        private val areaLookup = AreaLookupDto(
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
