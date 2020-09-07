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

import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.features.home.data.HomeDataSource
import com.chrisa.covid19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.HomeScreenDataModel
import com.chrisa.covid19.features.home.domain.models.LatestUkDataModel
import com.chrisa.covid19.features.home.domain.models.NewCaseModel
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
@FlowPreview
class LoadHomeDataUseCaseTest {

    private val savedAreaModelMapper = mockk<SavedAreaModelMapper>()
    private val homeDataSource = mockk<HomeDataSource>()

    private val sut = LoadHomeDataUseCase(homeDataSource, savedAreaModelMapper)

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {

            val dailyRecordDto = DailyRecordDto(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW.value,
                areaName = "United Kingdom",
                totalLabConfirmedCases = 122,
                dailyLabConfirmedCases = 22,
                lastUpdated = LocalDateTime.of(2020, 5, 6, 1, 1)
            )

            val dailyRecords = listOf(dailyRecordDto)

            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()
            every { homeDataSource.areaSummaries() } returns listOf(emptyList<AreaSummaryDto>()).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(emptyList<SavedAreaCaseDto>()).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            val homeScreenDataModel = emittedItems.first()

            sut.execute().collect { emittedItems.add(it) }

            val expectedItems = dailyRecords.map {
                LatestUkDataModel(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = it.areaType,
                    dailyLabConfirmedCases = it.dailyLabConfirmedCases,
                    totalLabConfirmedCases = it.totalLabConfirmedCases,
                    lastUpdated = it.lastUpdated
                )
            }

            assertThat(homeScreenDataModel.latestUkData).isEqualTo(expectedItems)
        }

    @Test
    fun `WHEN execute called THEN new cases list is emitted`() =
        runBlockingTest {

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

            every { homeDataSource.areaSummaries() } returns listOf(newCases).asFlow()
            every { homeDataSource.ukOverview() } returns listOf(emptyList<DailyRecordDto>()).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(emptyList<SavedAreaCaseDto>()).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            val homeScreenDataModel = emittedItems.first()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(homeScreenDataModel.topNewCases).isEqualTo(newCases.mapIndexed { index, areaSummary ->
                NewCaseModel(
                    position = index + 1,
                    areaCode = areaSummary.areaCode,
                    areaName = areaSummary.areaName,
                    areaType = areaSummary.areaType,
                    changeInCases = areaSummary.changeInCases,
                    currentNewCases = areaSummary.currentNewCases
                )
            })
        }

    @Test
    fun `GIVEN multiple saved area cases WHEN execute called THEN area case list is sorted by area name`() =
        runBlockingTest {

            val now = LocalDate
                .from(OffsetDateTime.now(ZoneOffset.UTC))

            val random = Random(0)

            val wokingArea = AreaEntity(
                areaCode = "A002",
                areaName = "Woking",
                areaType = AreaType.LTLA
            )

            val wokingCases = buildCases(
                now,
                100,
                30,
                random,
                wokingArea.areaCode,
                wokingArea.areaName,
                wokingArea.areaType.value
            )

            val wokingSavedAreaModel = SavedAreaModel(
                areaCode = wokingArea.areaCode,
                areaName = wokingArea.areaName,
                areaType = wokingArea.areaType.value,
                totalLabConfirmedCases = 100,
                changeInTotalLabConfirmedCases = 1000,
                totalLabConfirmedCasesLastWeek = 100
            )

            val aldershotArea = AreaEntity(
                areaCode = "A001",
                areaName = "Aldersot",
                areaType = AreaType.LTLA
            )
            val aldershotCases = buildCases(
                now,
                50,
                30,
                random,
                aldershotArea.areaCode,
                aldershotArea.areaName,
                aldershotArea.areaType.value
            )

            val aldershotSavedAreaModel = SavedAreaModel(
                areaCode = aldershotArea.areaCode,
                areaName = aldershotArea.areaName,
                areaType = aldershotArea.areaType.value,
                totalLabConfirmedCases = 100,
                changeInTotalLabConfirmedCases = 1000,
                totalLabConfirmedCasesLastWeek = 100
            )

            val allCases = wokingCases + aldershotCases

            every {
                savedAreaModelMapper.mapSavedAreaModel(
                    aldershotArea.areaCode,
                    aldershotArea.areaName,
                    aldershotCases
                )
            } returns aldershotSavedAreaModel
            every {
                savedAreaModelMapper.mapSavedAreaModel(
                    wokingArea.areaCode,
                    wokingArea.areaName,
                    wokingCases
                )
            } returns wokingSavedAreaModel
            every { homeDataSource.ukOverview() } returns listOf(emptyList<DailyRecordDto>()).asFlow()
            every { homeDataSource.areaSummaries() } returns listOf(emptyList<AreaSummaryDto>()).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(allCases).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val homeScreenDataModel = emittedItems.first()
            val emittedAreaCaseList = homeScreenDataModel.savedAreas

            assertThat(emittedAreaCaseList[0]).isEqualTo(aldershotSavedAreaModel)
            assertThat(emittedAreaCaseList[1]).isEqualTo(wokingSavedAreaModel)
        }

    private fun buildCases(
        startDate: LocalDate,
        startTotalCases: Int,
        numberOfCases: Int,
        random: Random,
        areaCode: String,
        areaName: String,
        areaType: String
    ): List<SavedAreaCaseDto> {

        var cumulativeTotal = startTotalCases
        var caseNumber = startTotalCases + 1

        val cases = (0 until numberOfCases).map {

            val dailyLabConfirmedCases = random.nextInt(15)
            cumulativeTotal += dailyLabConfirmedCases

            SavedAreaCaseDto(
                areaCode = areaCode,
                areaName = areaName,
                areaType = areaType,
                date = startDate.plusDays(it.toLong()),
                dailyLabConfirmedCases = dailyLabConfirmedCases,
                totalLabConfirmedCases = cumulativeTotal,
                dailyTotalLabConfirmedCasesRate = cumulativeTotal.toDouble() / caseNumber++
            )
        }

        return cases
    }
}
