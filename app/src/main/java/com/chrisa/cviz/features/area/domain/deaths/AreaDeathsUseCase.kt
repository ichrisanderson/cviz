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

package com.chrisa.cviz.features.area.domain.deaths

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.AreaDeathsDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.domain.AreaLookupUseCase

class AreaDeathsUseCase constructor(
    private val areaLookupUseCase: AreaLookupUseCase,
    private val deathsDataSource: AreaDeathsDataSource,
    private val areaCodeResolver: AreaCodeResolver
) {
    fun deaths(areaCode: String, areaType: AreaType, areaLookup: AreaLookupDto?): AreaDailyDataDto =
        if (areaLookup != null) {
            areaDeaths(areaCode, areaType, areaLookup)
                ?: regionDeaths(areaLookup)
                ?: nationDeaths(areaLookup)
                ?: defaultDeaths(areaCode)
                ?: ukDeaths()
        } else {
            defaultDeaths(areaCode)
                ?: ukDeaths()
        }

    private fun areaDeaths(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto
    ): AreaDailyDataDto? =
        deathsDataSource.deaths(areaCode).nullOnEmpty()
            ?.let { data ->
                AreaDailyDataDto(areaLookupUseCase.areaName(areaType, areaLookup), data)
            }

    private fun regionDeaths(areaLookup: AreaLookupDto): AreaDailyDataDto? =
        areaLookup.regionCode
            ?.let { deathsDataSource.deaths(areaLookup.regionCode) }
            ?.nullOnEmpty()
            ?.let { data -> AreaDailyDataDto(areaLookup.regionName!!, data) }

    private fun nationDeaths(areaLookup: AreaLookupDto): AreaDailyDataDto? =
        areaLookup.nationCode
            .let { deathsDataSource.deaths(areaLookup.nationCode) }
            .nullOnEmpty()
            ?.let { data -> AreaDailyDataDto(areaLookup.nationName, data) }

    private fun defaultDeaths(areaCode: String): AreaDailyDataDto? {
        val areaDto = areaCodeResolver.defaultAreaDto(areaCode)
        return deathsDataSource.deaths(areaDto.code).nullOnEmpty()
            ?.let { data -> AreaDailyDataDto(areaDto.name, data) }
    }

    private fun ukDeaths(): AreaDailyDataDto =
        AreaDailyDataDto(Constants.UK_AREA_NAME, deathsDataSource.deaths(Constants.UK_AREA_CODE))

    companion object {
        private fun List<DailyData>.nullOnEmpty(): List<DailyData>? =
            if (isEmpty()) null else this
    }
}
