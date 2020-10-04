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

package com.chrisa.covid19.features.home.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.AreaDataMetadataTuple
import com.chrisa.covid19.core.data.db.AreaSummaryEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private val sut = HomeDataSource(appDatabase)

    @Test
    fun `WHEN ukOverview called THEN all cases from uk are returned`() = runBlockingTest {

        val caseEntity = AreaDataMetadataTuple(
            lastUpdatedAt = LocalDateTime.now(),
            areaCode = "1234",
            areaName = "London",
            areaType = AreaType.UTLA,
            date = LocalDate.ofEpochDay(0),
            cumulativeCases = 222,
            infectionRate = 122.0,
            newCases = 122
        )

        val allCases = listOf(
            caseEntity,
            caseEntity.copy(areaCode = "1111", areaName = "England")
        )
        val allCasesFlow = flow { emit(allCases) }

        val allDailyRecordDtos = allCases.map {
            DailyRecordDto(
                areaCode = it.areaCode,
                areaName = it.areaName,
                areaType = it.areaType.value,
                newCases = it.newCases,
                cumulativeCases = it.cumulativeCases,
                lastUpdated = it.lastUpdatedAt
            )
        }

        every {
            appDatabase.areaDataDao().latestWithMetadataByAreaCodeAsFlow(
                listOf(
                    Constants.UK_AREA_CODE,
                    Constants.ENGLAND_AREA_CODE,
                    Constants.NORTHERN_IRELAND_AREA_CODE,
                    Constants.SCOTLAND_AREA_CODE,
                    Constants.WALES_AREA_CODE
                )
            )
        } returns allCasesFlow

        val emittedItems = mutableListOf<List<DailyRecordDto>>()

        sut.ukOverview().collect { emittedItems.add(it) }

        assertThat(emittedItems.size).isEqualTo(1)
        assertThat(emittedItems.first()).isEqualTo(allDailyRecordDtos)
    }

    @Test
    fun `GIVEN duplicate areas WHEN ukOverview called THEN all unique cases from uk are returned`() =
        runBlockingTest {

            val ukData = AreaDataMetadataTuple(
                lastUpdatedAt = LocalDateTime.now(),
                areaCode = Constants.UK_AREA_CODE,
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = syncTime.toLocalDate(),
                cumulativeCases = 222,
                infectionRate = 122.0,
                newCases = 122
            )

            val englandData = ukData.copy(
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = "England",
                areaType = AreaType.NATION,
                lastUpdatedAt = ukData.lastUpdatedAt.minusDays(4)
            )
            val northernIrelandData = ukData.copy(
                areaCode = Constants.NORTHERN_IRELAND_AREA_CODE,
                areaName = "Northern Ireland",
                areaType = AreaType.NATION,
                lastUpdatedAt = ukData.lastUpdatedAt.minusDays(3)
            )
            val scotlandData = ukData.copy(
                areaCode = Constants.SCOTLAND_AREA_CODE,
                areaName = "Scotland",
                areaType = AreaType.NATION,
                lastUpdatedAt = ukData.lastUpdatedAt.minusDays(2)
            )
            val walesData = ukData.copy(
                areaCode = Constants.WALES_AREA_CODE,
                areaName = "Wales",
                areaType = AreaType.NATION,
                lastUpdatedAt = ukData.lastUpdatedAt.minusDays(1)
            )

            val allCases = listOf(
                scotlandData,
                scotlandData.copy(date = ukData.date.minusDays(7)),
                northernIrelandData,
                northernIrelandData.copy(date = ukData.date.minusDays(7)),
                ukData,
                ukData.copy(date = ukData.date.minusDays(7)),
                walesData,
                walesData.copy(date = ukData.date.minusDays(7)),
                englandData,
                englandData.copy(date = englandData.date.minusDays(7))
            )
            val allCasesFlow = flow { emit(allCases) }

            every {
                appDatabase.areaDataDao().latestWithMetadataByAreaCodeAsFlow(
                    listOf(
                        Constants.UK_AREA_CODE,
                        Constants.ENGLAND_AREA_CODE,
                        Constants.NORTHERN_IRELAND_AREA_CODE,
                        Constants.SCOTLAND_AREA_CODE,
                        Constants.WALES_AREA_CODE
                    )
                )
            } returns allCasesFlow

            val emittedItems = mutableListOf<List<DailyRecordDto>>()

            sut.ukOverview().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems.first()).isEqualTo(listOf(
                DailyRecordDto(
                    areaCode = ukData.areaCode,
                    areaName = ukData.areaName,
                    areaType = ukData.areaType.value,
                    newCases = ukData.newCases,
                    cumulativeCases = ukData.cumulativeCases,
                    lastUpdated = ukData.lastUpdatedAt
                ),
                DailyRecordDto(
                    areaCode = englandData.areaCode,
                    areaName = englandData.areaName,
                    areaType = englandData.areaType.value,
                    newCases = englandData.newCases,
                    cumulativeCases = englandData.cumulativeCases,
                    lastUpdated = englandData.lastUpdatedAt
                ),
                DailyRecordDto(
                    areaCode = scotlandData.areaCode,
                    areaName = scotlandData.areaName,
                    areaType = scotlandData.areaType.value,
                    newCases = scotlandData.newCases,
                    cumulativeCases = scotlandData.cumulativeCases,
                    lastUpdated = scotlandData.lastUpdatedAt
                ),
                DailyRecordDto(
                    areaCode = walesData.areaCode,
                    areaName = walesData.areaName,
                    areaType = walesData.areaType.value,
                    newCases = walesData.newCases,
                    cumulativeCases = walesData.cumulativeCases,
                    lastUpdated = walesData.lastUpdatedAt
                ),
                DailyRecordDto(
                    areaCode = northernIrelandData.areaCode,
                    areaName = northernIrelandData.areaName,
                    areaType = northernIrelandData.areaType.value,
                    newCases = northernIrelandData.newCases,
                    cumulativeCases = northernIrelandData.cumulativeCases,
                    lastUpdated = northernIrelandData.lastUpdatedAt
                )
            ))
        }

    @Test
    fun `WHEN savedAreaCases called THEN all saved areas from database are returned`() =
        runBlockingTest {

            val caseEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId("1234"),
                areaCode = "1234",
                areaName = "London",
                areaType = AreaType.UTLA,
                date = LocalDate.ofEpochDay(0),
                cumulativeCases = 222,
                infectionRate = 122.0,
                newCases = 122
            )

            val allCases = listOf(
                caseEntity,
                caseEntity.copy(areaCode = "1111", areaName = "England")
            )
            val allCasesFlow = flow { emit(allCases) }

            val allSavedAreaDtos = allCases.map {
                SavedAreaCaseDto(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = it.areaType,
                    newCases = it.newCases,
                    cumulativeCases = it.cumulativeCases,
                    infectionRate = it.infectionRate,
                    date = it.date
                )
            }

            every {
                appDatabase.areaDataDao().allSavedAreaDataAsFlow()
            } returns allCasesFlow

            val emittedItems = mutableListOf<List<SavedAreaCaseDto>>()

            sut.savedAreaCases().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems.first()).isEqualTo(allSavedAreaDtos)
        }

    @Test
    fun `WHEN areaSummaryEntities called THEN all area summary entities are returned`() = runBlockingTest {

        val areaSummaryEntity = AreaSummaryEntity(
            areaCode = Constants.UK_AREA_CODE,
            areaType = AreaType.OVERVIEW,
            areaName = "UK",
            date = syncTime.toLocalDate(),
            baseInfectionRate = 100.0,
            cumulativeCasesWeek1 = 100,
            cumulativeCaseInfectionRateWeek1 = 85.0,
            newCaseInfectionRateWeek1 = 25.0,
            newCasesWeek1 = 30,
            cumulativeCasesWeek2 = 80,
            cumulativeCaseInfectionRateWeek2 = 80.0,
            newCaseInfectionRateWeek2 = 22.0,
            newCasesWeek2 = 22,
            cumulativeCasesWeek3 = 66,
            cumulativeCaseInfectionRateWeek3 = 82.0,
            newCaseInfectionRateWeek3 = 33.0,
            newCasesWeek3 = 26,
            cumulativeCasesWeek4 = 50,
            cumulativeCaseInfectionRateWeek4 = 75.0
        )

        val areaSummaryEntities = listOf(areaSummaryEntity)
        val allAreaSummaryEntities = flow { emit(areaSummaryEntities) }

        val allInfectionRates = areaSummaryEntities.map {
            AreaSummaryDto(
                areaCode = it.areaCode,
                areaType = it.areaType.value,
                areaName = it.areaName,
                changeInCases = it.newCasesWeek1 - it.newCasesWeek2,
                currentNewCases = it.newCasesWeek1,
                currentInfectionRate = it.newCaseInfectionRateWeek1,
                changeInInfectionRate = it.newCaseInfectionRateWeek1 - it.newCaseInfectionRateWeek2
            )
        }

        every {
            appDatabase.areaSummaryEntityDao().allAsFlow()
        } returns allAreaSummaryEntities

        val emittedItems = mutableListOf<List<AreaSummaryDto>>()

        sut.areaSummaries().collect { emittedItems.add(it) }

        assertThat(emittedItems.size).isEqualTo(1)
        assertThat(emittedItems.first()).isEqualTo(allInfectionRates)
    }
}
