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
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class HealthcareDataUseCase @Inject constructor(
    private val healthcareLookupsUseCase: HealthcareLookupsUseCase,
    private val healthcareAreaUseCase: HealthcareAreaUseCase,
    private val multiAreaHealthcareDataUseCase: MultiAreaHealthcareDataUseCase,
    private val singleAreaHealthcareDataUseCase: SingleAreaHealthcareDataUseCase
) {

    fun admissions(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ): AreaDailyDataCollection {
        val healthcareLookups = healthcareLookupsUseCase.healthcareLookups(areaType, areaLookup)
        return when {
            healthcareLookups.isEmpty() || areaLookup == null -> {
                val nhsRegion =
                    healthcareAreaUseCase.healthcareArea(areaCode, areaType, areaLookup)
                singleAreaHealthcareDataUseCase.admissionsForArea(nhsRegion.name, nhsRegion.code)
            }
            else -> {
                multiAreaHealthcareDataUseCase.admissionsForAreaCodes(
                    areaLookup.utlaName,
                    healthcareLookups
                )
            }
        }
    }
}
