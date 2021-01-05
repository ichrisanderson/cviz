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
import com.chrisa.cviz.core.data.db.HealthcareLookupEntity
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.HealthcareDataSource
import com.chrisa.cviz.features.area.data.HealthcareLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class HealthcareUseCase @Inject constructor(
    private val healthcareDataSynchroniser: HealthcareDataSynchroniser,
    private val healthcareDataSource: HealthcareDataSource,
    private val areaCodeResolver: AreaCodeResolver,
    private val healthcareLookupDataSource: HealthcareLookupDataSource
) {

    fun healthcareData(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ): AreaDailyDataCollection {
        val healthcareLookups = healthcareLookups(areaType, areaLookup)
        return when {
            healthcareLookups.isEmpty() || areaLookup == null -> {
                val nhsRegion = healthCareRegion(areaCode, areaType, areaLookup)
                singleTrustData(nhsRegion)
            }
            else -> {
                multiTrustData(areaLookup, healthcareLookups)
            }
        }
    }

    private fun healthcareLookups(
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ) =
        if (areaLookup != null && (areaType == AreaType.UTLA || areaType == AreaType.LTLA)) {
            healthcareLookupDataSource.healthcareLookups(areaLookup.utlaCode)
        } else {
            emptyList()
        }

    private fun multiTrustData(
        areaLookupDto: AreaLookupDto,
        healthcareLookups: List<HealthcareLookupEntity>
    ): AreaDailyDataCollection {
        val data = healthcareData(healthcareLookups.map { it.nhsTrustCode })
        return AreaDailyDataCollection(areaLookupDto.utlaName, data)
    }

    private fun singleTrustData(nhsRegion: AreaDto): AreaDailyDataCollection {
        val healthCareData = healthcareDataSource.healthcareData(nhsRegion.code)
        return AreaDailyDataCollection(
            nhsRegion.name,
            listOf(AreaDailyDataDto(nhsRegion.name, healthCareData))
        )
    }

    fun healthCareRegion(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ): AreaDto {
        val nhsTrustCode = areaLookup?.nhsTrustCode
        val nhsRegionCode = areaLookup?.nhsRegionCode
        return if (canUseNhsTrust(areaType) && nhsTrustCode != null) {
            AreaDto(nhsTrustCode, areaLookup.nhsTrustName.orEmpty(), AreaType.NHS_TRUST)
        } else if (nhsRegionCode != null) {
            AreaDto(nhsRegionCode, areaLookup.nhsRegionName.orEmpty(), AreaType.NHS_REGION)
        } else {
            areaCodeResolver.defaultAreaDto(areaCode)
        }
    }

    private fun canUseNhsTrust(areaType: AreaType) =
        areaType == AreaType.UTLA || areaType == AreaType.LTLA || areaType == AreaType.NHS_TRUST

    suspend fun syncHospitalData(areaCode: String, areaType: AreaType) {
        try {
            healthcareDataSynchroniser.performSync(areaCode, areaType)
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    fun healthcareData(areaCodes: List<String>): List<AreaDailyDataDto> =
        healthcareDataSource.healthcareDataFoAreaCodes(areaCodes)
}
