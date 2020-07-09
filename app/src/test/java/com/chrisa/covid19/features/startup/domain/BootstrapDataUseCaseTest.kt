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

package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.Bootstrapper
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
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
