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
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class LocalHealthcareDataSynchroniserTest {

    private val healthcareLookupDataSynchroniser: HealthcareLookupDataSynchroniser =
        mockk(relaxed = true)
    private val nhsRegionDataSynchroniser: NhsRegionDataSynchroniser =
        mockk(relaxed = true)
    private val sut = LocalHealthcareDataSynchroniser(
        healthcareLookupDataSynchroniser,
        nhsRegionDataSynchroniser
    )

    @Test
    fun `WHEN execute called THEN healthcare lookups synchronised`() = runBlockingTest {
        val areaCode = "area1"
        val areaType = AreaType.UTLA

        sut.execute(areaCode, areaType, areaLookupDto)

        coVerify { healthcareLookupDataSynchroniser.execute(areaCode, areaType, areaLookupDto) }
    }

    @Test
    fun `WHEN execute called THEN healthcare regions synchronised`() = runBlockingTest {
        val areaCode = "area1"
        val areaType = AreaType.UTLA

        sut.execute(areaCode, areaType, areaLookupDto)

        coVerify { nhsRegionDataSynchroniser.execute(areaCode, areaLookupDto) }
    }

    companion object {

        private val areaLookupDto = AreaLookupDto(
            lsoaCode = "",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "",
            utlaName = "",
            nhsRegionCode = null,
            nhsRegionName = null,
            nhsTrustCode = null,
            nhsTrustName = null,
            regionCode = "",
            regionName = null,
            nationCode = "",
            nationName = ""
        )
    }
}
