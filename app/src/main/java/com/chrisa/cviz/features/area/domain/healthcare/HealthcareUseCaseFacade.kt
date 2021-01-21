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

package com.chrisa.cviz.features.area.domain.healthcare

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.HealthcareLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class HealthcareUseCaseFacade @Inject constructor(
    private val healthcareDataUseCase: HealthcareDataUseCase,
    private val healthcareLookupDataSource: HealthcareLookupDataSource,
    private val healthcareAreaUseCase: HealthcareAreaUseCase,
    private val healthcareSyncUseCase: HealthcareSyncUseCase,
    private val transmissionRateUseCase: TransmissionRateUseCase,
    private val nhsRegionAreaUseCase: NhsRegionAreaUseCase
) {

    fun admissions(areaCode: String, areaType: AreaType, areaLookup: AreaLookupDto?) =
        healthcareDataUseCase.admissions(areaCode, areaType, areaLookup)

    fun healthcareLookups(areaCode: String) =
        healthcareLookupDataSource.healthcareLookups(areaCode)

    fun healthcareArea(areaCode: String, areaType: AreaType, areaLookup: AreaLookupDto?) =
        healthcareAreaUseCase.healthcareArea(areaCode, areaType, areaLookup)

    suspend fun syncHospitalData(areaCode: String, areaType: AreaType) =
        healthcareSyncUseCase.syncHospitalData(areaCode, areaType)

    fun transmissionRate(areaCode: String, areaLookupDto: AreaLookupDto?) =
        transmissionRateUseCase.transmissionRate(areaCode, areaLookupDto)

    fun nhsRegionArea(areaCode: String, areaLookup: AreaLookupDto?) =
        nhsRegionAreaUseCase.regionArea(areaCode, areaLookup)
}
