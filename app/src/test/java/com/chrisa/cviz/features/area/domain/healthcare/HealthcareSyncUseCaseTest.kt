/*
 * Copyright 2021 Chris Anderson.
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

package com.chrisa.cviz.features.area.domain.healthcare

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import timber.log.Timber

@ExperimentalCoroutinesApi
class HealthcareSyncUseCaseTest {

    private val healthcareDataSynchroniser: HealthcareDataSynchroniser = mockk()
    private val testDispatcher = TestCoroutineDispatcher()
    private val sut = HealthcareSyncUseCase(healthcareDataSynchroniser)

    @Test
    fun `WHEN healthcare data synchroniser runs THEN sync succeeds`() =
        testDispatcher.runBlockingTest {
            val areaCode = "E1"
            val areaType = AreaType.REGION
            coEvery {
                healthcareDataSynchroniser.performSync(areaCode, areaType)
            } just Runs

            val result = sut.syncHospitalData(areaCode, areaType)

            assertThat(result).isEqualTo(Unit)
        }

    @Test
    fun `WHEN healthcare data synchroniser errors THEN error is logged`() =
        testDispatcher.runBlockingTest {
            val areaCode = "E1"
            val areaType = AreaType.REGION
            val error = IOException()
            mockkStatic(Timber::class)
            every { Timber.e(error) } just Runs
            coEvery {
                healthcareDataSynchroniser.performSync(areaCode, areaType)
            } throws error

            sut.syncHospitalData(areaCode, areaType)

            verify { Timber.e(error) }
        }
}
