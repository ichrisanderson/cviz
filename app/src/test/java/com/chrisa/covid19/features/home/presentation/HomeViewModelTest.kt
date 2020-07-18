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

package com.chrisa.covid19.features.home.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.home.domain.LoadSavedAreaCasesUseCase
import com.chrisa.covid19.features.home.domain.models.AreaCaseListModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val loadSavedAreaCasesUseCase = mockk<LoadSavedAreaCasesUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN load saved area cases succeeds with non empty state WHEN viewmodel initialized THEN list of areas are emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCaseListModel = AreaCaseListModel(
                    areaCode = "UK001",
                    areaName = "England",
                    dailyTotalLabConfirmedCasesRate = 111.0,
                    totalLabConfirmedCases = 22,
                    changeInDailyTotalLabConfirmedCasesRate = 11.0,
                    changeInTotalLabConfirmedCases = 11,
                    totalLabConfirmedCasesLastWeek = 11
                )

                val allCases = listOf(areaCaseListModel)

                val allCasesFlow = flow {
                    emit(allCases)
                }

                every { loadSavedAreaCasesUseCase.execute() } returns allCasesFlow

                val sut = HomeViewModel(
                    loadSavedAreaCasesUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher)
                )

                val areCasesObserver = sut.areaCases.test()
                val isLoadingObserver = sut.isLoading.test()
                val isEmptyObserver = sut.isEmpty.test()

                runCurrent()

                assertThat(areCasesObserver.values[0]).isEqualTo(listOf(areaCaseListModel))
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
                assertThat(isEmptyObserver.values[0]).isEqualTo(false)
            }
        }

    @Test
    fun `GIVEN load saved area cases succeeds with empty state WHEN viewmodel initialized THEN empty list is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val allCases = emptyList<AreaCaseListModel>()

                val allCasesFlow = flow {
                    emit(allCases)
                }

                every { loadSavedAreaCasesUseCase.execute() } returns allCasesFlow

                val sut = HomeViewModel(
                    loadSavedAreaCasesUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher)
                )

                val areCasesObserver = sut.areaCases.test()
                val isLoadingObserver = sut.isLoading.test()
                val isEmptyObserver = sut.isEmpty.test()

                runCurrent()

                assertThat(areCasesObserver.values[0]).isEqualTo(listOf<AreaCaseListModel>())
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
                assertThat(isEmptyObserver.values[0]).isEqualTo(true)
            }
        }
}
