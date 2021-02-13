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

package com.chrisa.cviz.features.home.presentation.savedareas

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.cviz.core.util.test
import com.chrisa.cviz.features.home.domain.LoadSavedAreasUseCase
import com.chrisa.cviz.features.home.domain.RefreshDataUseCase
import com.chrisa.cviz.features.home.domain.models.SavedAreaSummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class SavedAreasViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val loadSavedAreasUseCase = mockk<LoadSavedAreasUseCase>()
    private val refreshDataUseCase = mockk<RefreshDataUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val coroutineDispatchers = TestCoroutineDispatchersImpl(testDispatcher)

    @Test
    fun `GIVEN load home data cases succeeds WHEN viewmodel initialized THEN home screen data is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val summaryModel = SavedAreaSummaryModel(
                    areaCode = "1234",
                    areaName = "Lambeth",
                    areaType = "ltla",
                    changeInCases = 22,
                    currentNewCases = 33,
                    changeInInfectionRate = 100.0,
                    currentInfectionRate = 10.0
                )

                every { loadSavedAreasUseCase.execute() } returns listOf(listOf(summaryModel)).asFlow()

                val sut = SavedAreasViewModel(
                    loadSavedAreasUseCase,
                    refreshDataUseCase,
                    coroutineDispatchers
                )

                val savedAreasObserver = sut.savedAreas.test()
                val isLoadingObserver = sut.isLoading.test()

                runCurrent()

                assertThat(savedAreasObserver.values[0]).isEqualTo(listOf(summaryModel))
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
            }
        }

    @Test
    fun `WHEN viewmodel refreshed THEN refresh state is emitted`() =
        testDispatcher.runBlockingTest {
            every { loadSavedAreasUseCase.execute() } returns emptyList<List<SavedAreaSummaryModel>>().asFlow()
            coEvery { refreshDataUseCase.execute() } just Runs
            val sut = SavedAreasViewModel(
                loadSavedAreasUseCase,
                refreshDataUseCase,
                coroutineDispatchers
            )
            val isRefreshingObserver = sut.isRefreshing.test()

            sut.refresh()

            assertThat(isRefreshingObserver.values[0]).isEqualTo(true)
            assertThat(isRefreshingObserver.values[1]).isEqualTo(false)
        }

    @Test
    fun `WHEN viewmodel refresh fails THEN is refreshing set to false`() =
        testDispatcher.runBlockingTest {
            every { loadSavedAreasUseCase.execute() } returns emptyList<List<SavedAreaSummaryModel>>().asFlow()
            coEvery { refreshDataUseCase.execute() } throws IOException()
            val sut = SavedAreasViewModel(
                loadSavedAreasUseCase,
                refreshDataUseCase,
                coroutineDispatchers
            )
            val isRefreshingObserver = sut.isRefreshing.test()

            sut.refresh()

            assertThat(isRefreshingObserver.values[0]).isEqualTo(true)
            assertThat(isRefreshingObserver.values[1]).isEqualTo(false)
        }
}
