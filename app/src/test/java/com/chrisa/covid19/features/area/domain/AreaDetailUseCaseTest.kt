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

package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.helper.RollingAverageHelper
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AreaDetailUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()
    private val rollingAverageHelper = mockk<RollingAverageHelper>()
    private val sut = AreaDetailUseCase(areaDataSource, rollingAverageHelper)

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {

            val areaCode = "1234"
            val areaType = "utla"

            val now = LocalDateTime.now()
            val metadataDTO = MetadataDto(
                lastUpdatedAt = now.minusDays(1),
                lastSyncTime = now
            )

            var totalLabConfirmedCases = 0
            val caseDTOs = (1 until 100).map {
                totalLabConfirmedCases += it
                CaseDto(
                    date = LocalDate.ofEpochDay(it.toLong()),
                    dailyLabConfirmedCases = it,
                    totalLabConfirmedCases = totalLabConfirmedCases
                )
            }

            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(areaCode, areaType) } returns listOf(metadataDTO).asFlow()
            coEvery { areaDataSource.loadAreaData(areaCode, areaType) } returns caseDTOs

            val caseModels = caseDTOs.map {
                CaseModel(
                    dailyLabConfirmedCases = it.dailyLabConfirmedCases,
                    date = it.date,
                    rollingAverage = 1.0
                )
            }

            val areaDetailModelFlow = sut.execute(areaCode, areaType)

            areaDetailModelFlow.collect { areaDetailModel ->
                assertThat(areaDetailModel).isEqualTo(
                    AreaDetailModel(
                        lastUpdatedAt = metadataDTO.lastUpdatedAt,
                        lastSyncedAt = metadataDTO.lastSyncTime,
                        allCases = caseModels,
                        latestCases = caseModels.takeLast(14)
                    )
                )
            }
        }
}
