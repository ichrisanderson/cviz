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
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaDetailDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.domain.arealookup.AreaLookupCodeResolver
import com.chrisa.cviz.features.area.domain.deaths.AreaDeathsFacade
import com.chrisa.cviz.features.area.domain.healthcare.HealthcareUseCaseFacade
import com.chrisa.cviz.features.area.domain.models.AlertLevelModel
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.domain.models.AreaTransmissionRateModel
import com.chrisa.cviz.features.area.domain.models.SoaDataModel
import com.chrisa.cviz.features.area.domain.models.TransmissionRateModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AreaDetailUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()
    private val areaLookupUseCase = mockk<AreaLookupUseCase>()
    private val healthcareFacade = mockk<HealthcareUseCaseFacade>()
    private val areaCasesUseCase = mockk<AreaCasesUseCase>()
    private val areaDeathsFacade = mockk<AreaDeathsFacade>()
    private val alertLevelUseCase = mockk<AlertLevelUseCase>()
    private val soaDataUseCase = mockk<SoaDataUseCase>()
    private val areaLookupCodeResolver = AreaLookupCodeResolver()
    private val areaDataSynchroniser = mockk<AreaDataSynchroniserFacade>()

    private val sut = AreaDetailUseCase(
        areaDataSource,
        areaLookupUseCase,
        areaCasesUseCase,
        areaDeathsFacade,
        healthcareFacade,
        alertLevelUseCase,
        soaDataUseCase,
        areaLookupCodeResolver,
        areaDataSynchroniser
    )

    @Before
    fun setup() {
        coEvery { areaDataSynchroniser.performSync(any(), any()) } just Runs
        coEvery { areaLookupUseCase.syncAreaLookup(any(), any()) } just Runs
        coEvery { healthcareFacade.syncHospitalData(any(), any()) } just Runs
        every { areaLookupUseCase.areaLookup(any(), any()) } returns areaLookupDto
        every { healthcareFacade.healthcareArea(any(), any(), any()) } returns ukArea
        every { healthcareFacade.nhsRegionArea(any(), any()) } returns ukArea
        every { healthcareFacade.transmissionRate(any()) } returns null
        every {
            healthcareFacade.admissions(
                any(),
                any(),
                any()
            )
        } returns ukAreaDailyDataCollection
        every { areaCasesUseCase.cases(any()) } returns ukAreaCaseDataDto
        every {
            areaDeathsFacade.publishedDeaths(
                any(),
                any(),
                any()
            )
        } returns ukAreaPublishedDeathsDataDto
        every { areaDeathsFacade.onsDeaths(any(), any(), any()) } returns ukAreaOnsDeathsDataDto
        every { healthcareFacade.healthcareLookups(any()) } returns emptyList()
        coEvery { alertLevelUseCase.alertLevel(any(), any()) } returns null
        coEvery { soaDataUseCase.syncSoaData(any(), any()) } just Runs
        every { soaDataUseCase.byAreaCode(any(), any()) } returns null
        every { areaDataSource.metadata(any()) } returns metadata
    }

    @Test
    fun `WHEN execute called THEN area cases are synced`() =
        runBlocking {
            every { areaDataSource.metadataAsFlow(ukAreaDetailDto.areaCode) } returns listOf(null).asFlow()

            sut.execute(ukAreaDetailDto.areaCode, ukAreaDetailDto.areaType.toAreaType())

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    ukAreaDetailDto.areaCode,
                    ukAreaDetailDto.areaType.toAreaType()
                )
            }
        }

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits no data result`() =
        runBlocking {
            every { areaDataSource.metadataAsFlow(ukAreaDetailDto.areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow =
                sut.execute(ukAreaDetailDto.areaCode, ukAreaDetailDto.areaType.toAreaType())

            areaDetailModelFlow.collect { resultResult ->
                assertThat(resultResult).isEqualTo(AreaDetailModelResult.NoData)
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {
            every { areaDataSource.metadataAsFlow(areaWithCases.areaCode) } returns
                listOf(metadata).asFlow()

            val areaDetailModelFlow =
                sut.execute(areaWithCases.areaCode, areaWithCases.areaType.toAreaType())

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            lastUpdatedAt = lastUpdatedDateTime,
                            casesAreaName = ukAreaCaseDataDto.name,
                            cases = ukAreaCaseDataDto.data,
                            deathsByPublishedDateAreaName = ukAreaPublishedDeathsDataDto.name,
                            deathsByPublishedDate = ukAreaPublishedDeathsDataDto.data,
                            onsDeathAreaName = ukAreaOnsDeathsDataDto.name,
                            onsDeathsByRegistrationDate = ukAreaOnsDeathsDataDto.data,
                            hospitalAdmissionsAreaName = Constants.UK_AREA_NAME,
                            hospitalAdmissions = emptyList(),
                            transmissionRate = null,
                            alertLevel = null
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest deaths for the area`() =
        runBlocking {
            every { areaDataSource.metadataAsFlow(areaWithDeaths.areaCode) } returns
                listOf(metadata).asFlow()

            val areaDetailModelFlow =
                sut.execute(areaWithDeaths.areaCode, areaWithDeaths.areaType.toAreaType())

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            lastUpdatedAt = lastUpdatedDateTime,
                            casesAreaName = ukAreaCaseDataDto.name,
                            cases = ukAreaCaseDataDto.data,
                            deathsByPublishedDateAreaName = ukAreaPublishedDeathsDataDto.name,
                            deathsByPublishedDate = ukAreaPublishedDeathsDataDto.data,
                            onsDeathAreaName = ukAreaOnsDeathsDataDto.name,
                            onsDeathsByRegistrationDate = ukAreaOnsDeathsDataDto.data,
                            hospitalAdmissionsAreaName = Constants.UK_AREA_NAME,
                            hospitalAdmissions = emptyList(),
                            transmissionRate = null,
                            alertLevel = null
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest hospital admissions for the area`() =
        runBlocking {
            every { areaDataSource.metadataAsFlow(areaWithHospitalAdmissions.areaCode) } returns
                listOf(metadata).asFlow()
            every { healthcareFacade.healthcareArea(any(), any(), any()) } returns
                AreaDto(
                    areaWithHospitalAdmissions.areaCode,
                    areaWithHospitalAdmissions.areaName,
                    areaWithHospitalAdmissions.areaType.toAreaType()
                )
            every { healthcareFacade.admissions(any(), any(), any()) } returns
                ukAreaDailyDataCollection.copy(
                    data = listOf(
                        AreaDailyDataDto(
                            areaWithHospitalAdmissions.areaName,
                            SynchronisationTestData.dailyData()
                        )
                    )
                )

            val areaDetailModelFlow = sut.execute(
                areaWithHospitalAdmissions.areaCode,
                areaWithHospitalAdmissions.areaType.toAreaType()
            )

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            lastUpdatedAt = lastUpdatedDateTime,
                            casesAreaName = ukAreaCaseDataDto.name,
                            cases = ukAreaCaseDataDto.data,
                            deathsByPublishedDateAreaName = ukAreaPublishedDeathsDataDto.name,
                            deathsByPublishedDate = ukAreaPublishedDeathsDataDto.data,
                            onsDeathAreaName = ukAreaOnsDeathsDataDto.name,
                            onsDeathsByRegistrationDate = ukAreaOnsDeathsDataDto.data,
                            hospitalAdmissionsAreaName = areaWithHospitalAdmissions.areaName,
                            hospitalAdmissions = listOf(
                                AreaDailyDataDto(
                                    areaWithHospitalAdmissions.areaName,
                                    SynchronisationTestData.dailyData()
                                )
                            ),
                            transmissionRate = null,
                            alertLevel = null
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest transmission data for the area`() =
        runBlocking {
            every { areaDataSource.metadataAsFlow(ukAreaDetailDto.areaCode) } returns
                listOf(metadata).asFlow()
            every {
                healthcareFacade.transmissionRate(ukArea)
            } returns
                nhsAreaTransmissionRateDto

            val areaDetailModelFlow = sut.execute(
                ukAreaDetailDto.areaCode,
                ukAreaDetailDto.areaType.toAreaType()
            )

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            lastUpdatedAt = lastUpdatedDateTime,
                            casesAreaName = ukAreaCaseDataDto.name,
                            cases = ukAreaCaseDataDto.data,
                            deathsByPublishedDateAreaName = ukAreaPublishedDeathsDataDto.name,
                            deathsByPublishedDate = ukAreaPublishedDeathsDataDto.data,
                            onsDeathAreaName = ukAreaOnsDeathsDataDto.name,
                            onsDeathsByRegistrationDate = ukAreaOnsDeathsDataDto.data,
                            hospitalAdmissionsAreaName = ukAreaDetailDto.areaName,
                            hospitalAdmissions = emptyList(),
                            transmissionRate = nhsAreaTransmissionRateDto
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest alert level data for the area`() =
        runBlocking {
            val alertLevel = AlertLevelModel(
                alertLevelUrl = "http://acme.com"
            )
            every { areaDataSource.metadataAsFlow(ukAreaDetailDto.areaCode) } returns
                listOf(metadata).asFlow()
            every {
                alertLevelUseCase.alertLevel(
                    ukAreaDetailDto.areaCode,
                    ukAreaDetailDto.areaType.toAreaType()
                )
            } returns
                alertLevel

            val areaDetailModelFlow = sut.execute(
                ukAreaDetailDto.areaCode,
                ukAreaDetailDto.areaType.toAreaType()
            )

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            lastUpdatedAt = lastUpdatedDateTime,
                            casesAreaName = ukAreaCaseDataDto.name,
                            cases = ukAreaCaseDataDto.data,
                            deathsByPublishedDateAreaName = ukAreaPublishedDeathsDataDto.name,
                            deathsByPublishedDate = ukAreaPublishedDeathsDataDto.data,
                            onsDeathAreaName = ukAreaOnsDeathsDataDto.name,
                            onsDeathsByRegistrationDate = ukAreaOnsDeathsDataDto.data,
                            hospitalAdmissionsAreaName = ukAreaDetailDto.areaName,
                            hospitalAdmissions = emptyList(),
                            transmissionRate = null,
                            alertLevel = alertLevel
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest soa data for the area`() =
        runBlocking {
            val msoaCode = "msoa1"
            val msoaAreaLookup = areaLookupDto.copy(
                msoaCode = msoaCode,
                msoaName = "msoa name"
            )
            val soaData = SoaDataModel(
                areaCode = msoaAreaLookup.msoaCode,
                areaName = msoaAreaLookup.msoaName!!,
                areaType = AreaType.MSOA,
                data = emptyList()
            )
            every { areaDataSource.metadataAsFlow(msoaCode) } returns
                listOf(metadata).asFlow()
            every { areaLookupUseCase.areaLookup(any(), any()) } returns msoaAreaLookup
            every {
                soaDataUseCase.byAreaCode(msoaAreaLookup.msoaCode, AreaType.MSOA)
            } returns
                soaData

            val areaDetailModelFlow = sut.execute(
                msoaCode,
                AreaType.MSOA
            )

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            lastUpdatedAt = lastUpdatedDateTime,
                            casesAreaName = ukAreaCaseDataDto.name,
                            cases = ukAreaCaseDataDto.data,
                            deathsByPublishedDateAreaName = ukAreaPublishedDeathsDataDto.name,
                            deathsByPublishedDate = ukAreaPublishedDeathsDataDto.data,
                            onsDeathAreaName = ukAreaOnsDeathsDataDto.name,
                            onsDeathsByRegistrationDate = ukAreaOnsDeathsDataDto.data,
                            hospitalAdmissionsAreaName = ukAreaDetailDto.areaName,
                            hospitalAdmissions = emptyList(),
                            transmissionRate = null,
                            alertLevel = null,
                            soaData = soaData
                        )
                    )
                )
            }
        }

    companion object {

        fun String.toAreaType() = AreaType.from(this)!!

        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 12, 0)
        private val lastUpdatedDateTime = LocalDateTime.of(2020, 1, 1, 11, 0)

        private val metadata =
            MetadataDto(lastUpdatedAt = lastUpdatedDateTime, lastSyncTime = syncDateTime)

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
        private val ukArea =
            AreaDto(Constants.UK_AREA_CODE, Constants.UK_AREA_NAME, AreaType.OVERVIEW)
        private val ukAreaDetailDto = AreaDetailDto(
            areaName = Constants.UK_AREA_NAME,
            areaCode = Constants.UK_AREA_CODE,
            areaType = AreaType.OVERVIEW.value,
            cases = emptyList(),
            deathsByPublishedDate = emptyList(),
            onsDeathsByRegistrationDate = emptyList()
        )
        private val ukAreaDailyDataCollection =
            AreaDailyDataCollection(Constants.UK_AREA_NAME, emptyList())
        private val ukAreaCaseDataDto = AreaDailyDataDto(
            Constants.UK_AREA_NAME, listOf(
                DailyData(
                    10,
                    100,
                    10.0,
                    syncDateTime.toLocalDate()
                )
            )
        )
        private val ukAreaPublishedDeathsDataDto = AreaDailyDataDto(
            Constants.UK_AREA_NAME, listOf(
                DailyData(
                    30,
                    200,
                    30.0,
                    syncDateTime.toLocalDate()
                )
            )
        )
        private val ukAreaOnsDeathsDataDto = AreaDailyDataDto(
            Constants.UK_AREA_NAME, listOf(
                DailyData(
                    40,
                    400,
                    44.0,
                    syncDateTime.toLocalDate()
                )
            )
        )

        private val areaWithCases = ukAreaDetailDto.copy(
            cases = SynchronisationTestData.dailyData()
        )

        private val areaWithDeaths = ukAreaDetailDto.copy(
            deathsByPublishedDate = SynchronisationTestData.dailyData()
        )

        private val areaWithHospitalAdmissions = ukAreaDetailDto

        private val nhsAreaDto = AreaDto("nhsCode", "nhsName", AreaType.NHS_REGION)
        private val nhsTransmissionRate = TransmissionRateModel(
            date = lastUpdatedDateTime.toLocalDate(),
            transmissionRateMin = 1.0,
            transmissionRateMax = 2.0,
            transmissionRateGrowthRateMin = 0.3,
            transmissionRateGrowthRateMax = 0.7
        )
        private val nhsAreaTransmissionRateDto =
            AreaTransmissionRateModel(nhsAreaDto.name, syncDateTime, nhsTransmissionRate)
    }
}
