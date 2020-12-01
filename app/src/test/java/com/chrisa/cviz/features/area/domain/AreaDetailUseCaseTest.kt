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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.AreaDataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.RollingAverageHelper
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDetailDto
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
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

    private val areaDataSynchroniser = mockk<AreaDataSynchroniser>()
    private val areaDataSource = mockk<AreaDataSource>()
    private val rollingAverageHelper = mockk<RollingAverageHelper>()
    private val sut = AreaDetailUseCase(areaDataSynchroniser, areaDataSource)

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits no data result`() =
        runBlocking {
            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(area.areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow = sut.execute(area.areaCode, area.areaType)

            areaDetailModelFlow.collect { resultResult ->
                assertThat(resultResult).isEqualTo(AreaDetailModelResult.NoData)
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

            val areaDetailModelFlow = sut.execute(areaWithCases.areaCode, areaWithCases.areaType)

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            areaType = AreaType.OVERVIEW.value,
                            lastUpdatedAt = lastUpdatedDateTime,
                            lastSyncedAt = syncDateTime,
                            cases = areaWithCases.cases,
                            deaths = emptyList(),
                            hospitalAdmissions = emptyList()
                        )
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

            val areaDetailModelFlow = sut.execute(areaWithDeaths.areaCode, areaWithDeaths.areaType)

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            areaType = AreaType.OVERVIEW.value,
                            lastUpdatedAt = lastUpdatedDateTime,
                            lastSyncedAt = syncDateTime,
                            cases = emptyList(),
                            deaths = areaWithDeaths.deaths,
                            hospitalAdmissions = emptyList()
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest hospital admissions for the area`() =
        runBlocking {
            every { rollingAverageHelper.average(any(), any()) } returns 1.0
            every { areaDataSource.loadAreaMetadata(areaWithHospitalAdmissions.areaCode) } returns listOf(
                metadata
            ).asFlow()
            coEvery { areaDataSource.loadAreaData(areaWithHospitalAdmissions.areaCode) } returns areaWithHospitalAdmissions

            val areaDetailModelFlow = sut.execute(
                areaWithHospitalAdmissions.areaCode,
                areaWithHospitalAdmissions.areaType
            )

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            areaType = AreaType.OVERVIEW.value,
                            lastUpdatedAt = lastUpdatedDateTime,
                            lastSyncedAt = syncDateTime,
                            cases = emptyList(),
                            deaths = emptyList(),
                            hospitalAdmissions = emptyList()
                        )
                    )
                )
            }
        }

    companion object {

        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 12, 0)
        private val lastUpdatedDateTime = LocalDateTime.of(2020, 1, 1, 11, 0)

        private val metadata =
            MetadataDto(lastUpdatedAt = lastUpdatedDateTime, lastSyncTime = syncDateTime)

        private val area = AreaDetailDto(
            areaName = "United Kingdom",
            areaCode = Constants.UK_AREA_CODE,
            areaType = AreaType.OVERVIEW.value,
            cases = emptyList(),
            deaths = emptyList()
        )

        private val areaWithCases = area.copy(
            cases = SynchronisationTestData.dailyData()
        )

        private val areaWithDeaths = area.copy(
            deaths = SynchronisationTestData.dailyData()
        )

        private val areaWithHospitalAdmissions = area
    }
}
