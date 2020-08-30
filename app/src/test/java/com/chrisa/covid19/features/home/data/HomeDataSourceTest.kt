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
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.time.TimeProvider
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.MetadataDto
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
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val timeProvider = mockk<TimeProvider>()
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
    private val sut = HomeDataSource(appDatabase, timeProvider)

    @Before
    fun setup() {
        every { timeProvider.currentTime() } returns syncTime
    }

    @Test
    fun `WHEN overviewMetadata called THEN overview metadata from database is returned`() =
        runBlockingTest {

            val metadataEntity = MetadataEntity(
                id = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                lastUpdatedAt = syncTime.minusDays(1),
                lastSyncTime = syncTime
            )

            val overviewMetadataFlow = flow { emit(metadataEntity) }

            every {
                appDatabase.metadataDao().metadataAsFlow(MetaDataIds.areaCodeId(Constants.UK_AREA_CODE))
            } returns overviewMetadataFlow

            val emittedItems = mutableListOf<MetadataDto>()

            sut.overviewMetadata().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems.first()).isEqualTo(MetadataDto(
                lastUpdatedAt = metadataEntity.lastUpdatedAt
            ))
        }

    @Test
    fun `WHEN ukOverview called THEN all cases from uk are returned`() = runBlockingTest {

        val caseEntity = AreaDataEntity(
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
                areaName = it.areaName,
                dailyLabConfirmedCases = it.newCases,
                totalLabConfirmedCases = it.cumulativeCases,
                date = it.date
            )
        }

        every {
            appDatabase.areaDataDao().allByAreaCodeFlow(Constants.UK_AREA_CODE)
        } returns allCasesFlow

        val emittedItems = mutableListOf<List<DailyRecordDto>>()

        sut.ukOverview().collect { emittedItems.add(it) }

        assertThat(emittedItems.size).isEqualTo(1)
        assertThat(emittedItems.first()).isEqualTo(allDailyRecordDtos)
    }

    @Test
    fun `WHEN savedAreaCases called THEN all saved areas from database are returned`() = runBlockingTest {

        val caseEntity = AreaDataEntity(
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
                areaType = it.areaType.value,
                dailyLabConfirmedCases = it.newCases,
                totalLabConfirmedCases = it.cumulativeCases,
                dailyTotalLabConfirmedCasesRate = it.infectionRate,
                date = it.date
            )
        }

        every {
            appDatabase.areaDataDao().allSavedAreaData()
        } returns allCasesFlow

        val emittedItems = mutableListOf<List<SavedAreaCaseDto>>()

        sut.savedAreaCases().collect { emittedItems.add(it) }

        assertThat(emittedItems.size).isEqualTo(1)
        assertThat(emittedItems.first()).isEqualTo(allSavedAreaDtos)
    }
}
