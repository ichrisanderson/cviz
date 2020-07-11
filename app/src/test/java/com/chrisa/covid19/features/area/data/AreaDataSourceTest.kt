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
import com.chrisa.covid19.core.data.db.CaseEntity
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class AreaDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
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
    fun `WHEN saveArea called THEN entity is inserted`() {

        val dto = SavedAreaDto("A01")
        val entity = dto.toSavedAreaEntity()

        every { appDatabase.savedAreaDao().insert(entity) } just Runs

        val savedState = sut.saveArea(dto)

        verify(exactly = 1) { appDatabase.savedAreaDao().insert(entity) }
    }

    @Test
    fun `WHEN loadCaseMetadata called THEN case metadata is returned`() {

        val metadataDTO = MetadataEntity(
            id = MetadataEntity.CASE_METADATA_ID,
            disclaimer = "disclaimer",
            lastUpdatedAt = Date(2)
        )

        every {
            appDatabase.metadataDao().searchMetadata(MetadataEntity.CASE_METADATA_ID)
        } returns listOf(metadataDTO)

        val metadata = sut.loadCaseMetadata()

        assertThat(metadata).isEqualTo(
            MetadataDto(
                lastUpdatedAt = metadataDTO.lastUpdatedAt
            )
        )
    }

    @Test
    fun `WHEN loadCases called THEN case data is returned`() {

        val casesEntity = CaseEntity(
            areaCode = "1234",
            areaName = "London",
            date = Date(1),
            dailyLabConfirmedCases = 222,
            dailyTotalLabConfirmedCasesRate = 122.0,
            totalLabConfirmedCases = 122
        )

        every {
            appDatabase.casesDao().searchAllCases(casesEntity.areaCode)
        } returns listOf(casesEntity)

        val cases = sut.loadCases(casesEntity.areaCode)

        assertThat(cases.size).isEqualTo(1)
        assertThat(cases.first()).isEqualTo(
            CaseDto(
                date = casesEntity.date,
                dailyLabConfirmedCases = casesEntity.dailyLabConfirmedCases
            )
        )
    }
}
