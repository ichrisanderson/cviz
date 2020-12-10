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
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaCodeMapper
import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class HealthcareUseCase @Inject constructor(
    private val healthcareDataSynchroniser: HealthcareDataSynchroniser,
    private val areaDataSource: AreaDataSource,
    private val areaCodeMapper: AreaCodeMapper
) {

    fun healthcareData(areaCode: String, areaLookup: AreaLookupDto?): AreaDailyDataDto {
        val nhsRegion = healthCareRegion(areaCode, areaLookup)
        val healthCareData = areaDataSource.healthcareData(nhsRegion.code)
        return AreaDailyDataDto(nhsRegion.name, healthCareData)
    }

    fun healthCareRegion(areaCode: String, areaLookup: AreaLookupDto?): AreaDto {
        val nhsRegionCode = areaLookup?.nhsRegionCode
        return if (nhsRegionCode != null) {
            AreaDto(nhsRegionCode, areaLookup.nhsRegionName.orEmpty(), AreaType.NHS_REGION)
        } else {
            areaCodeMapper.defaultAreaDto(areaCode)
        }
    }

    suspend fun syncHospitalData(areaCode: String, areaType: AreaType) {
        try {
            healthcareDataSynchroniser.performSync(areaCode, areaType)
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }
}
