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
import com.chrisa.cviz.features.area.data.AreaDeathsDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto

class AreaDeathsUseCase constructor(
    private val areaLookupUseCase: AreaLookupUseCase,
    private val deathsDataSource: AreaDeathsDataSource
) {
    fun deaths(areaCode: String, areaType: AreaType): AreaDailyDataDto {
        val areaLookup = areaLookupUseCase.areaLookup(areaCode, areaType)
        if (areaLookup != null) {
            val areaDeaths = deathsDataSource.deaths(areaCode)
            if (areaDeaths.isNotEmpty()) {
                val areaName = areaLookupUseCase.areaName(areaType, areaLookup)
                return AreaDailyDataDto(areaName, areaDeaths)
            }
            if (areaLookup.regionCode != null) {
                val regionDeaths = deathsDataSource.deaths(areaLookup.regionCode)
                if (regionDeaths.isNotEmpty()) {
                    return AreaDailyDataDto(areaLookup.regionName!!, regionDeaths)
                }
            }
            val nationDeaths = deathsDataSource.deaths(areaLookup.nationCode)
            if (nationDeaths.isNotEmpty()) {
                return AreaDailyDataDto(areaLookup.nationName, nationDeaths)
            }
        }
        val overviewDeaths = deathsDataSource.deaths(Constants.UK_AREA_CODE)
        return AreaDailyDataDto("United Kingdom", overviewDeaths)
    }
}
