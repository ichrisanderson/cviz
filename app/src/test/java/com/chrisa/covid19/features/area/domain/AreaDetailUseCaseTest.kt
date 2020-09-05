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

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.helper.RollingAverageHelper
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
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
import org.junit.Test

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AreaDetailUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()
    private val rollingAverageHelper = mockk<RollingAverageHelper>()
    private val caseChangeModelMapper = mockk<CaseChangeModelMapper>()
    private val sut = AreaDetailUseCase(areaDataSource, rollingAverageHelper, caseChangeModelMapper)

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits with null data`() =
        runBlocking {

            val areaCode = "1234"
            every { areaDataSource.loadAreaMetadata(areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow = sut.execute(areaCode)

            areaDetailModelFlow.collect { areaDetailModel ->
                assertThat(areaDetailModel).isEqualTo(
                    AreaDetailModel(
                        lastUpdatedAt = null,
                        lastSyncedAt = null,
                        allCases = emptyList(),
                        latestCases = emptyList(),
                        changeInNewCasesThisWeek = 0,
                        currentNewCases = 0,
                        currentInfectionRate = 0.0,
                        changeInInfectionRatesThisWeek = 0.0
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {

            val areaCode = "1234"
            val now = LocalDateTime.now()
            val metadataDTO = MetadataDto(
                lastUpdatedAt = now.minusDays(1),
                lastSyncTime = now
            )

            var totalLabConfirmedCases = 0
            val caseDTOs = (1 until 100).map {
                totalLabConfirmedCases += it
                CaseDto(
                    newCases = it,
                    cumulativeCases = totalLabConfirmedCases,
                    date = LocalDate.ofEpochDay(it.toLong()),
                    infectionRate = 30.0,
                    baseRate = 0.4
                )
            }

            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(areaCode) } returns listOf(metadataDTO).asFlow()
            coEvery { areaDataSource.loadAreaData(areaCode) } returns caseDTOs

            val caseModels = caseDTOs.map {
                CaseModel(
                    newCases = it.newCases,
                    date = it.date,
                    rollingAverage = 1.0,
                    cumulativeCases = it.cumulativeCases,
                    baseRate = 0.4
                )
            }

            val caseChangeModel = CaseChangeModel(
                changeInNewCasesThisWeek = 0,
                currentNewCases = 0,
                cumulativeCases = 0,
                currentInfectionRate = 0.0,
                changeInInfectionRatesThisWeek = 0.0
            )
            every { caseChangeModelMapper.mapSavedAreaModel(caseModels) } returns caseChangeModel

            val areaDetailModelFlow = sut.execute(areaCode)

            areaDetailModelFlow.collect { areaDetailModel ->
                assertThat(areaDetailModel).isEqualTo(
                    AreaDetailModel(
                        lastUpdatedAt = metadataDTO.lastUpdatedAt,
                        lastSyncedAt = metadataDTO.lastSyncTime,
                        allCases = caseModels,
                        latestCases = caseModels.takeLast(14),
                        changeInNewCasesThisWeek = caseChangeModel.changeInNewCasesThisWeek,
                        currentNewCases = caseChangeModel.currentNewCases,
                        currentInfectionRate = caseChangeModel.currentInfectionRate,
                        changeInInfectionRatesThisWeek = caseChangeModel.changeInInfectionRatesThisWeek
                    )
                )
            }
        }
}
