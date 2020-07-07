package com.chrisa.covid19.features.startup.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.startup.domain.BootstrapDataUseCase
import com.chrisa.covid19.features.startup.domain.SynchronizeCasesUseCase
import com.chrisa.covid19.features.startup.domain.SynchronizeDeathsUseCase
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class StartupViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val bootstrapDataUseCase = mockk<BootstrapDataUseCase>()
    private val synchronizeCasesUseCase = mockk<SynchronizeCasesUseCase>()
    private val synchronizeDeathsUseCase = mockk<SynchronizeDeathsUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN bootstap succeeds WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                coEvery { bootstrapDataUseCase.execute() } just Runs
                coEvery { synchronizeCasesUseCase.execute(any()) } just Runs
                coEvery { synchronizeDeathsUseCase.execute(any()) } just Runs

                val sut = StartupViewModel(
                    bootstrapDataUseCase,
                    synchronizeCasesUseCase,
                    synchronizeDeathsUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher)
                )

                val statesObserver = sut.startupState.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(StartupState.Loading)
                assertThat(statesObserver.values[1]).isEqualTo(StartupState.Success)
            }
        }
}
