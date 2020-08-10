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

import androidx.room.withTransaction
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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
    private val covidApi = mockk<CovidApi>()
    private val sut = AreaDataSource(appDatabase, covidApi)

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
    fun `WHEN loadCaseMetadata called THEN case metadata is returned`() = runBlocking {

        val metadataDTO = MetadataEntity(
            id = MetadataEntity.AREA_DATA_OVERVIEW_METADATA_ID,
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        val allMetadata = listOf(metadataDTO)

        every {
            appDatabase.metadataDao().metadataAsFlow(MetadataEntity.AREA_DATA_OVERVIEW_METADATA_ID)
        } returns allMetadata.asFlow()

        val metadataFlow = sut.loadAreaMetadata()

        metadataFlow.collect { metadata ->
            assertThat(metadata).isEqualTo(
                MetadataDto(
                    lastUpdatedAt = metadataDTO.lastUpdatedAt
                )
            )
        }
    }

    @Test
    fun `WHEN loadCases called THEN case data is returned`() = runBlocking {

        val areaData = AreaDataModel(
            areaCode = "1234",
            areaName = "London",
            areaType = "utla",
            date = LocalDate.ofEpochDay(0),
            cumulativeCases = 222,
            infectionRate = 122.0,
            newCases = 122
        )

        val page = Page(
            length = null,
            maxPageLimit = null,
            data = listOf(areaData)
        )
        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { appDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
        every { appDatabase.areaDataDao() } returns mockk(relaxed = true)
        coEvery { covidApi.pagedAreaCodeDataBySpeciminDate(any()) } returns page

        val cases = sut.loadCases(areaData.areaCode, areaData.areaType)

        assertThat(cases.size).isEqualTo(1)
        assertThat(cases.first()).isEqualTo(
            CaseDto(
                date = areaData.date,
                dailyLabConfirmedCases = areaData.newCases!!,
                totalLabConfirmedCases = areaData.cumulativeCases!!
            )
        )
    }
}
