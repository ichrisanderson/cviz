package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.synchronization.DeathDataSynchronizer
import io.mockk.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class SynchronizeDeathsUseCaseTest {

    private val deathDataSynchronizer = mockk<DeathDataSynchronizer>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)

    private val sut = SynchronizeDeathsUseCase(deathDataSynchronizer)

    @Test
    fun `WHEN execute called THEN cased data synchronization is executed`() =
        testDispatcher.runBlockingTest {
            coEvery { deathDataSynchronizer.performSync() } just Runs
            sut.execute(testCoroutineScope)
            coVerify { deathDataSynchronizer.performSync() }
        }
}
