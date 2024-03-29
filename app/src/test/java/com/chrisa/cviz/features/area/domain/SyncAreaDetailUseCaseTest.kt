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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.AreaDataSynchroniser
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Test

@ExperimentalCoroutinesApi
class SyncAreaDetailUseCaseTest {

    private val dataSynchroniser = mockk<AreaDataSynchroniser>(relaxed = true)
    private val sut = SyncAreaDetailUseCase(dataSynchroniser)

    @Test
    fun `WHEN execute called THEN performSync is called`() =
        runBlocking {
            val areaCode = "1234"
            val areaType = AreaType.OVERVIEW

            sut.execute(areaCode, areaType.value)

            coVerify { dataSynchroniser.performSync(areaCode, areaType) }
        }
}
