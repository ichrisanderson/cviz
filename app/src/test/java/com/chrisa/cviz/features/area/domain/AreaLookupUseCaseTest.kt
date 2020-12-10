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

import com.chrisa.cviz.core.data.synchronisation.AreaLookupDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaLookupDataSource
import io.mockk.mockk

class AreaLookupUseCaseTest {

    private val areaLookupDataSynchroniser = mockk<AreaLookupDataSynchroniser>()
    private val areaLookupDataSource = mockk<AreaLookupDataSource>()
    private val sut = AreaLookupUseCase(areaLookupDataSynchroniser, areaLookupDataSource)

//    @Test
//    fun `GIVEN healthcare area exists WHEN healthCareArea called THEN area data returned`() {
//        val areas = listOf(
//            AreaDto("1", "", AreaType.REGION),
//            AreaDto("2", "", AreaType.UTLA),
//            AreaDto("3", "", AreaType.LTLA)
//        )
//        every { areaLookupDao.byRegion("1") } returns lookupEntity
//        every { areaLookupDao.byUtla("2") } returns lookupEntity
//        every { areaLookupDao.byLtla("3") } returns lookupEntity
//
//        areas.forEach { area ->
//            val areaDto = sut.healthCareArea(area.code, area.regionType)
//
//            assertThat(areaDto).isEqualTo(
//                AreaDto(
//                    code = lookupEntity.nhsRegionCode!!,
//                    name = lookupEntity.nhsRegionName!!,
//                    regionType = AreaType.NHS_REGION
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `GIVEN healthcare area does not exist WHEN healthCareArea called THEN default data returned`() {
//        val areas = listOf(
//            AreaDto("1", "", AreaType.REGION),
//            AreaDto("2", "", AreaType.UTLA),
//            AreaDto("3", "", AreaType.LTLA)
//        )
//        every { areaLookupDao.byRegion("1") } returns lookupEntityWithoutNhsRegion
//        every { areaLookupDao.byUtla("2") } returns lookupEntityWithoutNhsRegion
//        every { areaLookupDao.byLtla("3") } returns lookupEntityWithoutNhsRegion
//
//        areas.forEach { area ->
//            val areaDto = sut.healthCareArea(area.code, area.regionType)
//
//            assertThat(areaDto).isEqualTo(defaultArea)
//        }
//    }
//
//    @Test
//    fun `GIVEN non-lookup area WHEN healthCareArea called THEN default area data returned`() {
//        val areas = listOf(
//            AreaDto("K1", "", AreaType.OVERVIEW),
//            AreaDto("E2", "", AreaType.NATION)
//        )
//
//        areas.forEach { area ->
//            val areaDto = sut.healthCareArea(area.code, area.regionType)
//
//            assertThat(areaDto).isEqualTo(defaultArea)
//        }
//    }
//
//    companion object {
//        val lookupEntity = AreaLookupEntity(
//            lsoaCode = "",
//            lsoaName = null,
//            msoaCode = "",
//            msoaName = null,
//            ltlaCode = "",
//            ltlaName = "",
//            utlaCode = "",
//            utlaName = "",
//            nhsRegionCode = "NHS_1",
//            nhsRegionName = "NHS",
//            regionCode = "",
//            regionName = null,
//            nationCode = "",
//            nationName = ""
//        )
//        val lookupEntityWithoutNhsRegion = lookupEntity.copy(
//            nhsRegionCode = null,
//            nhsRegionName = null
//        )
//        val defaultArea = AreaDto(
//            code = Constants.UK_AREA_CODE,
//            name = "United Kingdom",
//            regionType = AreaType.OVERVIEW
//        )
//    }
}
