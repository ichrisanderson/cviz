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

package com.chrisa.covid19.features.startup.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.features.startup.domain.BootstrapDataUseCase
import com.chrisa.covid19.features.startup.domain.ClearNonSavedAreaCacheDataUseCase
import com.chrisa.covid19.features.startup.domain.SynchroniseAreasUseCase
import com.chrisa.covid19.features.startup.domain.SynchroniseOverviewDataUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StartupViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val bootstrapDataUseCase = mockk<BootstrapDataUseCase>()
    private val clearNonSavedAreaCacheDataUseCase = mockk<ClearNonSavedAreaCacheDataUseCase>()
    private val synchroniseCasesUseCase = mockk<SynchroniseAreasUseCase>()
    private val synchroniseOverviewDataUseCase = mockk<SynchroniseOverviewDataUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN bootstap succeeds WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                coEvery { clearNonSavedAreaCacheDataUseCase.execute() } just Runs
                coEvery { bootstrapDataUseCase.execute() } just Runs
                coEvery { synchroniseCasesUseCase.execute(any()) } just Runs
                coEvery { synchroniseOverviewDataUseCase.execute(any()) } just Runs

                val sut = StartupViewModel(
                    bootstrapDataUseCase,
                    clearNonSavedAreaCacheDataUseCase,
                    synchroniseCasesUseCase,
                    synchroniseOverviewDataUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher)
                )

                val statesObserver = sut.startupState.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(StartupState.Loading)
                assertThat(statesObserver.values[1]).isEqualTo(StartupState.Success)
            }
        }
}
