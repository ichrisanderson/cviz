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

import com.chrisa.cviz.features.area.data.AdmissionsDataSource
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AreaCodesAdmissionsUseCaseTest {

    private val admissionsDataSource: AdmissionsDataSource = mockk(relaxed = true)
    private val sut = AreaCodesAdmissionsUseCase(admissionsDataSource)

    @Test
    fun `WHEN admissionsForAreaCodes called THEN admissionsDataSource queried`() {
        val areaCodes = listOf("1", "2", "3")

        sut.admissionsForAreaCodes(areaCodes)

        verify(exactly = 1) { admissionsDataSource.admissionsForAreaCodes(areaCodes) }
    }
}
