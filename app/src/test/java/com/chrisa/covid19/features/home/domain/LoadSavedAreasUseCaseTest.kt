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

package com.chrisa.covid19.features.home.domain

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.synchronisation.AreaData
import com.chrisa.covid19.core.data.synchronisation.AreaSummary
import com.chrisa.covid19.core.data.synchronisation.AreaSummaryMapper
import com.chrisa.covid19.features.home.data.HomeDataSource
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.SummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class LoadSavedAreasUseCaseTest {

    private val areaSummaryMapper = mockk<AreaSummaryMapper>()
    private val homeDataSource = mockk<HomeDataSource>()
    private val sut = LoadSavedAreasUseCase(homeDataSource, areaSummaryMapper)

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {

            val areaName = "UK"
            val areaCode = Constants.UK_AREA_CODE
            val areaType = AreaType.OVERVIEW

            val savedAreaCaseDto = SavedAreaCaseDto(
                areaName = areaName,
                areaCode = areaCode,
                areaType = areaType,
                newCases = 10,
                cumulativeCases = 100,
                infectionRate = 10.0,
                date = LocalDate.now()
            )
            val areaSummary = AreaSummary(
                areaName = areaName,
                areaCode = areaCode,
                areaType = areaType.value,
                currentNewCases = 100,
                changeInCases = 10,
                currentInfectionRate = 40.0,
                changeInInfectionRate = 10.0
            )
            val newCases = listOf(savedAreaCaseDto)
            every {
                areaSummaryMapper.mapAreaDataToAreaSummary(
                    any(),
                    any(),
                    any(),
                    allCases = newCases.map {
                        AreaData(
                            newCases = savedAreaCaseDto.newCases,
                            cumulativeCases = savedAreaCaseDto.cumulativeCases,
                            infectionRate = savedAreaCaseDto.infectionRate,
                            date = savedAreaCaseDto.date
                        )
                    }
                )
            } returns areaSummary
            every { homeDataSource.savedAreaCases() } returns listOf(newCases).asFlow()

            val emittedItems = mutableListOf<List<SummaryModel>>()
            sut.execute().collect { emittedItems.add(it) }

            val summaryModels = emittedItems.first()

            assertThat(summaryModels)
                .isEqualTo(
                    listOf(
                        SummaryModel(
                            position = 1,
                            areaCode = areaSummary.areaCode,
                            areaName = areaSummary.areaName,
                            areaType = areaSummary.areaType,
                            changeInCases = areaSummary.changeInCases,
                            currentNewCases = areaSummary.currentNewCases,
                            currentInfectionRate = areaSummary.currentInfectionRate,
                            changeInInfectionRate = areaSummary.changeInInfectionRate
                        )
                    )
                )
        }
}
