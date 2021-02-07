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

package com.chrisa.cviz.features.area.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.cviz.core.util.test
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.domain.AreaDetailModelResult
import com.chrisa.cviz.features.area.domain.AreaDetailUseCase
import com.chrisa.cviz.features.area.domain.DeleteSavedAreaUseCase
import com.chrisa.cviz.features.area.domain.InsertSavedAreaUseCase
import com.chrisa.cviz.features.area.domain.IsSavedUseCase
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.presentation.mappers.AreaDataModelMapper
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import com.chrisa.cviz.features.area.presentation.models.HospitalAdmissionsAreaModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.plaidapp.core.util.event.Event
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AreaViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val areaDetailUseCase = mockk<AreaDetailUseCase>(relaxed = true)
    private val isSavedUseCase = mockk<IsSavedUseCase>(relaxed = true)
    private val insertSavedAreaUseCase = mockk<InsertSavedAreaUseCase>(relaxed = true)
    private val deleteSavedAreaUseCase = mockk<DeleteSavedAreaUseCase>(relaxed = true)
    private val areaUiModelMapper = mockk<AreaDataModelMapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN area detail succeeds WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                val areaCasesModel = areaData()
                val areaDetailModel = areaDetailModel()
                coEvery { areaDetailUseCase.execute(areaCode, areaType) } returns
                    listOf(AreaDetailModelResult.Success(areaDetailModel)).asFlow()
                every {
                    areaUiModelMapper.mapAreaDetailModel(
                        eq(areaDetailModel),
                        any()
                    )
                } returns
                    areaCasesModel

                val sut = areaViewModel()

                val statesObserver = sut.areaDataModel.test()
                val isLoadingObserver = sut.isLoading.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(areaCasesModel)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
            }
        }

    @Test
    fun `GIVEN areaDetailUseCase returns no data WHEN viewmodel initialized THEN error state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                coEvery { areaDetailUseCase.execute(areaCode, areaType) } returns
                    listOf(AreaDetailModelResult.NoData).asFlow()

                val sut = areaViewModel()

                val statesObserver = sut.areaDataModel.test()
                val isLoadingObserver = sut.isLoading.test()
                val syncAreaError = sut.syncAreaError.test()

                runCurrent()

                assertThat(statesObserver.values).isEmpty()
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
                assertThat(syncAreaError.values[0]).isEqualTo(Event(true))
            }
        }

    @Test
    fun `GIVEN areaDetailUseCase fails WHEN viewmodel initialized THEN error state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                coEvery { areaDetailUseCase.execute(areaCode, areaType) } throws IOException()
                val sut = areaViewModel()
                val statesObserver = sut.areaDataModel.test()
                val isLoadingObserver = sut.isLoading.test()
                val syncAreaError = sut.syncAreaError.test()

                runCurrent()

                assertThat(statesObserver.values).isEmpty()
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
                assertThat(syncAreaError.values[0]).isEqualTo(Event(true))
            }
        }

    @Test
    fun `WHEN refresh called THEN saveAreaUseCase is executed`() =
        testDispatcher.runBlockingTest {
            coEvery { areaDetailUseCase.execute(areaCode, areaType) } throws IOException()
            val sut = areaViewModel()
            coVerify(exactly = 1) { areaDetailUseCase.execute(areaCode, areaType) }

            sut.retry()

            coVerify(exactly = 2) { areaDetailUseCase.execute(areaCode, areaType) }
        }

    @Test
    fun `GIVEN isSaved succeeds WHEN viewmodel initialized THEN saved state is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                val publisher = ConflatedBroadcastChannel(false)
                every { isSavedUseCase.execute(areaCode) } returns publisher.asFlow()
                val sut = areaViewModel()
                val observer = sut.isSaved.test()

                runCurrent()
                publisher.sendBlocking(true)
                runCurrent()
                publisher.sendBlocking(false)
                runCurrent()
                publisher.sendBlocking(true)
                runCurrent()
                publisher.sendBlocking(false)
                runCurrent()

                assertThat(observer.values.size).isEqualTo(5)
                assertThat(observer.values[0]).isEqualTo(false)
                assertThat(observer.values[1]).isEqualTo(true)
                assertThat(observer.values[2]).isEqualTo(false)
                assertThat(observer.values[3]).isEqualTo(true)
                assertThat(observer.values[4]).isEqualTo(false)
            }
        }

    @Test
    fun `WHEN insertSavedArea called THEN saveAreaUseCase is executed`() =
        testDispatcher.runBlockingTest {
            every { insertSavedAreaUseCase.execute(areaCode) } just Runs

            val sut = areaViewModel()

            sut.insertSavedArea()

            verify(exactly = 1) { insertSavedAreaUseCase.execute(areaCode) }
        }

    @Test
    fun `WHEN deleteSavedArea called THEN deleteSavedAreaUseCase is executed`() =
        testDispatcher.runBlockingTest {
            every { deleteSavedAreaUseCase.execute(areaCode) } returns 1

            val sut = areaViewModel()

            sut.deleteSavedArea()

            verify(exactly = 1) { deleteSavedAreaUseCase.execute(areaCode) }
        }

    @Test
    fun `WHEN retry called THEN loading states emitted`() =
        testDispatcher.runBlockingTest {
            val areaDetailModel = areaDetailModel()
            coEvery { areaDetailUseCase.execute(areaCode, areaType) } returns
                listOf(AreaDetailModelResult.Success(areaDetailModel)).asFlow()
            val sut = areaViewModel()

            val isLoadingObserver = sut.isLoading.test()
            sut.retry()

            assertThat(isLoadingObserver.values[0]).isEqualTo(false)
            assertThat(isLoadingObserver.values[1]).isEqualTo(true)
            assertThat(isLoadingObserver.values[2]).isEqualTo(false)
        }

    @Test
    fun `WHEN refresh called THEN refresh states emitted`() =
        testDispatcher.runBlockingTest {
            val areaDetailModel = areaDetailModel()
            coEvery { areaDetailUseCase.execute(areaCode, areaType) } returns
                listOf(AreaDetailModelResult.Success(areaDetailModel)).asFlow()
            val sut = areaViewModel()

            val isRefreshingObserver = sut.isRefreshing.test()
            sut.refresh()

            assertThat(isRefreshingObserver.values[0]).isEqualTo(false)
            assertThat(isRefreshingObserver.values[1]).isEqualTo(true)
        }

    @Test
    fun `WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                val admissions = SynchronisationTestData.dailyData(1, 23)
                val hospitalAdmissions = listOf(
                    AreaDailyDataDto("T1", admissions),
                    AreaDailyDataDto("T2", admissions),
                    AreaDailyDataDto("T3", admissions)
                )
                val areaCasesModel = areaData().copy(
                    hospitalAdmissions = hospitalAdmissions
                )
                val filteredAreaCasesModel = areaData().copy(
                    hospitalAdmissions = emptyList()
                )
                val areaDetailModel = areaDetailModel()
                val filteredHospitalAdmissionsAreaModels = hospitalAdmissions.map {
                    HospitalAdmissionsAreaModel(it.name, true)
                }
                val filteredHospitalAdmissionsAreaNames = filteredHospitalAdmissionsAreaModels.map {
                    it.areaName
                }.toSet()
                val sut = areaViewModel()
                coEvery { areaDetailUseCase.execute(areaCode, areaType) } returns
                    listOf(AreaDetailModelResult.Success(areaDetailModel)).asFlow()
                every {
                    areaUiModelMapper.mapAreaDetailModel(
                        eq(areaDetailModel),
                        any()
                    )
                } returns
                    areaCasesModel
                every {
                    areaUiModelMapper.updateHospitalAdmissionFilters(
                        areaCasesModel,
                        filteredHospitalAdmissionsAreaNames
                    )
                } returns
                    filteredAreaCasesModel

                runCurrent()

                val statesObserver = sut.areaDataModel.test()

                sut.setHospitalAdmissionFilter(filteredHospitalAdmissionsAreaModels)

                assertThat(statesObserver.values[0]).isEqualTo(areaCasesModel)
                assertThat(statesObserver.values[1]).isEqualTo(filteredAreaCasesModel)
            }
        }

    private fun areaViewModel(): AreaViewModel {
        return AreaViewModel(
            areaDetailUseCase,
            isSavedUseCase,
            insertSavedAreaUseCase,
            deleteSavedAreaUseCase,
            TestCoroutineDispatchersImpl(testDispatcher),
            areaUiModelMapper,
            savedStateHandle
        )
    }

    companion object {
        private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)
        private const val areaCode = "AC-001"
        private val areaType = AreaType.UTLA
        private val savedStateHandle =
            SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType.value))
        private val lastData = SynchronisationTestData.dailyData().last()

        private fun areaDetailModel(): AreaDetailModel {
            return AreaDetailModel(
                lastUpdatedAt = null,
                casesAreaName = "",
                cases = emptyList(),
                deathsByPublishedDateAreaName = "",
                deathsByPublishedDate = emptyList(),
                onsDeathAreaName = "",
                onsDeathsByRegistrationDate = emptyList(),
                hospitalAdmissionsAreaName = "",
                hospitalAdmissions = emptyList(),
                transmissionRate = null
            )
        }

        private fun areaData(): AreaDataModel {
            return AreaDataModel(
                lastUpdatedDate = syncTime,
                lastCaseDate = lastData.date,
                caseAreaName = "",
                caseSummary = WeeklySummary.EMPTY,
                caseChartData = emptyList(),
                showDeathsByPublishedDate = false,
                lastDeathPublishedDate = lastData.date,
                deathsByPublishedDateAreaName = "",
                deathsByPublishedDateSummary = WeeklySummary.EMPTY,
                deathsByPublishedDateChartData = emptyList(),
                showOnsDeaths = false,
                lastOnsDeathRegisteredDate = null,
                onsDeathsAreaName = "",
                onsDeathsByRegistrationDateChartData = emptyList(),
                showHospitalAdmissions = false,
                lastHospitalAdmissionDate = lastData.date,
                hospitalAdmissionsRegionName = "",
                hospitalAdmissionsSummary = WeeklySummary.EMPTY,
                hospitalAdmissions = emptyList(),
                hospitalAdmissionsChartData = emptyList(),
                canFilterHospitalAdmissionsAreas = false,
                hospitalAdmissionsAreas = emptyList(),
                areaTransmissionRate = null
            )
        }
    }
}
