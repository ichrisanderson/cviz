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

package com.chrisa.cviz.features.home.domain

import com.chrisa.cviz.core.data.synchronisation.DataSynchroniser
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RefreshDataUseCaseTest {

    private val dataSynchroniser = mockk<DataSynchroniser>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val dispatchers = TestCoroutineDispatchersImpl(testDispatcher)
    private val sut = RefreshDataUseCase(dispatchers, dataSynchroniser)

    @Test
    fun `WHEN execute called THEN performSync is called`() =
        testDispatcher.runBlockingTest {
            sut.execute()

            coVerify { dataSynchroniser.syncData() }
        }
}
