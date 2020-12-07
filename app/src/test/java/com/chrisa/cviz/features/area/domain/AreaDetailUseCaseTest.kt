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
import com.chrisa.cviz.core.data.synchronisation.AreaLookupDataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.RollingAverageHelper
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.data.AreaLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDetailDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class AreaDetailUseCaseTest {

    private val areaDataSynchroniser = mockk<AreaDataSynchroniser>()
    private val areaLookupDataSynchroniser = mockk<AreaLookupDataSynchroniser>()
    private val healthcareDataSynchroniser = mockk<HealthcareDataSynchroniser>()
    private val areaDataSource = mockk<AreaDataSource>()
    private val areaLookupDataSource = mockk<AreaLookupDataSource>()
    private val rollingAverageHelper = mockk<RollingAverageHelper>()
    private val sut = AreaDetailUseCase(
        areaDataSynchroniser,
        areaLookupDataSynchroniser,
        healthcareDataSynchroniser,
        areaDataSource,
        areaLookupDataSource
    )

    @Before
    fun setup() {
        coEvery { areaDataSynchroniser.performSync(any(), any()) } just Runs
        coEvery { areaLookupDataSynchroniser.performSync(any(), any()) } just Runs
        coEvery { healthcareDataSynchroniser.performSync(any(), any()) } just Runs
        every { areaLookupDataSource.healthCareArea(any(), any()) } returns ukArea
        every { areaDataSource.healthcareData(ukArea.code) } returns emptyList()
        every { rollingAverageHelper.average(any(), any()) } returns 1.0
    }

    @Test
    fun `WHEN execute called THEN area cases are synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata(area.areaCode) } returns listOf(null).asFlow()

            sut.execute(area.areaCode, area.areaType.toAreaType())

            coVerify(exactly = 1) {
                areaDataSynchroniser.performSync(
                    area.areaCode,
                    area.areaType.toAreaType()
                )
            }
        }

    @Test
    fun `GIVEN overview area WHEN execute called THEN area lookup is not synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("") } returns listOf(null).asFlow()

            sut.execute("", AreaType.OVERVIEW)

            coVerify(exactly = 0) { areaLookupDataSynchroniser.performSync(any(), any()) }
        }

    @Test
    fun `GIVEN nation area WHEN execute called THEN area lookup is not synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("") } returns listOf(null).asFlow()

            sut.execute("", AreaType.NATION)

            coVerify(exactly = 0) { areaLookupDataSynchroniser.performSync(any(), any()) }
        }

    @Test
    fun `GIVEN region area WHEN execute called THEN area lookup is synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()

            sut.execute("1", AreaType.REGION)

            coVerify(exactly = 1) { areaLookupDataSynchroniser.performSync("1", AreaType.REGION) }
        }

    @Test
    fun `GIVEN ltla area WHEN execute called THEN area lookup is synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()

            sut.execute("1", AreaType.LTLA)

            coVerify(exactly = 1) { areaLookupDataSynchroniser.performSync("1", AreaType.LTLA) }
        }

    @Test
    fun `GIVEN utla area WHEN execute called THEN area lookup is synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()

            sut.execute("1", AreaType.UTLA)

            coVerify(exactly = 1) { areaLookupDataSynchroniser.performSync("1", AreaType.UTLA) }
        }

    @Test
    fun `GIVEN overview area WHEN execute called THEN hospital synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()

            sut.execute("1", AreaType.OVERVIEW)

            coVerify(exactly = 1) { healthcareDataSynchroniser.performSync("1", AreaType.OVERVIEW) }
        }

    @Test
    fun `GIVEN nation area WHEN execute called THEN hospital synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()

            sut.execute("1", AreaType.NATION)

            coVerify(exactly = 1) { healthcareDataSynchroniser.performSync("1", AreaType.NATION) }
        }

    @Test
    fun `GIVEN region area WHEN execute called THEN hospital synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()
            every { areaLookupDataSource.healthCareArea(any(), any()) } returns
                AreaDto("1", "", AreaType.REGION)

            sut.execute("1", AreaType.REGION)

            coVerify(exactly = 1) { healthcareDataSynchroniser.performSync("1", AreaType.REGION) }
        }

    @Test
    fun `GIVEN ltla area WHEN execute called THEN hospital synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()
            every { areaLookupDataSource.healthCareArea(any(), any()) } returns
                AreaDto("1", "", AreaType.LTLA)

            sut.execute("1", AreaType.LTLA)

            coVerify(exactly = 1) { healthcareDataSynchroniser.performSync("1", AreaType.LTLA) }
        }

    @Test
    fun `GIVEN utla area WHEN execute called THEN hospital synced`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata("1") } returns listOf(null).asFlow()
            every { areaLookupDataSource.healthCareArea(any(), any()) } returns
                AreaDto("1", "", AreaType.UTLA)

            sut.execute("1", AreaType.UTLA)

            coVerify(exactly = 1) { healthcareDataSynchroniser.performSync("1", AreaType.UTLA) }
        }

    @Test
    fun `GIVEN metadata is null WHEN execute called THEN area detail emits no data result`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata(area.areaCode) } returns listOf(null).asFlow()

            val areaDetailModelFlow = sut.execute(area.areaCode, area.areaType.toAreaType())

            areaDetailModelFlow.collect { resultResult ->
                assertThat(resultResult).isEqualTo(AreaDetailModelResult.NoData)
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata(areaWithCases.areaCode) } returns
                listOf(metadata).asFlow()
            every { areaDataSource.loadAreaData(areaWithCases.areaCode) } returns
                areaWithCases

            val areaDetailModelFlow =
                sut.execute(areaWithCases.areaCode, areaWithCases.areaType.toAreaType())

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            areaType = AreaType.OVERVIEW.value,
                            lastUpdatedAt = lastUpdatedDateTime,
                            lastSyncedAt = syncDateTime,
                            cases = areaWithCases.cases,
                            deaths = emptyList(),
                            hospitalAdmissionsRegion = "United Kingdom",
                            hospitalAdmissions = emptyList()
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest deaths for the area`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata(areaWithDeaths.areaCode) } returns
                listOf(metadata).asFlow()
            every { areaDataSource.loadAreaData(areaWithDeaths.areaCode) } returns
                areaWithDeaths

            val areaDetailModelFlow =
                sut.execute(areaWithDeaths.areaCode, areaWithDeaths.areaType.toAreaType())

            areaDetailModelFlow.collect { result ->
                assertThat(result).isEqualTo(
                    AreaDetailModelResult.Success(
                        AreaDetailModel(
                            areaType = AreaType.OVERVIEW.value,
                            lastUpdatedAt = lastUpdatedDateTime,
                            lastSyncedAt = syncDateTime,
                            cases = emptyList(),
                            deaths = areaWithDeaths.deaths,
                            hospitalAdmissionsRegion = "United Kingdom",
                            hospitalAdmissions = emptyList()
                        )
                    )
                )
            }
        }

    @Test
    fun `WHEN execute called THEN area detail contains the latest hospital admissions for the area`() =
        runBlocking {
            every { areaDataSource.loadAreaMetadata(areaWithHospitalAdmissions.areaCode) } returns
                listOf(metadata).asFlow()
            every { areaDataSource.loadAreaData(areaWithHospitalAdmissions.areaCode) } returns
                areaWithHospitalAdmissions
            every { areaLookupDataSource.healthCareArea(any(), any()) } returns
                AreaDto(
                    areaWithHospitalAdmissions.areaCode,
                    areaWithHospitalAdmissions.areaName,
                    areaWithHospitalAdmissions.areaType.toAreaType()
                )
            every { areaDataSource.healthcareData(areaWithHospitalAdmissions.areaCode) } returns
                SynchronisationTestData.dailyData()

            val areaDetailModelFlow = sut.execute(
                areaWithHospitalAdmissions.areaCode,
                areaWithHospitalAdmissions.areaType.toAreaType()
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
                            hospitalAdmissionsRegion = areaWithHospitalAdmissions.areaName,
                            hospitalAdmissions = SynchronisationTestData.dailyData()
                        )
                    )
                )
            }
        }

    companion object {

        fun String.toAreaType() = AreaType.from(this)!!

        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 12, 0)
        private val lastUpdatedDateTime = LocalDateTime.of(2020, 1, 1, 11, 0)

        private val metadata =
            MetadataDto(lastUpdatedAt = lastUpdatedDateTime, lastSyncTime = syncDateTime)

        private val ukArea = AreaDto(Constants.UK_AREA_CODE, "United Kingdom", AreaType.OVERVIEW)
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
