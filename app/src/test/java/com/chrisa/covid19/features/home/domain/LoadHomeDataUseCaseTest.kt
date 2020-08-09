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

import com.chrisa.covid19.features.home.data.HomeDataSource
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.MetadataDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.helpers.PastTwoWeekCaseBreakdownHelper
import com.chrisa.covid19.features.home.domain.helpers.WeeklyCaseDifferenceHelper
import com.chrisa.covid19.features.home.domain.models.HomeScreenDataModel
import com.chrisa.covid19.features.home.domain.models.LatestUkData
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

    private val homeDataSource = mockk<HomeDataSource>()
    private val pastTwoWeekCaseBreakdownHelper =
        PastTwoWeekCaseBreakdownHelper()
    private val weeklyCaseDifferenceHelper =
        WeeklyCaseDifferenceHelper()

    private val sut = LoadHomeDataUseCase(
        homeDataSource,
        pastTwoWeekCaseBreakdownHelper,
        weeklyCaseDifferenceHelper
    )

    @Test
    fun `WHEN execute called THEN daily record list is emitted`() =
        runBlockingTest {

            val areaName = "United Kingdom"

            val metadataDto = MetadataDto(
                lastUpdatedAt = LocalDateTime.of(2020, 5, 6, 1, 1)
            )

            val dailyRecordDto = DailyRecordDto(
                areaName = areaName,
                totalLabConfirmedCases = 122,
                dailyLabConfirmedCases = 22,
                date = LocalDate.of(2020, 5, 6)
            )

            val dailyRecords = listOf(dailyRecordDto)

            every { homeDataSource.overviewMetadata() } returns listOf(metadataDto).asFlow()
            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(emptyList<SavedAreaCaseDto>()).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            val homeScreenDataModel = emittedItems.first()

            sut.execute().collect { emittedItems.add(it) }

            val expectedItems = dailyRecords.map {
                LatestUkData(
                    areaName = it.areaName,
                    dailyLabConfirmedCases = it.dailyLabConfirmedCases,
                    totalLabConfirmedCases = it.totalLabConfirmedCases,
                    lastUpdated = metadataDto.lastUpdatedAt
                )
            }

            assertThat(homeScreenDataModel.latestUkData).isEqualTo(expectedItems.takeLast(1).first())
        }

    @Test
    fun `GIVEN less than a weeks worth of cases WHEN execute called THEN area case list is created with figures from past week`() =
        runBlockingTest {

            val now = LocalDate
                .from(OffsetDateTime.now(ZoneOffset.UTC))

            val random = Random(0)
            val areaCode = "001"
            val areaName = "UK"
            val areaType = "utla"

            val latestWeekData = buildWeeklyData(
                now.minusDays(7),
                0,
                random,
                areaCode,
                areaName,
                areaType
            )

            val metadataDto = MetadataDto(
                lastUpdatedAt = LocalDateTime.of(2020, 5, 6, 1, 1)
            )

            val dailyRecordDto = DailyRecordDto(
                areaName = "United Kingdom",
                dailyLabConfirmedCases = 111,
                totalLabConfirmedCases = 111,
                date = LocalDate.of(2020, 1, 1)
            )
            val dailyRecords = listOf(dailyRecordDto)

            every { homeDataSource.overviewMetadata() } returns listOf(metadataDto).asFlow()
            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(latestWeekData.cases).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val homeScreenDataModel = emittedItems.first()
            val emittedAreaCaseList = homeScreenDataModel.savedAreas
            val lastCaseInLatestWeek = latestWeekData.cases.last()

            assertThat(emittedAreaCaseList.first()).isEqualTo(
                SavedAreaModel(
                    areaCode = lastCaseInLatestWeek.areaCode,
                    areaName = lastCaseInLatestWeek.areaName,
                    areaType = lastCaseInLatestWeek.areaType,
                    totalLabConfirmedCases = lastCaseInLatestWeek.totalLabConfirmedCases,
                    dailyTotalLabConfirmedCasesRate = lastCaseInLatestWeek.dailyTotalLabConfirmedCasesRate,
                    changeInDailyTotalLabConfirmedCasesRate = lastCaseInLatestWeek.dailyTotalLabConfirmedCasesRate,
                    changeInTotalLabConfirmedCases = lastCaseInLatestWeek.totalLabConfirmedCases,
                    totalLabConfirmedCasesLastWeek = lastCaseInLatestWeek.totalLabConfirmedCases
                )
            )
        }

    @Test
    fun `GIVEN more than a weeks worth of cases WHEN execute called THEN area case list is created with figures from combined weeks`() =
        runBlockingTest {

            val now = LocalDate
                .from(OffsetDateTime.now(ZoneOffset.UTC))

            val random = Random(0)
            val areaCode = "001"
            val areaName = "UK"
            val areaType = "UK"

            val previousWeekData = buildWeeklyData(
                now.minusDays(14),
                0,
                random,
                areaCode,
                areaName,
                areaType
            )

            val latestWeekData = buildWeeklyData(
                now.minusDays(7),
                previousWeekData.cumulativeTotalCases,
                random,
                areaCode,
                areaName,
                areaType
            )

            val allCases = previousWeekData.cases + latestWeekData.cases

            val metadataDto = MetadataDto(
                lastUpdatedAt = LocalDateTime.of(2020, 5, 6, 1, 1)
            )

            val dailyRecordDto = DailyRecordDto(
                areaName = "United Kingdom",
                dailyLabConfirmedCases = 111,
                totalLabConfirmedCases = 111,
                date = LocalDate.of(2020, 1, 1)
            )
            val dailyRecords = listOf(dailyRecordDto)

            every { homeDataSource.overviewMetadata() } returns listOf(metadataDto).asFlow()
            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(allCases).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val homeScreenDataModel = emittedItems.first()
            val emittedAreaCaseList = homeScreenDataModel.savedAreas
            val lastCaseThisWeek = latestWeekData.cases.last()
            val lastCasePreviousWeek = previousWeekData.cases.last()

            assertThat(emittedAreaCaseList.first()).isEqualTo(
                SavedAreaModel(
                    areaCode = lastCaseThisWeek.areaCode,
                    areaName = lastCaseThisWeek.areaName,
                    areaType = lastCaseThisWeek.areaType,
                    totalLabConfirmedCases = lastCaseThisWeek.totalLabConfirmedCases,
                    dailyTotalLabConfirmedCasesRate = lastCaseThisWeek.dailyTotalLabConfirmedCasesRate,
                    changeInDailyTotalLabConfirmedCasesRate = lastCaseThisWeek.dailyTotalLabConfirmedCasesRate - lastCasePreviousWeek.dailyTotalLabConfirmedCasesRate,
                    changeInTotalLabConfirmedCases = latestWeekData.totalCasesInWeek - previousWeekData.totalCasesInWeek,
                    totalLabConfirmedCasesLastWeek = latestWeekData.totalCasesInWeek
                )
            )
        }

    @Test
    fun `GIVEN multiple saved area cases WHEN execute called THEN area case list is sorted by area name`() =
        runBlockingTest {

            val now = LocalDate
                .from(OffsetDateTime.now(ZoneOffset.UTC))

            val random = Random(0)

            val wokingCases = buildWeeklyAreaData(
                now,
                random,
                "A002",
                areaName = "Woking",
                areaType = "utla"
            )

            val aldershotCases = buildWeeklyAreaData(
                now,
                random,
                "A001",
                areaName = "Aldersot",
                areaType = "utla"
            )

            val allCases =
                wokingCases.weekOne.cases + wokingCases.weekTwo.cases + aldershotCases.weekOne.cases + aldershotCases.weekTwo.cases

            val metadataDto = MetadataDto(
                lastUpdatedAt = LocalDateTime.of(2020, 5, 6, 1, 1)
            )

            val dailyRecordDto = DailyRecordDto(
                areaName = "United Kingdom",
                dailyLabConfirmedCases = 111,
                totalLabConfirmedCases = 111,
                date = LocalDate.of(2020, 1, 1)
            )
            val dailyRecords = listOf(dailyRecordDto)

            every { homeDataSource.overviewMetadata() } returns listOf(metadataDto).asFlow()
            every { homeDataSource.ukOverview() } returns listOf(dailyRecords).asFlow()
            every { homeDataSource.savedAreaCases() } returns listOf(allCases).asFlow()

            val emittedItems = mutableListOf<HomeScreenDataModel>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val homeScreenDataModel = emittedItems.first()
            val emittedAreaCaseList = homeScreenDataModel.savedAreas

            val lastWokingCaseThisWeek = wokingCases.weekTwo.cases.last()
            val lastWokingCasePreviousWeek = wokingCases.weekOne.cases.last()

            val lastAldershotCaseThisWeek = aldershotCases.weekTwo.cases.last()
            val lastAldershotCasePreviousWeek = aldershotCases.weekOne.cases.last()

            assertThat(emittedAreaCaseList[0]).isEqualTo(
                SavedAreaModel(
                    areaCode = lastAldershotCaseThisWeek.areaCode,
                    areaName = lastAldershotCaseThisWeek.areaName,
                    areaType = lastAldershotCaseThisWeek.areaType,
                    totalLabConfirmedCases = lastAldershotCaseThisWeek.totalLabConfirmedCases,
                    dailyTotalLabConfirmedCasesRate = lastAldershotCaseThisWeek.dailyTotalLabConfirmedCasesRate,
                    changeInDailyTotalLabConfirmedCasesRate = lastAldershotCaseThisWeek.dailyTotalLabConfirmedCasesRate - lastAldershotCasePreviousWeek.dailyTotalLabConfirmedCasesRate,
                    changeInTotalLabConfirmedCases = aldershotCases.weekTwo.totalCasesInWeek - aldershotCases.weekOne.totalCasesInWeek,
                    totalLabConfirmedCasesLastWeek = aldershotCases.weekTwo.totalCasesInWeek
                )
            )
            assertThat(emittedAreaCaseList[1]).isEqualTo(
                SavedAreaModel(
                    areaCode = lastWokingCaseThisWeek.areaCode,
                    areaName = lastWokingCaseThisWeek.areaName,
                    areaType = lastWokingCaseThisWeek.areaType,
                    totalLabConfirmedCases = lastWokingCaseThisWeek.totalLabConfirmedCases,
                    dailyTotalLabConfirmedCasesRate = lastWokingCaseThisWeek.dailyTotalLabConfirmedCasesRate,
                    changeInDailyTotalLabConfirmedCasesRate = lastWokingCaseThisWeek.dailyTotalLabConfirmedCasesRate - lastWokingCasePreviousWeek.dailyTotalLabConfirmedCasesRate,
                    changeInTotalLabConfirmedCases = wokingCases.weekTwo.totalCasesInWeek - wokingCases.weekOne.totalCasesInWeek,
                    totalLabConfirmedCasesLastWeek = wokingCases.weekTwo.totalCasesInWeek
                )
            )
        }

    private fun buildWeeklyAreaData(
        startDate: LocalDate,
        random: Random,
        areaCode: String,
        areaName: String,
        areaType: String
    ): WeeklyAreaData {

        val previousWeekData = buildWeeklyData(
            startDate.minusDays(14),
            0,
            random,
            areaCode,
            areaName,
            areaType
        )

        val latestWeekData = buildWeeklyData(
            startDate.minusDays(7),
            previousWeekData.cumulativeTotalCases,
            random,
            areaCode,
            areaName,
            areaType
        )

        return WeeklyAreaData(
            previousWeekData,
            latestWeekData
        )
    }

    data class WeeklyAreaData(
        val weekOne: SavedAreaCaseDtoWrapper,
        val weekTwo: SavedAreaCaseDtoWrapper
    )

    private fun buildWeeklyData(
        startDate: LocalDate,
        startTotalCases: Int,
        random: Random,
        areaCode: String,
        areaName: String,
        areaType: String
    ): SavedAreaCaseDtoWrapper {

        var cumulativeTotal = startTotalCases
        var caseNumber = startTotalCases + 1

        val cases = (0 until 7).map {

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

        return SavedAreaCaseDtoWrapper(
            cumulativeTotalCases = cumulativeTotal,
            totalCasesInWeek = cumulativeTotal - startTotalCases,
            cases = cases
        )
    }

    data class SavedAreaCaseDtoWrapper(
        val cumulativeTotalCases: Int,
        val totalCasesInWeek: Int,
        val cases: List<SavedAreaCaseDto>
    )
}
