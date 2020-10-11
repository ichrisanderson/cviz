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

package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.synchronisation.AreaSummary
import com.chrisa.covid19.core.data.synchronisation.AreaSummaryMapper
import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.AreaCaseDto
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.DeathDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.helper.RollingAverageHelper
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
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
    private val rollingAverageHelper = mockk<RollingAverageHelper>()
    private val areaSummaryMapper = mockk<AreaSummaryMapper>()
    private val areaCode = "1234"
    private val sut = AreaDetailUseCase(areaDataSource, rollingAverageHelper, areaSummaryMapper)
    private val now = LocalDateTime.now()
    private val metadataDTO = MetadataDto(lastUpdatedAt = now.minusDays(1), lastSyncTime = now)

    @Before
    fun setup() {
        every { rollingAverageHelper.average(any(), any()) } returns 1.0
        every { areaDataSource.loadAreaMetadata(any()) } returns listOf(metadataDTO).asFlow()
    }

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits with null data`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata(areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow = sut.execute(areaCode)

            areaDetailModelFlow.collect { areaDetailModel ->
                assertThat(areaDetailModel).isEqualTo(
                    AreaDetailModel(
                        lastUpdatedAt = null,
                        weeklyInfectionRate = 0.0,
                        changeInInfectionRate = 0.0,
                        weeklyCases = 0,
                        changeInCases = 0,
                        cumulativeCases = 0,
                        lastSyncedAt = null,
                        allCases = emptyList(),
                        deathsByPublishedDate = emptyList(),
                        deathsByDeathDate = emptyList()
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {
            val caseDTOs = caseDtos()
            val caseModels = caseModels(caseDTOs)
            val lastCase = caseDTOs.last()
            val areaCaseDto = areaCaseDto().copy(cases = caseDTOs)
            val areaSummary = areaSummary(areaCaseDto)

            coEvery { areaDataSource.loadAreaData(any()) } returns areaCaseDto
            every {
                areaSummaryMapper.mapAreaDataToAreaSummary(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns areaSummary

            val areaDetailModelFlow = sut.execute(areaSummary.areaCode)

            areaDetailModelFlow.collect { areaDetailModel ->
                assertThat(areaDetailModel).isEqualTo(
                    AreaDetailModel(
                        lastUpdatedAt = metadataDTO.lastUpdatedAt,
                        weeklyInfectionRate = areaSummary.currentInfectionRate,
                        changeInInfectionRate = areaSummary.changeInInfectionRate,
                        weeklyCases = areaSummary.currentNewCases,
                        changeInCases = areaSummary.changeInCases,
                        cumulativeCases = lastCase.cumulativeCases,
                        lastSyncedAt = metadataDTO.lastSyncTime,
                        allCases = caseModels,
                        deathsByPublishedDate = emptyList(),
                        deathsByDeathDate = emptyList()
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest deaths for the area`() =
        runBlocking {
            val deaths = deathDtos()
            val deathModels = deathModels(deaths)
            val areaCaseDto = areaCaseDto().copy(
                deathsByPublishedDate = deaths,
                deathsByDeathDate = deaths
            )
            val areaSummary = areaSummary(areaCaseDto)

            every { areaDataSource.loadAreaMetadata(any()) } returns listOf(metadataDTO).asFlow()
            coEvery { areaDataSource.loadAreaData(any()) } returns areaCaseDto
            every {
                areaSummaryMapper.mapAreaDataToAreaSummary(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns areaSummary

            val areaDetailModelFlow = sut.execute(areaSummary.areaCode)

            areaDetailModelFlow.collect { areaDetailModel ->
                assertThat(areaDetailModel).isEqualTo(
                    AreaDetailModel(
                        lastUpdatedAt = metadataDTO.lastUpdatedAt,
                        weeklyInfectionRate = areaSummary.currentInfectionRate,
                        changeInInfectionRate = areaSummary.changeInInfectionRate,
                        weeklyCases = areaSummary.currentNewCases,
                        changeInCases = areaSummary.changeInCases,
                        cumulativeCases = 0,
                        lastSyncedAt = metadataDTO.lastSyncTime,
                        allCases = emptyList(),
                        deathsByPublishedDate = deathModels,
                        deathsByDeathDate = deathModels
                    )
                )
            }
        }

    private fun areaSummary(areaCaseDto: AreaCaseDto): AreaSummary {
        return AreaSummary(
            areaCode = areaCaseDto.areaCode,
            areaName = areaCaseDto.areaName,
            areaType = areaCaseDto.areaType,
            changeInCases = 100,
            currentNewCases = 10,
            currentInfectionRate = 10.0,
            changeInInfectionRate = 110.0
        )
    }

    private fun areaCaseDto(): AreaCaseDto {
        return AreaCaseDto(
            areaCode = "1234",
            areaName = "Woking",
            areaType = AreaType.LTLA.value,
            cases = emptyList(),
            deathsByPublishedDate = emptyList(),
            deathsByDeathDate = emptyList()
        )
    }

    private fun caseDtos(): List<CaseDto> {
        var cumulativeCases = 0
        return (1 until 100).map {
            cumulativeCases += it
            CaseDto(
                newCases = it,
                cumulativeCases = cumulativeCases,
                date = LocalDate.ofEpochDay(it.toLong()),
                infectionRate = 0.0,
                baseRate = 0.0
            )
        }
    }

    private fun caseModels(caseDTOs: List<CaseDto>): List<CaseModel> {
        return caseDTOs.map {
            CaseModel(
                newCases = it.newCases,
                date = it.date,
                rollingAverage = 1.0,
                cumulativeCases = it.cumulativeCases,
                baseRate = 0.0
            )
        }
    }

    private fun deathDtos(): List<DeathDto> {
        var cumulativeDeaths = 0
        return (1 until 100).map {
            cumulativeDeaths += it
            DeathDto(
                newDeaths = it,
                cumulativeDeaths = cumulativeDeaths,
                date = LocalDate.ofEpochDay(it.toLong()),
                deathRate = 0.0,
                baseRate = 0.0
            )
        }
    }

    private fun deathModels(caseDTOs: List<DeathDto>): List<DeathModel> {
        return caseDTOs.map {
            DeathModel(
                newDeaths = it.newDeaths,
                date = it.date,
                rollingAverage = 1.0,
                cumulativeDeaths = it.cumulativeDeaths,
                baseRate = 0.0
            )
        }
    }
}
