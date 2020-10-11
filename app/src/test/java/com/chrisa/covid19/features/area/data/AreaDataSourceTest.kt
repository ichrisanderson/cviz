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

package com.chrisa.covid19.features.area.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataDao
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.features.area.data.dtos.AreaCaseDto
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.DeathDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class AreaDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val sut = AreaDataSource(appDatabase)

    @Test
    fun `GIVEN area is not saved WHEN isSaved called THEN savedState is false`() = runBlockingTest {

        val areaCode = "A01"
        val publisher = ConflatedBroadcastChannel(false)

        every { appDatabase.savedAreaDao().isSaved(areaCode) } returns publisher.asFlow()

        val savedState = sut.isSaved(areaCode).first()

        assertThat(savedState).isEqualTo(false)
    }

    @Test
    fun `GIVEN area is saved WHEN isSaved called THEN savedState is true`() = runBlockingTest {

        val areaCode = "A01"
        val publisher = ConflatedBroadcastChannel(false)

        every { appDatabase.savedAreaDao().isSaved(areaCode) } returns publisher.asFlow()

        val savedState = sut.isSaved(areaCode).first()

        assertThat(savedState).isEqualTo(false)
    }

    @Test
    fun `WHEN insertSavedArea called THEN entity is inserted into the database`() {

        val dto = SavedAreaDto("A01")
        val entity = dto.toSavedAreaEntity()

        every { appDatabase.savedAreaDao().insert(entity) } just Runs

        sut.insertSavedArea(dto)

        verify(exactly = 1) { appDatabase.savedAreaDao().insert(entity) }
    }

    @Test
    fun `WHEN deleteSavedArea called THEN entity is deleted from the database`() {

        val dto = SavedAreaDto("A01")
        val entity = dto.toSavedAreaEntity()

        every { appDatabase.savedAreaDao().delete(entity) } returns 1

        val deletedRows = sut.deleteSavedArea(dto)

        assertThat(deletedRows).isEqualTo(1)
    }

    @Test
    fun `WHEN loadAreaMetadata called THEN area metadata is returned`() = runBlocking {

        val areaCode = "1234"

        val now = LocalDateTime.now()
        val metadataDTO = MetadataEntity(
            id = MetaDataIds.areaCodeId(areaCode),
            lastUpdatedAt = now.minusDays(1),
            lastSyncTime = now
        )

        val allMetadata = listOf(metadataDTO)

        every {
            appDatabase.metadataDao().metadataAsFlow(MetaDataIds.areaCodeId(areaCode))
        } returns allMetadata.asFlow()

        val metadataFlow = sut.loadAreaMetadata(areaCode)

        metadataFlow.collect { metadata ->
            assertThat(metadata).isEqualTo(
                MetadataDto(
                    lastUpdatedAt = metadataDTO.lastUpdatedAt,
                    lastSyncTime = metadataDTO.lastSyncTime
                )
            )
        }
    }

    @Test
    fun `WHEN loadAreaData called THEN area data is returned`() = runBlocking {

        val areaCode = "1234"
        val areaName = "London"
        val areaType = AreaType.UTLA

        val areaData = AreaDataEntity(
            areaCode = areaCode,
            areaName = areaName,
            areaType = areaType,
            metadataId = MetaDataIds.areaCodeId("1234"),
            date = LocalDate.ofEpochDay(0),
            cumulativeCases = 222,
            infectionRate = 122.0,
            newCases = 122,
            newDeathsByPublishedDate = 15,
            cumulativeDeathsByPublishedDate = 20,
            cumulativeDeathsByPublishedDateRate = 30.0,
            newDeathsByDeathDate = 40,
            cumulativeDeathsByDeathDate = 50,
            cumulativeDeathsByDeathDateRate = 60.0,
            newAdmissions = 70,
            cumulativeAdmissions = 80,
            occupiedBeds = 90
        )

        every { areaDataDao.allByAreaCode(areaData.areaCode) } returns listOf(areaData)
        every { appDatabase.areaDataDao() } returns areaDataDao

        val areaCase = sut.loadAreaData(areaData.areaCode)

        assertThat(areaCase).isEqualTo(
            AreaCaseDto(
                areaCode = areaCode,
                areaName = areaName,
                areaType = areaType.value,
                cases = listOf(
                    CaseDto(
                        newCases = areaData.newCases,
                        cumulativeCases = areaData.cumulativeCases,
                        date = areaData.date,
                        infectionRate = areaData.infectionRate,
                        baseRate = areaData.infectionRate / areaData.cumulativeCases
                    )
                ),
                deathsByPublishedDate = listOf(
                    DeathDto(
                        newDeaths = areaData.newDeathsByPublishedDate!!,
                        cumulativeDeaths = areaData.cumulativeDeathsByPublishedDate!!,
                        date = areaData.date,
                        deathRate = areaData.cumulativeDeathsByPublishedDateRate!!,
                        baseRate = areaData.cumulativeDeathsByPublishedDateRate!! / areaData.cumulativeDeathsByPublishedDate!!
                    )
                )
            )
        )
    }
}
