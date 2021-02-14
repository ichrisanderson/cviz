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

package com.chrisa.cviz.features.home.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cviz.features.home.data.HomeDataSource
import com.chrisa.cviz.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.cviz.features.home.data.dtos.SavedSoaDataDto
import com.chrisa.cviz.features.home.domain.models.SavedAreaSummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class LoadSavedAreasUseCaseTest {

    private val areaSummaryMapper = mockk<WeeklySummaryBuilder>()
    private val homeDataSource = mockk<HomeDataSource>()
    private val sut = LoadSavedAreasUseCase(homeDataSource, areaSummaryMapper)

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {
            val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)
            val ukAreaCaseDto = SavedAreaCaseDto(
                areaCode = Constants.UK_AREA_CODE,
                areaName = Constants.UK_AREA_NAME,
                areaType = AreaType.OVERVIEW,
                newCases = 10,
                cumulativeCases = 100,
                infectionRate = 10.0,
                date = syncDate.toLocalDate()
            )
            val englandAreaCaseDto = SavedAreaCaseDto(
                areaName = Constants.ENGLAND_AREA_NAME,
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaType = AreaType.NATION,
                newCases = 10,
                cumulativeCases = 100,
                infectionRate = 10.0,
                date = syncDate.toLocalDate()
            )
            val soaAreaCaseDto = SavedSoaDataDto(
                areaName = "Westminister",
                areaCode = "E1234",
                areaType = AreaType.MSOA,
                rollingSum = 11,
                rollingRate = 122.0,
                date = syncDate.toLocalDate()
            )
            val weeklySummary = WeeklySummary(
                lastDate = syncDate.toLocalDate(),
                currentTotal = 12220,
                dailyTotal = 320,
                weeklyTotal = 100,
                changeInTotal = 10,
                weeklyRate = 40.0,
                changeInRate = 10.0
            )
            val newCases = listOf(ukAreaCaseDto, englandAreaCaseDto)
            val newSoaData = listOf(soaAreaCaseDto)
            every { areaSummaryMapper.buildWeeklySummary(any()) } returns weeklySummary
            every { homeDataSource.savedAreaCases() } returns listOf(newCases).asFlow()
            every { homeDataSource.savedSoaData() } returns listOf(newSoaData).asFlow()
            val emittedItems = mutableListOf<List<SavedAreaSummaryModel>>()

            sut.execute().collect { emittedItems.add(it) }

            val summaryModels = emittedItems.first()

            assertThat(summaryModels)
                .isEqualTo(
                    listOf(
                        SavedAreaSummaryModel(
                            areaCode = englandAreaCaseDto.areaCode,
                            areaName = englandAreaCaseDto.areaName,
                            areaType = englandAreaCaseDto.areaType.value,
                            changeInCases = weeklySummary.changeInTotal,
                            currentNewCases = weeklySummary.weeklyTotal,
                            currentInfectionRate = weeklySummary.weeklyRate,
                            changeInInfectionRate = weeklySummary.changeInRate
                        ),
                        SavedAreaSummaryModel(
                            areaCode = ukAreaCaseDto.areaCode,
                            areaName = ukAreaCaseDto.areaName,
                            areaType = ukAreaCaseDto.areaType.value,
                            changeInCases = weeklySummary.changeInTotal,
                            currentNewCases = weeklySummary.weeklyTotal,
                            currentInfectionRate = weeklySummary.weeklyRate,
                            changeInInfectionRate = weeklySummary.changeInRate
                        ),
                        SavedAreaSummaryModel(
                            areaCode = soaAreaCaseDto.areaCode,
                            areaName = soaAreaCaseDto.areaName,
                            areaType = soaAreaCaseDto.areaType.value,
                            changeInCases = soaAreaCaseDto.rollingSum,
                            currentNewCases = soaAreaCaseDto.rollingSum,
                            currentInfectionRate = soaAreaCaseDto.rollingRate,
                            changeInInfectionRate = soaAreaCaseDto.rollingRate
                        )
                    )
                )
        }
}
