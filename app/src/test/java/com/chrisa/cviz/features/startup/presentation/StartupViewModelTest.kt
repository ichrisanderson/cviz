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

package com.chrisa.cviz.features.startup.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.cviz.core.util.test
import com.chrisa.cviz.features.startup.domain.StartupResult
import com.chrisa.cviz.features.startup.domain.StartupUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.plaidapp.core.util.event.Event
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

    private val startupUseCase = mockk<StartupUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val dispatchers = TestCoroutineDispatchersImpl(testDispatcher)

    @Test
    fun `GIVEN startup shows home screen WHEN viewmodel initialized THEN home screen event emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                coEvery { startupUseCase.execute() } returns StartupResult.ShowHomeScreen
                val sut = StartupViewModel(startupUseCase, dispatchers)
                val navigateHomeObserver = sut.navigateHome.test()

                runCurrent()

                assertThat(navigateHomeObserver.values[0]).isEqualTo(Event(Unit))
            }
        }

    @Test
    fun `GIVEN startup shows fatal error WHEN viewmodel initialized THEN fatal error emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {
                coEvery { startupUseCase.execute() } returns StartupResult.ShowFatalError
                val sut = StartupViewModel(startupUseCase, dispatchers)
                val syncErrorObserver = sut.syncError.test()

                runCurrent()

                assertThat(syncErrorObserver.values[0]).isEqualTo(Event(Unit))
            }
        }
}
