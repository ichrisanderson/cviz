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
import com.chrisa.cron19.core.data.synchronisation.WeeklySummary
import com.chrisa.cron19.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cron19.features.home.data.HomeDataSource
import com.chrisa.cron19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.cron19.features.home.domain.models.SummaryModel
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
            val savedAreaCaseDto = SavedAreaCaseDto(
                areaName = "UK",
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                newCases = 10,
                cumulativeCases = 100,
                infectionRate = 10.0,
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
            val newCases = listOf(savedAreaCaseDto)
            every { areaSummaryMapper.buildWeeklySummary(any()) } returns weeklySummary
            every { homeDataSource.savedAreaCases() } returns listOf(newCases).asFlow()
            val emittedItems = mutableListOf<List<SummaryModel>>()

            sut.execute().collect { emittedItems.add(it) }

            val summaryModels = emittedItems.first()

            assertThat(summaryModels)
                .isEqualTo(
                    listOf(
                        SummaryModel(
                            position = 1,
                            areaCode = savedAreaCaseDto.areaCode,
                            areaName = savedAreaCaseDto.areaName,
                            areaType = savedAreaCaseDto.areaType.value,
                            changeInCases = weeklySummary.changeInTotal,
                            currentNewCases = weeklySummary.weeklyTotal,
                            currentInfectionRate = weeklySummary.weeklyRate,
                            changeInInfectionRate = weeklySummary.changeInRate
                        )
                    )
                )
        }
}
