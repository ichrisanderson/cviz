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

package com.chrisa.cviz.features.home.presentation.summarylist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.cviz.core.util.test
import com.chrisa.cviz.features.home.domain.LoadAreaSummariesUseCase
import com.chrisa.cviz.features.home.domain.RefreshDataUseCase
import com.chrisa.cviz.features.home.domain.models.SortOption
import com.chrisa.cviz.features.home.domain.models.SummaryModel
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
class SummaryListViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val loadAreaSummariesUseCase = mockk<LoadAreaSummariesUseCase>()
    private val refreshDataUseCase = mockk<RefreshDataUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN load area data succeeds WHEN viewmodel initialized THEN summary list data is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                val sortOption = SortOption.RisingCases
                val summaryModel = SummaryModel(
                    position = 1,
                    areaCode = "1234",
                    areaName = "Lambeth",
                    areaType = "ltla",
                    changeInCases = 22,
                    currentNewCases = 33,
                    changeInInfectionRate = 100.0,
                    currentInfectionRate = 10.0
                )
                val summaries = listOf(summaryModel)
                val savedStateHandle = SavedStateHandle(mapOf("sortOption" to sortOption))
                every { loadAreaSummariesUseCase.execute(sortOption) } returns listOf(summaries).asFlow()

                val sut = SummaryListViewModel(
                    loadAreaSummariesUseCase,
                    refreshDataUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher),
                    savedStateHandle
                )

                val summariesObserver = sut.summaries.test()
                val isLoadingObserver = sut.isLoading.test()

                runCurrent()

                assertThat(summariesObserver.values[0]).isEqualTo(summaries)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
            }
        }

    @Test
    fun `WHEN viewmodel refreshed THEN refresh state is emitted`() =
        testDispatcher.runBlockingTest {
            val sortOption = SortOption.RisingCases
            every { loadAreaSummariesUseCase.execute(sortOption) } returns emptyList<List<SummaryModel>>().asFlow()
            coEvery { refreshDataUseCase.execute() } just Runs
            val savedStateHandle = SavedStateHandle(mapOf("sortOption" to sortOption))
            val sut = SummaryListViewModel(
                loadAreaSummariesUseCase,
                refreshDataUseCase,
                TestCoroutineDispatchersImpl(testDispatcher),
                savedStateHandle
            )
            val isRefreshingObserver = sut.isRefreshing.test()

            sut.refresh()

            assertThat(isRefreshingObserver.values[0]).isEqualTo(true)
            assertThat(isRefreshingObserver.values[1]).isEqualTo(false)
        }

    @Test
    fun `WHEN viewmodel refresh fails THEN is refreshing set to false`() =
        testDispatcher.runBlockingTest {
            val sortOption = SortOption.RisingCases
            every { loadAreaSummariesUseCase.execute(sortOption) } returns emptyList<List<SummaryModel>>().asFlow()
            coEvery { refreshDataUseCase.execute() } throws IOException()
            val savedStateHandle = SavedStateHandle(mapOf("sortOption" to sortOption))
            val sut = SummaryListViewModel(
                loadAreaSummariesUseCase,
                refreshDataUseCase,
                TestCoroutineDispatchersImpl(testDispatcher),
                savedStateHandle
            )
            val isRefreshingObserver = sut.isRefreshing.test()

            sut.refresh()

            assertThat(isRefreshingObserver.values[0]).isEqualTo(true)
            assertThat(isRefreshingObserver.values[1]).isEqualTo(false)
        }
}
