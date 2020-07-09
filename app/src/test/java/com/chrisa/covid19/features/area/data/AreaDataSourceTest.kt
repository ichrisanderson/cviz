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
import com.chrisa.covid19.features.area.data.dtos.CaseDTO
import com.chrisa.covid19.features.area.data.dtos.MetadataDTO
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.util.Date
import org.junit.Test

class AreaDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()

    private val sut = AreaDataSource(appDatabase)

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
            MetadataDTO(
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
            CaseDTO(
                date = casesEntity.date,
                dailyLabConfirmedCases = casesEntity.dailyLabConfirmedCases
            )
        )
    }
}
