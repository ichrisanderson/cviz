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
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.features.area.data.dtos.AreaDetailDto
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
        every { appDatabase.savedAreaDao().isSaved(areaData.areaCode) } returns ConflatedBroadcastChannel(false).asFlow()

        val savedState = sut.isSaved(areaData.areaCode).first()

        assertThat(savedState).isEqualTo(false)
    }

    @Test
    fun `GIVEN area is saved WHEN isSaved called THEN savedState is true`() = runBlockingTest {
        every { appDatabase.savedAreaDao().isSaved(areaData.areaCode) } returns ConflatedBroadcastChannel(false).asFlow()

        val savedState = sut.isSaved(areaData.areaCode).first()

        assertThat(savedState).isEqualTo(false)
    }

    @Test
    fun `WHEN insertSavedArea called THEN entity is inserted into the database`() {
        val dto = SavedAreaDto(areaData.areaCode)
        val entity = dto.toSavedAreaEntity()

        every { appDatabase.savedAreaDao().insert(entity) } just Runs

        sut.insertSavedArea(dto)

        verify(exactly = 1) { appDatabase.savedAreaDao().insert(entity) }
    }

    @Test
    fun `WHEN deleteSavedArea called THEN entity is deleted from the database`() {
        val dto = SavedAreaDto(areaData.areaCode)
        every { appDatabase.savedAreaDao().delete(dto.toSavedAreaEntity()) } returns 1

        val deletedRows = sut.deleteSavedArea(dto)

        assertThat(deletedRows).isEqualTo(1)
    }

    @Test
    fun `WHEN loadAreaMetadata called THEN area metadata is returned`() = runBlocking {
        every {
            appDatabase.metadataDao().metadataAsFlow(MetaDataIds.areaCodeId(areaData.areaCode))
        } returns listOf(metadata).asFlow()

        val metadataFlow = sut.loadAreaMetadata(areaData.areaCode)

        metadataFlow.collect { metadataDto ->
            assertThat(metadataDto).isEqualTo(
                MetadataDto(
                    lastUpdatedAt = metadata.lastUpdatedAt,
                    lastSyncTime = metadata.lastSyncTime
                )
            )
        }
    }

    @Test
    fun `WHEN loadAreaData called THEN area data is returned`() = runBlocking {
        every { areaDataDao.allByAreaCode(areaData.areaCode) } returns listOf(areaData)
        every { appDatabase.areaDataDao() } returns areaDataDao

        val areaCase = sut.loadAreaData(areaData.areaCode)

        assertThat(areaCase).isEqualTo(
            AreaDetailDto(
                areaCode = areaData.areaCode,
                areaName = areaData.areaName,
                areaType = areaData.areaType.value,
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

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val metadata = MetadataEntity(
            id = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
            lastUpdatedAt = syncDate.minusDays(1),
            lastSyncTime = syncDate
        )

        private val areaData = AreaDataEntity(
            areaCode = Constants.UK_AREA_CODE,
            areaName = "United Kingdom",
            areaType = AreaType.OVERVIEW,
            metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
            date = syncDate.toLocalDate(),
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
    }
}
