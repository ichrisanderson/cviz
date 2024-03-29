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
import javax.inject.Inject

class HealthcareLookupDataSynchroniser @Inject constructor(
    private val healthcareUseCaseFacade: HealthcareUseCaseFacade,
    private val healthcareAreaDataSynchroniser: HealthcareAreaDataSynchroniser,
    private val nhsTrustDataSynchroniser: NhsTrustDataSynchroniser
) {

    suspend fun execute(
        areaCode: String,
        healthcareAreaCode: String,
        healthcareAreaType: AreaType,
        areaLookup: AreaLookupDto?
    ) {
        val healthcareLookups = healthcareUseCaseFacade.healthcareLookups(healthcareAreaCode)
        if (healthcareLookups.isEmpty()) {
            healthcareAreaDataSynchroniser.execute(areaCode, healthcareAreaCode, healthcareAreaType, areaLookup)
        } else {
            healthcareLookups.forEach { lookup ->
                nhsTrustDataSynchroniser.execute(areaCode, lookup.nhsTrustCode)
            }
        }
    }
}
