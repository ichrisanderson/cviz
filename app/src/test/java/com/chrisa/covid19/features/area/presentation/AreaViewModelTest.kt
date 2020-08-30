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

package com.chrisa.covid19.features.area.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.LineChartData
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.area.domain.AreaDetailUseCase
import com.chrisa.covid19.features.area.domain.DeleteSavedAreaUseCase
import com.chrisa.covid19.features.area.domain.InsertSavedAreaUseCase
import com.chrisa.covid19.features.area.domain.IsSavedUseCase
import com.chrisa.covid19.features.area.domain.SyncAreaDetailUseCase
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.presentation.mappers.AreaCasesModelMapper
import com.chrisa.covid19.features.area.presentation.models.AreaCasesModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.plaidapp.core.util.event.Event
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class AreaViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val areaDetailUseCase = mockk<AreaDetailUseCase>(relaxed = true)
    private val syncAreaDetailUseCase = mockk<SyncAreaDetailUseCase>(relaxed = true)
    private val isSavedUseCase = mockk<IsSavedUseCase>(relaxed = true)
    private val insertSavedAreaUseCase = mockk<InsertSavedAreaUseCase>(relaxed = true)
    private val deleteSavedAreaUseCase = mockk<DeleteSavedAreaUseCase>(relaxed = true)
    private val areaUiModelMapper = mockk<AreaCasesModelMapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN area detail succeeds WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val areaType = "utla"
                val savedStateHandle =
                    SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

                val caseModels = listOf(
                    CaseModel(
                        dailyLabConfirmedCases = 123,
                        date = LocalDate.ofEpochDay(0),
                        rollingAverage = 1.0
                    )
                )

                val now = LocalDateTime.now()
                val areaDetailModel = AreaDetailModel(
                    lastUpdatedAt = now.minusDays(1),
                    lastSyncedAt = now,
                    allCases = caseModels,
                    latestCases = caseModels.takeLast(7)
                )

                val areaCasesModel = AreaCasesModel(
                    lastUpdatedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(0),
                        ZoneOffset.UTC
                    ),
                    allCasesChartData = BarChartData(
                        label = "All cases",
                        values = emptyList()
                    ),
                    allCasesRollingAverageLineChartData = LineChartData(
                        label = "Rolling average",
                        values = emptyList()
                    ),
                    latestCasesBarChartData = BarChartData(
                        label = "Latest cases",
                        values = emptyList()
                    ),
                    latestCasesRollingAverageLineChartData = LineChartData(
                        label = "Rolling average",
                        values = emptyList()
                    )
                )

                coEvery { areaDetailUseCase.execute(areaCode) } returns listOf(
                    areaDetailModel
                ).asFlow()
                every { areaUiModelMapper.mapAreaDetailModel(areaDetailModel) } returns areaCasesModel

                val sut = areaViewModel(savedStateHandle)

                val statesObserver = sut.areaCases.test()
                val isLoadingObserver = sut.isLoading.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(areaCasesModel)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
            }
        }

    @Test
    fun `GIVEN area detail succeeds with stale data and syncAreaDetailUseCase fails WHEN viewmodel initialized THEN error state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val areaType = "utla"
                val savedStateHandle =
                    SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

                val caseModels = listOf(
                    CaseModel(
                        dailyLabConfirmedCases = 123,
                        date = LocalDate.ofEpochDay(0),
                        rollingAverage = 1.0
                    )
                )

                val now = LocalDateTime.now()
                val areaDetailModel = AreaDetailModel(
                    lastUpdatedAt = now.minusDays(1),
                    lastSyncedAt = now.minusDays(1),
                    allCases = caseModels,
                    latestCases = caseModels.takeLast(7)
                )

                val areaCasesModel = AreaCasesModel(
                    lastUpdatedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(0),
                        ZoneOffset.UTC
                    ),
                    allCasesChartData = BarChartData(
                        label = "All cases",
                        values = emptyList()
                    ),
                    allCasesRollingAverageLineChartData = LineChartData(
                        label = "Rolling average",
                        values = emptyList()
                    ),
                    latestCasesBarChartData = BarChartData(
                        label = "Latest cases",
                        values = emptyList()
                    ),
                    latestCasesRollingAverageLineChartData = LineChartData(
                        label = "Rolling average",
                        values = emptyList()
                    )
                )

                coEvery { areaDetailUseCase.execute(areaCode) } returns listOf(
                    areaDetailModel
                ).asFlow()
                coEvery { syncAreaDetailUseCase.execute(areaCode, areaType) } throws IOException()
                every { areaUiModelMapper.mapAreaDetailModel(areaDetailModel) } returns areaCasesModel

                val sut = areaViewModel(savedStateHandle)

                val statesObserver = sut.areaCases.test()
                val isLoadingObserver = sut.isLoading.test()
                val syncAreaError = sut.syncAreaError.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(areaCasesModel)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
                assertThat(syncAreaError.values[0]).isEqualTo(Event(false))
            }
        }

    @Test
    fun `GIVEN area detail succeeds with no sync data AND syncAreaDetailUseCase fails WHEN viewmodel initialized THEN error state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val areaType = "utla"
                val savedStateHandle =
                    SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

                val areaDetailModel = AreaDetailModel(
                    lastUpdatedAt = null,
                    lastSyncedAt = null,
                    allCases = emptyList(),
                    latestCases = emptyList()
                )

                coEvery { areaDetailUseCase.execute(areaCode) } returns listOf(
                    areaDetailModel
                ).asFlow()
                coEvery { syncAreaDetailUseCase.execute(areaCode, areaType) } throws IOException()

                val sut = areaViewModel(savedStateHandle)

                val statesObserver = sut.areaCases.test()
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
    fun `GIVEN area detail succeeds with no sync data AND syncAreaDetailUseCase fails with 304 WHEN viewmodel initialized THEN error state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val areaType = "utla"
                val savedStateHandle =
                    SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

                val areaDetailModel = AreaDetailModel(
                    lastUpdatedAt = null,
                    lastSyncedAt = null,
                    allCases = emptyList(),
                    latestCases = emptyList()
                )

                val areaCasesModel = AreaCasesModel(
                    lastUpdatedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(0),
                        ZoneOffset.UTC
                    ),
                    allCasesChartData = BarChartData(
                        label = "All cases",
                        values = emptyList()
                    ),
                    allCasesRollingAverageLineChartData = LineChartData(
                        label = "Rolling average",
                        values = emptyList()
                    ),
                    latestCasesBarChartData = BarChartData(
                        label = "Latest cases",
                        values = emptyList()
                    ),
                    latestCasesRollingAverageLineChartData = LineChartData(
                        label = "Rolling average",
                        values = emptyList()
                    )
                )

                val exception = mockk<HttpException>()
                every { exception.code() } returns 304

                coEvery { areaDetailUseCase.execute(areaCode) } returns listOf(
                    areaDetailModel
                ).asFlow()
                every { areaUiModelMapper.mapAreaDetailModel(areaDetailModel) } returns areaCasesModel
                coEvery { syncAreaDetailUseCase.execute(areaCode, areaType) } throws exception

                val sut = areaViewModel(savedStateHandle)

                val statesObserver = sut.areaCases.test()
                val isLoadingObserver = sut.isLoading.test()
                val syncAreaError = sut.syncAreaError.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(areaCasesModel)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
                assertThat(syncAreaError.values).isEmpty()
            }
        }

    @Test
    fun `GIVEN areaDetailUseCase fails WHEN viewmodel initialized THEN error state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val areaType = "utla"
                val savedStateHandle =
                    SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

                coEvery { areaDetailUseCase.execute(areaCode) } throws IOException()

                val sut = areaViewModel(savedStateHandle)

                val statesObserver = sut.areaCases.test()
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

            val areaCode = "AC-001"
            val areaType = "utla"
            val savedStateHandle =
                SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

            coEvery { areaDetailUseCase.execute(areaCode) } throws IOException()

            val sut = areaViewModel(savedStateHandle)

            verify(exactly = 1) { areaDetailUseCase.execute(areaCode) }

            sut.refresh()

            verify(exactly = 2) { areaDetailUseCase.execute(areaCode) }
        }

    @Test
    fun `GIVEN isSaved succeeds WHEN viewmodel initialized THEN saved state is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val areaType = "utla"
                val savedStateHandle =
                    SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

                val publisher = ConflatedBroadcastChannel(false)

                every { isSavedUseCase.execute(areaCode) } returns publisher.asFlow()

                val sut = areaViewModel(savedStateHandle)

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

            val areaCode = "AC-001"
            val areaType = "utla"
            val savedStateHandle =
                SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

            every { insertSavedAreaUseCase.execute(areaCode) } just Runs

            val sut = areaViewModel(savedStateHandle)

            sut.insertSavedArea()

            verify(exactly = 1) { insertSavedAreaUseCase.execute(areaCode) }
        }

    @Test
    fun `WHEN deleteSavedArea called THEN deleteSavedAreaUseCase is executed`() =
        testDispatcher.runBlockingTest {

            val areaCode = "AC-001"
            val areaType = "utla"
            val savedStateHandle =
                SavedStateHandle(mapOf("areaCode" to areaCode, "areaType" to areaType))

            every { deleteSavedAreaUseCase.execute(areaCode) } returns 1

            val sut = areaViewModel(savedStateHandle)

            sut.deleteSavedArea()

            verify(exactly = 1) { deleteSavedAreaUseCase.execute(areaCode) }
        }

    private fun areaViewModel(savedStateHandle: SavedStateHandle): AreaViewModel {
        return AreaViewModel(
            syncAreaDetailUseCase,
            areaDetailUseCase,
            isSavedUseCase,
            insertSavedAreaUseCase,
            deleteSavedAreaUseCase,
            TestCoroutineDispatchersImpl(testDispatcher),
            areaUiModelMapper,
            savedStateHandle
        )
    }
}
