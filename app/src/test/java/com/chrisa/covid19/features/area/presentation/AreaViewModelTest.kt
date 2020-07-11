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
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.area.domain.AreaDetailUseCase
import com.chrisa.covid19.features.area.domain.IsSavedUseCase
import com.chrisa.covid19.features.area.domain.SaveAreaUseCase
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.presentation.mappers.AreaCasesModelMapper
import com.chrisa.covid19.features.area.presentation.models.AreaCasesModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.Date
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

    private val areaDetailUseCase = mockk<AreaDetailUseCase>()
    private val isSavedUseCase = mockk<IsSavedUseCase>()
    private val saveAreaUseCase = mockk<SaveAreaUseCase>()
    private val areaUiModelMapper = mockk<AreaCasesModelMapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN area detail usecase succeeds WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val savedStateHandle = SavedStateHandle(mapOf("areaCode" to areaCode))

                val caseModels = listOf(
                    CaseModel(
                        date = Date(0),
                        dailyLabConfirmedCases = 123
                    )
                )

                val areaDetailModel = AreaDetailModel(
                    lastUpdatedAt = Date(0),
                    allCases = caseModels,
                    latestCases = caseModels.takeLast(7)
                )

                val areaCasesModel = AreaCasesModel(
                    lastUpdatedAt = Date(0),
                    allCasesChartData = BarChartData(
                        label = "All cases",
                        values = emptyList()
                    ),
                    latestCasesChartData = BarChartData(
                        label = "Latest cases",
                        values = emptyList()
                    )
                )

                every { areaDetailUseCase.execute(areaCode) } returns areaDetailModel
                every { areaUiModelMapper.mapAreaDetailModel(areaDetailModel) } returns areaCasesModel

                val sut = AreaViewModel(
                    areaDetailUseCase,
                    isSavedUseCase,
                    saveAreaUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher),
                    areaUiModelMapper,
                    savedStateHandle
                )

                val statesObserver = sut.areaCases.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(areaCasesModel)
            }
        }

    @Test
    fun `GIVEN isSaved usecase succeeds WHEN viewmodel initialized THEN saved state is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val savedStateHandle = SavedStateHandle(mapOf("areaCode" to areaCode))

                val publisher = ConflatedBroadcastChannel(false)

                every { isSavedUseCase.execute(areaCode) } returns publisher.asFlow()

                val sut = AreaViewModel(
                    areaDetailUseCase,
                    isSavedUseCase,
                    saveAreaUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher),
                    areaUiModelMapper,
                    savedStateHandle
                )

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
    fun `WHEN saveArea called THEN saveAreaUseCase is executed`() =
        testDispatcher.runBlockingTest {

            val areaCode = "AC-001"
            val savedStateHandle = SavedStateHandle(mapOf("areaCode" to areaCode))

            every { saveAreaUseCase.execute(areaCode) } just Runs

            val sut = AreaViewModel(
                areaDetailUseCase,
                isSavedUseCase,
                saveAreaUseCase,
                TestCoroutineDispatchersImpl(testDispatcher),
                areaUiModelMapper,
                savedStateHandle
            )

            sut.saveArea()

            verify(exactly = 1) { saveAreaUseCase.execute(areaCode) }
        }
}
