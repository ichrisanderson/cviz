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
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.AreaLookupDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class AreaLookupUseCase @Inject constructor(
    private val areaLookupDataSynchroniser: AreaLookupDataSynchroniser,
    private val areaLookupDataSource: AreaLookupDataSource
) {

    fun areaLookup(areaCode: String, areaType: AreaType): AreaLookupDto? {
        return when (areaType) {
            AreaType.MSOA -> areaLookupDataSource.areaLookupByMsoa(areaCode)
            AreaType.LTLA -> areaLookupDataSource.areaLookupByLtla(areaCode)
            AreaType.UTLA -> areaLookupDataSource.areaLookupByUtla(areaCode)
            AreaType.REGION -> areaLookupDataSource.areaLookupByRegion(areaCode)
            AreaType.NHS_REGION -> areaLookupDataSource.areaLookupByNhsRegion(areaCode)
            AreaType.NHS_TRUST -> areaLookupDataSource.areaLookupByNhsTrust(areaCode)
            AreaType.NATION,
            AreaType.OVERVIEW -> null
        }
    }

    suspend fun syncAreaLookup(areaCode: String, areaType: AreaType) {
        areaLookupDataSynchroniser.performSync(areaCode, areaType)
    }

    fun areaName(areaType: AreaType, areaLookupDto: AreaLookupDto): String {
        return when (areaType) {
            AreaType.MSOA -> areaLookupDto.msoaName!!
            AreaType.LTLA -> areaLookupDto.ltlaName
            AreaType.UTLA -> areaLookupDto.utlaName
            AreaType.REGION -> areaLookupDto.regionName!!
            AreaType.NHS_REGION -> areaLookupDto.nhsRegionName!!
            AreaType.NHS_TRUST -> areaLookupDto.nhsTrustName!!
            AreaType.NATION -> areaLookupDto.nationName
            AreaType.OVERVIEW -> Constants.UK_AREA_NAME
        }
    }
}
