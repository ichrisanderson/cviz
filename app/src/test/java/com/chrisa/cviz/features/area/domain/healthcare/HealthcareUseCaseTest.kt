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

package com.chrisa.cviz.features.area.domain.healthcare

import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.HealthcareDataSource
import com.chrisa.cviz.features.area.data.HealthcareLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class HealthcareUseCaseTest {

    private val healthcareDataSynchroniser: HealthcareDataSynchroniser = mockk()
    private val healthcareDataSource: HealthcareDataSource = mockk()
    private val healthcareLookupDataSource: HealthcareLookupDataSource = mockk()
    private val areaCodeResolver: AreaCodeResolver = mockk()
    private val testDispatcher = TestCoroutineDispatcher()

//    private val sut = HealthcareUseCase(
//        healthcareDataSynchroniser,
//        healthcareDataSource,
//        areaCodeResolver,
//        healthcareLookupDataSource
//    )
//
//    @Test
//    fun `GIVEN areaLookup is null WHEN healthcareData called THEN single area data returned`() {
//        val areaCode = "E1"
//        val areaName = "London"
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            AreaType.UTLA
//        )
//        every { healthcareDataSource.healthcareData(areaCode) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = AreaType.UTLA,
//            areaLookup = null
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaName,
//                listOf(
//                    AreaDailyDataDto(
//                        areaName,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN overview area WHEN healthcareData called THEN overview health data returned`() {
//        val areaCode = "Overview1"
//        val areaName = "Overview"
//        val areaType = AreaType.OVERVIEW
//
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            areaType
//        )
//        every { healthcareDataSource.healthcareData(areaCode) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = areaType,
//            areaLookup = areaLookupDto
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaName,
//                listOf(
//                    AreaDailyDataDto(
//                        areaName,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN nation area WHEN healthcareData called THEN nation health data returned`() {
//        val areaCode = "Nation1"
//        val areaName = "Nation"
//        val areaType = AreaType.NATION
//
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            areaType
//        )
//        every { healthcareDataSource.healthcareData(areaCode) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = areaType,
//            areaLookup = areaLookupDto
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaName,
//                listOf(
//                    AreaDailyDataDto(
//                        areaName,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN region area WHEN healthcareData called THEN region data returned`() {
//        val areaCode = "Region1"
//        val areaName = "Region"
//        val areaType = AreaType.REGION
//
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            areaType
//        )
//        every { healthcareDataSource.healthcareData(areaCode) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = areaType,
//            areaLookup = areaLookupDto
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaName,
//                listOf(
//                    AreaDailyDataDto(
//                        areaName,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN nhs region area WHEN healthcareData called THEN nhs region data returned`() {
//        val areaCode = "NhsRegion1"
//        val areaName = "NhsRegion"
//        val areaType = AreaType.NHS_REGION
//
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            areaType
//        )
//        every { healthcareDataSource.healthcareData(areaLookupDto.nhsRegionCode!!) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = areaType,
//            areaLookup = areaLookupDto
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaLookupDto.nhsRegionName!!,
//                listOf(
//                    AreaDailyDataDto(
//                        areaLookupDto.nhsRegionName!!,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN nhs trust area WHEN healthcareData called THEN nhs trust data returned`() {
//        val areaCode = "NhsTrust1"
//        val areaName = "NhsTrust"
//        val areaType = AreaType.NHS_TRUST
//
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            areaType
//        )
//        every { healthcareDataSource.healthcareData(areaLookupDto.nhsTrustCode!!) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = areaType,
//            areaLookup = areaLookupDto
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaLookupDto.nhsTrustName!!,
//                listOf(
//                    AreaDailyDataDto(
//                        areaLookupDto.nhsTrustName!!,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN utla or ltla area and lookup has no nhs data WHEN healthcareData called THEN nhs trust data returned`() {
//        val areaCode = "Utla1"
//        val areaName = "Utla"
//        val areaType = AreaType.UTLA
//        val lookup = areaLookupDto.copy(
//            nhsRegionCode = null,
//            nhsTrustCode = null
//        )
//
//        every { areaCodeResolver.defaultAreaDto(areaCode) } returns AreaDto(
//            areaCode,
//            areaName,
//            areaType
//        )
//        every { healthcareLookupDataSource.healthcareLookups(lookup.utlaCode) } returns emptyList()
//        every { healthcareDataSource.healthcareData(areaCode) } returns listOf(dailyData)
//
//        val healthcareData = sut.healthcareData(
//            areaCode = areaCode,
//            areaType = areaType,
//            areaLookup = lookup
//        )
//
//        assertThat(healthcareData).isEqualTo(
//            AreaDailyDataCollection(
//                areaName,
//                listOf(
//                    AreaDailyDataDto(
//                        areaName,
//                        listOf(dailyData)
//                    )
//                )
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN nhsTrustCode present WHEN healthCareRegion called THEN nhs region returned`() {
//        val lookup = areaLookupDto
//        val result = sut.healthCareRegion("E1", AreaType.UTLA, lookup)
//
//        assertThat(result).isEqualTo(
//            AreaDto(
//                areaLookupDto.nhsTrustCode!!,
//                areaLookupDto.nhsTrustName!!,
//                AreaType.NHS_TRUST
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN nhsRegionCode present WHEN healthCareRegion called THEN nhs region returned`() {
//        val lookup = areaLookupDto.copy(nhsTrustCode = null)
//        val result = sut.healthCareRegion("E1", AreaType.UTLA, lookup)
//
//        assertThat(result).isEqualTo(
//            AreaDto(
//                areaLookupDto.nhsRegionCode!!,
//                areaLookupDto.nhsRegionName!!,
//                AreaType.NHS_REGION
//            )
//        )
//    }
//
//    @Test
//    fun `GIVEN nhsRegionCode not present WHEN healthCareRegion called THEN default area data returned`() {
//        val lookup = areaLookupDto.copy(nhsTrustCode = null, nhsRegionCode = null)
//        val defaultArea = AreaDto("E1", "England", AreaType.NATION)
//        every { areaCodeResolver.defaultAreaDto("E1") } returns defaultArea
//
//        val result = sut.healthCareRegion("E1", AreaType.UTLA, lookup)
//
//        assertThat(result).isEqualTo(defaultArea)
//    }
//
//    @Test
//    fun `WHEN healthcare data synchroniser throws THEN sync succeeds`() =
//        testDispatcher.runBlockingTest {
//            coEvery {
//                healthcareDataSynchroniser.performSync(
//                    "E1",
//                    AreaType.UTLA
//                )
//            } throws IOException()
//
//            val result = sut.syncHospitalData("E1", AreaType.UTLA)
//
//            assertThat(result).isEqualTo(Unit)
//        }
//
//    @Test
//    fun `WHEN healthcare data synchroniser runs THEN sync succeeds`() =
//        testDispatcher.runBlockingTest {
//            coEvery {
//                healthcareDataSynchroniser.performSync(
//                    "E1",
//                    AreaType.UTLA
//                )
//            } just Runs
//
//            val result = sut.syncHospitalData("E1", AreaType.UTLA)
//
//            assertThat(result).isEqualTo(Unit)
//        }
//
//    @Test
//    fun `WHEN healthcareData called THEN healthcareDataFoAreaCodes returned`() {
//        val areaCodes = listOf("A", "B", "C")
//        every { healthcareDataSource.healthcareDataFoAreaCodes(areaCodes) } returns listOf(
//            areaDailyDataDto
//        )
//
//        val healthcareData = sut.healthcareData(areaCodes)
//
//        assertThat(healthcareData).isEqualTo(listOf(areaDailyDataDto))
//    }

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
        val dailyData = DailyData(
            newValue = 0,
            cumulativeValue = 0,
            rate = 0.0,
            date = LocalDate.of(2020, 1, 2)
        )
        val areaDailyDataDto = AreaDailyDataDto(
            name = "London",
            data = listOf(dailyData)
        )
    }
}
