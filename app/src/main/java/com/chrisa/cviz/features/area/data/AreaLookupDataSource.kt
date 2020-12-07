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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import javax.inject.Inject

class AreaLookupDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val areaCodeMapper: AreaCodeMapper
) {

    fun healthCareArea(areaCode: String, areaType: AreaType): AreaDto {
        val dao = appDatabase.areaLookupDao()
        return when (areaType) {
            AreaType.LTLA -> areaDto(areaCode, dao.byLtla(areaCode))
            AreaType.UTLA -> areaDto(areaCode, dao.byUtla(areaCode))
            AreaType.REGION -> areaDto(areaCode, dao.byRegion(areaCode))
            AreaType.NHS_REGION -> areaDto(areaCode, dao.byNhsRegion(areaCode))
            AreaType.NATION,
            AreaType.OVERVIEW -> areaCodeMapper.defaultAreaDto(areaCode)
        }
    }

    private fun areaDto(areaCode: String, lookup: AreaLookupEntity?): AreaDto {
        val regionCode = lookup?.nhsRegionCode
        return if (regionCode != null) {
            AreaDto(regionCode, lookup.nhsRegionName.orEmpty(), AreaType.NHS_REGION)
        } else {
            areaCodeMapper.defaultAreaDto(areaCode)
        }
    }
}
