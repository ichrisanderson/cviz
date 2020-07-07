package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.Bootstrapper
import io.mockk.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class BootstrapDataUseCaseTest {

    private val bootStrapper = mockk<Bootstrapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = BootstrapDataUseCase(bootStrapper)

    @Test
    fun `WHEN execute called THEN bootstrapData is executed`() =
        testDispatcher.runBlockingTest {
            coEvery { bootStrapper.bootstrapData() } just Runs
            sut.execute()
            coVerify { bootStrapper.bootstrapData() }
        }
}

