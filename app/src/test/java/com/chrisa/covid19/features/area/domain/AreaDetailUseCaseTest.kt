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
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.synchronisation.WeeklySummary
import com.chrisa.covid19.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.DeathDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.helper.RollingAverageHelper
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
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
    private val areaSummaryMapper = mockk<WeeklySummaryBuilder>()
    private val sut = AreaDetailUseCase(areaDataSource, rollingAverageHelper, areaSummaryMapper)

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits with null data`() =
        runBlocking {
            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(area.areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow = sut.execute(area.areaCode)

            areaDetailModelFlow.collect { emittedAreaDetailModel ->
                assertThat(emittedAreaDetailModel).isEqualTo(emptyAreaDetailModel)
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {
            with(areaWithCases) {
                every { rollingAverageHelper.average(any(), any()) } returns 1.0
                every { areaDataSource.loadAreaMetadata(areaCode) } returns listOf(metadata).asFlow()
                coEvery { areaDataSource.loadAreaData(areaCode) } returns areaDetail
                every { areaSummaryMapper.buildWeeklySummary(any()) } returns weeklySummary

                val areaDetailModelFlow = sut.execute(areaCode)

                areaDetailModelFlow.collect { emittedAreaDetailModel ->
                    assertThat(emittedAreaDetailModel).isEqualTo(areaDetailModel)
                }
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest deaths for the area`() =
        runBlocking {
            with(areaWithDeaths) {
                every { rollingAverageHelper.average(any(), any()) } returns 1.0
                every { areaDataSource.loadAreaMetadata(areaCode) } returns listOf(metadata).asFlow()
                coEvery { areaDataSource.loadAreaData(areaCode) } returns areaDetail
                every { areaSummaryMapper.buildWeeklySummary(any()) } returns weeklySummary

                val areaDetailModelFlow = sut.execute(areaCode)

                areaDetailModelFlow.collect { emittedAreaDetailModel ->
                    assertThat(emittedAreaDetailModel).isEqualTo(areaDetailModel)
                }
            }
        }

    companion object {

        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val emptyWeeklySummary =
            WeeklySummary(
                weeklyTotal = 0,
                changeInTotal = 0,
                weeklyRate = 0.0,
                changeInRate = 0.0
            )

        private val emptyAreaDetailModel =
            AreaDetailModel(
                areaType = null,
                lastUpdatedAt = null,
                lastSyncedAt = null,
                cumulativeCases = 0,
                newCases = 0,
                allCases = emptyList(),
                weeklyCaseSummary = emptyWeeklySummary,
                deathsByPublishedDate = emptyList(),
                weeklyDeathSummary = emptyWeeklySummary
            )

        private val area = AreaDetailTestData(
            metadata = MetadataDto(lastUpdatedAt = syncDate.minusDays(1), lastSyncTime = syncDate),
            areaName = "United Kingdom",
            areaCode = Constants.UK_AREA_CODE,
            areaType = AreaType.OVERVIEW,
            cases = emptyList(),
            weeklySummary = WeeklySummary(
                weeklyTotal = 1000,
                changeInTotal = 10,
                weeklyRate = 100.0,
                changeInRate = 20.0
            ),
            deaths = emptyList()
        )

        private val areaWithCases = area.copy(
            cases = caseDtos()
        )

        private val areaWithDeaths = area.copy(
            deaths = deathDtos()
        )

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
    }
}
