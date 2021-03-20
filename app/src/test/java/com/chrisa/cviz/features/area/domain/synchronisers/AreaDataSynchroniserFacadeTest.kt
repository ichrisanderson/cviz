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

package com.chrisa.cviz.features.area.domain.synchronisers

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.domain.AreaDataSynchroniserFacade
import com.chrisa.cviz.features.area.domain.AreaDetailSynchroniser
import com.chrisa.cviz.features.area.domain.AreaLookupUseCase
import com.chrisa.cviz.features.area.domain.SoaDataUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AreaDataSynchroniserFacadeTest {
    private val areaLookupUseCase: AreaLookupUseCase = mockk(relaxed = true)
    private val soaDataUseCase: SoaDataUseCase = mockk(relaxed = true)
    private val areaDetailSynchroniser: AreaDetailSynchroniser = mockk(relaxed = true)

    private val sut =
        AreaDataSynchroniserFacade(areaLookupUseCase, soaDataUseCase, areaDetailSynchroniser)

    @Test
    fun `WHEN performSync called THEN sync is triggered`() = runBlocking {
        val areaCode = "area1"
        val areaType = AreaType.UTLA
        sut.performSync(areaCode, areaType)

        coVerify { areaLookupUseCase.syncAreaLookup(areaCode, areaType) }
        coVerify { soaDataUseCase.syncSoaData(areaCode, areaType) }
        coVerify { areaDetailSynchroniser.performSync(areaCode, areaType) }
    }
}
