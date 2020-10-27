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

package com.chrisa.cron19.features.home.domain

import com.chrisa.cron19.core.data.db.AreaType
import com.chrisa.cron19.core.data.db.Constants
import com.chrisa.cron19.features.home.data.HomeDataSource
import com.chrisa.cron19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cron19.features.home.domain.models.SortOption
import com.chrisa.cron19.features.home.domain.models.SummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
class LoadAreaSummariesUseCaseTest {

    private val homeDataSource = mockk<HomeDataSource>()
    private val areaSummaryListSorter = mockk<AreaSummaryListSorter>()
    private val sut = LoadAreaSummariesUseCase(homeDataSource, areaSummaryListSorter)

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {
            val sortOption = SortOption.RisingCases
            val newCaseDto = AreaSummaryDto(
                areaName = "UK",
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW.value,
                changeInCases = 10,
                currentNewCases = 100,
                changeInInfectionRate = 10.0,
                currentInfectionRate = 100.0
            )
            val newCases = listOf(newCaseDto)
            every { areaSummaryListSorter.sort(newCases, sortOption) } returns newCases
            every { homeDataSource.areaSummaries() } returns listOf(newCases).asFlow()

            val emittedItems = mutableListOf<List<SummaryModel>>()
            sut.execute(sortOption).collect { emittedItems.add(it) }

            val summaryModels = emittedItems.first()

            assertThat(summaryModels)
                .isEqualTo(newCases.mapIndexed { index, areaSummary ->
                    SummaryModel(
                        position = index + 1,
                        areaCode = areaSummary.areaCode,
                        areaName = areaSummary.areaName,
                        areaType = areaSummary.areaType,
                        changeInCases = areaSummary.changeInCases,
                        currentNewCases = areaSummary.currentNewCases,
                        changeInInfectionRate = areaSummary.changeInInfectionRate,
                        currentInfectionRate = areaSummary.currentInfectionRate
                    )
                })
        }
}
