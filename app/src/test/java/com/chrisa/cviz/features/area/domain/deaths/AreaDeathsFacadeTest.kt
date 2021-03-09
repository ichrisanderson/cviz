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

package com.chrisa.cviz.features.area.domain.deaths

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AreaDeathsFacadeTest {

    private val publishedDeathsUseCase: AreaDeathsUseCase = mockk(relaxed = true)
    private val onsDeathsUseCase: AreaDeathsUseCase = mockk(relaxed = true)

    private val sut = AreaDeathsFacade(publishedDeathsUseCase, onsDeathsUseCase)

    @Test
    fun `WHEN publishedDeaths called THEN publishedDeathsUseCase queried`() {
        sut.publishedDeaths(areaCode, areaType, areaLookup)

        verify { publishedDeathsUseCase.deaths(areaCode, areaType, areaLookup) }
    }

    @Test
    fun `WHEN onsDeaths called THEN publishedDeathsUseCase queried`() {
        sut.onsDeaths(areaCode, areaType, areaLookup)

        verify { onsDeathsUseCase.deaths(areaCode, areaType, areaLookup) }
    }

    companion object {
        private val areaCode = "E1"
        private val areaType = AreaType.REGION
        private val areaLookup = AreaLookupDto(
            lsoaCode = "E11011",
            lsoaName = "Soho",
            msoaCode = "E11011",
            msoaName = "Soho",
            ltlaCode = "E1101",
            ltlaName = "Westminster",
            utlaCode = "E1101",
            utlaName = "Westminster",
            nhsRegionCode = "E110",
            nhsRegionName = "London",
            nhsTrustCode = "GUYS",
            nhsTrustName = "St Guys",
            regionCode = "London",
            regionName = "E12000007",
            nationCode = Constants.ENGLAND_AREA_CODE,
            nationName = Constants.ENGLAND_AREA_NAME
        )
    }
}
