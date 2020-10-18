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

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.synchronisation.RollingAverageHelper
import com.chrisa.covid19.core.data.synchronisation.SynchronisationTestData
import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.AreaDetailDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
    private val sut = AreaDetailUseCase(areaDataSource)

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits with null data`() =
        runBlocking {
            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(area.areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow = sut.execute(area.areaCode)

            areaDetailModelFlow.collect { emittedAreaDetailModel ->
                assertThat(emittedAreaDetailModel).isEqualTo(emptyAreaDetailModel)
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {
            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(areaWithCases.areaCode) } returns listOf(
                metadata
            ).asFlow()
            coEvery { areaDataSource.loadAreaData(areaWithCases.areaCode) } returns areaWithCases

            val areaDetailModelFlow = sut.execute(areaWithCases.areaCode)

            areaDetailModelFlow.collect { emittedAreaDetailModel ->
                assertThat(emittedAreaDetailModel).isEqualTo(
                    AreaDetailModel(
                        areaType = AreaType.OVERVIEW.value,
                        lastSyncedAt = syncDateTime,
                        allCases = areaWithCases.cases,
                        allDeaths = emptyList()
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest deaths for the area`() =
        runBlocking {
            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(areaWithDeaths.areaCode) } returns listOf(
                metadata
            ).asFlow()
            coEvery { areaDataSource.loadAreaData(areaWithDeaths.areaCode) } returns areaWithDeaths

            val areaDetailModelFlow = sut.execute(areaWithDeaths.areaCode)

            areaDetailModelFlow.collect { emittedAreaDetailModel ->
                assertThat(emittedAreaDetailModel).isEqualTo(
                    AreaDetailModel(
                        areaType = AreaType.OVERVIEW.value,
                        lastSyncedAt = syncDateTime,
                        allCases = emptyList(),
                        allDeaths = areaWithDeaths.deaths
                    )
                )
            }
        }

    companion object {

        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)
        private val metadata =
            MetadataDto(lastUpdatedAt = syncDateTime.minusDays(1), lastSyncTime = syncDateTime)

        private val emptyAreaDetailModel =
            AreaDetailModel(
                areaType = null,
                lastSyncedAt = null,
                allCases = emptyList(),
                allDeaths = emptyList()
            )

        private val area = AreaDetailDto(
            areaName = "United Kingdom",
            areaCode = Constants.UK_AREA_CODE,
            areaType = AreaType.OVERVIEW.value,
            cases = emptyList(),
            deaths = emptyList(),
            hospitalAdmissions = emptyList()
        )

        private val areaWithCases = area.copy(
            cases = SynchronisationTestData.dailyData()
        )

        private val areaWithDeaths = area.copy(
            deaths = SynchronisationTestData.dailyData()
        )
    }
}
