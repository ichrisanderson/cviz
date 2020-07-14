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
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.helpers.PastTwoWeekCaseBreakdownHelper
import com.chrisa.covid19.features.home.domain.helpers.WeeklyCaseDifferenceHelper
import com.chrisa.covid19.features.home.domain.models.AreaCaseListModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class LoadSavedAreaCasesUseCaseTest {

    private val pastTwoWeekCaseBreakdownHelper =
        PastTwoWeekCaseBreakdownHelper()
    private val weeklyCaseDifferenceHelper =
        WeeklyCaseDifferenceHelper()
    private val homeDataSource = mockk<HomeDataSource>()

    private val sut = LoadSavedAreaCasesUseCase(
        homeDataSource,
        pastTwoWeekCaseBreakdownHelper,
        weeklyCaseDifferenceHelper
    )

    @Test
    fun `GIVEN less than a weeks worth of cases WHEN execute called THEN area case list is created with figures from past week`() =
        runBlockingTest {

            val now = LocalDate
                .from(OffsetDateTime.now(ZoneOffset.UTC))

            val random = Random(0)
            val areaCode = "001"
            val areaName = "UK"

            val latestWeekData = buildWeeklyData(
                now.minusDays(7),
                0,
                random,
                areaCode,
                areaName
            )

            val allCasesFlow = flow {
                emit(latestWeekData.cases)
            }

            every { homeDataSource.savedAreaCases() } returns allCasesFlow

            val emittedItems = mutableListOf<List<AreaCaseListModel>>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val emittedAreaCaseList = emittedItems.first()
            val lastCaseInLatestWeek = latestWeekData.cases.last()

            assertThat(emittedAreaCaseList.first()).isEqualTo(
                AreaCaseListModel(
                    areaCode = lastCaseInLatestWeek.areaCode,
                    areaName = lastCaseInLatestWeek.areaName,
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

            val previousWeekData = buildWeeklyData(
                now.minusDays(14),
                0,
                random,
                areaCode,
                areaName
            )

            val latestWeekData = buildWeeklyData(
                now.minusDays(7),
                previousWeekData.cumulativeTotalCases,
                random,
                areaCode,
                areaName
            )

            val allCases = previousWeekData.cases + latestWeekData.cases

            val allCasesFlow = flow {
                emit(allCases)
            }

            every { homeDataSource.savedAreaCases() } returns allCasesFlow

            val emittedItems = mutableListOf<List<AreaCaseListModel>>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val emittedAreaCaseList = emittedItems.first()
            val lastCaseThisWeek = latestWeekData.cases.last()
            val lastCasePreviousWeek = previousWeekData.cases.last()

            assertThat(emittedAreaCaseList.first()).isEqualTo(
                AreaCaseListModel(
                    areaCode = lastCaseThisWeek.areaCode,
                    areaName = lastCaseThisWeek.areaName,
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
                areaName = "Woking"
            )

            val aldershotCases = buildWeeklyAreaData(
                now,
                random,
                "A001",
                areaName = "Aldersot"
            )

            val allCases =
                wokingCases.weekOne.cases + wokingCases.weekTwo.cases + aldershotCases.weekOne.cases + aldershotCases.weekTwo.cases

            val allCasesFlow = flow {
                emit(allCases)
            }

            every { homeDataSource.savedAreaCases() } returns allCasesFlow

            val emittedItems = mutableListOf<List<AreaCaseListModel>>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val emittedAreaCaseList = emittedItems.first()

            val lastWokingCaseThisWeek = wokingCases.weekTwo.cases.last()
            val lastWokingCasePreviousWeek = wokingCases.weekOne.cases.last()

            val lastAldershotCaseThisWeek = aldershotCases.weekTwo.cases.last()
            val lastAldershotCasePreviousWeek = aldershotCases.weekOne.cases.last()

            assertThat(emittedAreaCaseList[0]).isEqualTo(
                AreaCaseListModel(
                    areaCode = lastAldershotCaseThisWeek.areaCode,
                    areaName = lastAldershotCaseThisWeek.areaName,
                    totalLabConfirmedCases = lastAldershotCaseThisWeek.totalLabConfirmedCases,
                    dailyTotalLabConfirmedCasesRate = lastAldershotCaseThisWeek.dailyTotalLabConfirmedCasesRate,
                    changeInDailyTotalLabConfirmedCasesRate = lastAldershotCaseThisWeek.dailyTotalLabConfirmedCasesRate - lastAldershotCasePreviousWeek.dailyTotalLabConfirmedCasesRate,
                    changeInTotalLabConfirmedCases = aldershotCases.weekTwo.totalCasesInWeek - aldershotCases.weekOne.totalCasesInWeek,
                    totalLabConfirmedCasesLastWeek = aldershotCases.weekTwo.totalCasesInWeek
                )
            )
            assertThat(emittedAreaCaseList[1]).isEqualTo(
                AreaCaseListModel(
                    areaCode = lastWokingCaseThisWeek.areaCode,
                    areaName = lastWokingCaseThisWeek.areaName,
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
        areaName: String
    ): WeeklyAreaData {

        val previousWeekData = buildWeeklyData(
            startDate.minusDays(14),
            0,
            random,
            areaCode,
            areaName
        )

        val latestWeekData = buildWeeklyData(
            startDate.minusDays(7),
            previousWeekData.cumulativeTotalCases,
            random,
            areaCode,
            areaName
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
        areaName: String
    ): SavedAreaCaseDtoWrapper {

        var cumulativeTotal = startTotalCases
        var caseNumber = startTotalCases + 1

        val cases = (0 until 7).map {

            val dailyLabConfirmedCases = random.nextInt(15)
            cumulativeTotal += dailyLabConfirmedCases

            SavedAreaCaseDto(
                areaCode = areaCode,
                areaName = areaName,
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
