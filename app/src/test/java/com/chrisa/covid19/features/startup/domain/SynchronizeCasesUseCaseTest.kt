package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.synchronization.CaseDataSynchronizer
import io.mockk.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class SynchronizeCasesUseCaseTest {

    private val caseDataSynchronizer = mockk<CaseDataSynchronizer>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)

    private val sut = SynchronizeCasesUseCase(caseDataSynchronizer)

    @Test
    fun `WHEN execute called THEN cased data synchronization is executed`() =
        testDispatcher.runBlockingTest {
            coEvery { caseDataSynchronizer.performSync() } just Runs
            sut.execute(testCoroutineScope)
            coVerify { caseDataSynchronizer.performSync() }
        }
}
