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
import com.chrisa.cviz.core.data.synchronisation.AreaDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataCollection
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.domain.deaths.AreaDeathsFacade
import com.chrisa.cviz.features.area.domain.healthcare.HealthcareUseCaseFacade
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSynchroniser: AreaDataSynchroniser,
    private val areaDataSource: AreaDataSource,
    private val areaLookupUseCase: AreaLookupUseCase,
    private val areaCasesUseCase: AreaCasesUseCase,
    private val areaDeathsFacade: AreaDeathsFacade,
    private val healthcareUseCaseFacade: HealthcareUseCaseFacade,
    private val alertLevelUseCase: AlertLevelUseCase,
    private val soaDataUseCase: SoaDataUseCase
) {

    suspend fun execute(areaCode: String, areaType: AreaType): Flow<AreaDetailModelResult> {
        syncAreaData(areaCode, areaType)
        val metadataFlow = areaDataSource.metadataAsFlow(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                AreaDetailModelResult.NoData
            } else {
                val areaLookup = areaLookupUseCase.areaLookup(areaCode, areaType)

                val soaData = soaDataUseCase.byAreaCode(areaCode, areaType)

                val areaLookupCode = areaLookupCode(areaCode, areaType, areaLookup)
                val areaMetadata = areaDataSource.metadata(areaLookupCode.areaCode)!!

                val areaCases =
                    areaCasesUseCase.cases(areaLookupCode.areaCode)
                val publishedDeaths =
                    areaDeathsFacade.publishedDeaths(
                        areaLookupCode.areaCode,
                        areaLookupCode.areaType,
                        areaLookup
                    )
                val onsDeaths = areaDeathsFacade.onsDeaths(
                    areaLookupCode.areaCode,
                    areaLookupCode.areaType,
                    areaLookup
                )
                val admissions: AreaDailyDataCollection =
                    healthcareUseCaseFacade.admissions(
                        areaLookupCode.areaCode,
                        areaLookupCode.areaType,
                        areaLookup
                    )
                val nhsRegion =
                    healthcareUseCaseFacade.nhsRegionArea(areaLookupCode.areaCode, areaLookup)
                val transmissionRate =
                    healthcareUseCaseFacade.transmissionRate(nhsRegion)
                val alertLevel =
                    alertLevelUseCase.alertLevel(areaLookupCode.areaCode, areaLookupCode.areaType)

                AreaDetailModelResult.Success(
                    AreaDetailModel(
                        lastUpdatedAt = areaMetadata.lastUpdatedAt,
                        casesAreaName = areaCases.name,
                        cases = areaCases.data,
                        deathsByPublishedDateAreaName = publishedDeaths.name,
                        deathsByPublishedDate = publishedDeaths.data,
                        onsDeathAreaName = onsDeaths.name,
                        onsDeathsByRegistrationDate = onsDeaths.data,
                        hospitalAdmissionsAreaName = admissions.name,
                        hospitalAdmissions = admissions.data,
                        transmissionRate = transmissionRate,
                        alertLevel = alertLevel,
                        soaData = soaData
                    )
                )
            }
        }
    }

    private fun areaLookupCode(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ): AreaLookupCode =
        when (areaType) {
            AreaType.MSOA ->
                AreaLookupCode(areaLookup!!.utlaCode, AreaType.UTLA)
            else ->
                AreaLookupCode(areaCode, areaType)
        }

    data class AreaLookupCode(val areaCode: String, val areaType: AreaType)

    private suspend fun syncAreaData(
        areaCode: String,
        areaType: AreaType
    ) {
        areaLookupUseCase.syncAreaLookup(areaCode, areaType)
        soaDataUseCase.syncSoaData(areaCode, areaType)
        alertLevelUseCase.syncAlertLevel(areaCode, areaType)
        syncAuthorityData(areaCode, areaType)
    }

    private suspend fun AreaDetailUseCase.syncAuthorityData(
        areaCode: String,
        areaType: AreaType
    ) {
        val areaLookup = areaLookupUseCase.areaLookup(areaCode, areaType)
        val areaLookupCode = areaLookupCode(areaCode, areaType, areaLookup)
        syncAreaCases(areaLookupCode.areaCode, areaLookupCode.areaType)
        syncHealthcare(areaLookupCode.areaCode, areaLookupCode.areaType, areaLookup)
    }

    private suspend fun syncHealthcare(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ) {
        when (areaType) {
            AreaType.MSOA, AreaType.UTLA, AreaType.LTLA, AreaType.REGION -> {
                val healthcareLookups = healthcareUseCaseFacade.healthcareLookups(areaCode)
                if (healthcareLookups.isEmpty()) {
                    val nhsRegion =
                        healthcareUseCaseFacade.healthcareArea(areaCode, areaType, areaLookup)
                    healthcareUseCaseFacade.syncHospitalData(nhsRegion.code, nhsRegion.areaType)
                } else {
                    healthcareLookups.forEach { lookup ->
                        healthcareUseCaseFacade.syncHospitalData(
                            lookup.nhsTrustCode,
                            AreaType.NHS_TRUST
                        )
                    }
                }
                val nhsRegionArea = healthcareUseCaseFacade.nhsRegionArea(areaCode, areaLookup)
                healthcareUseCaseFacade.syncHospitalData(
                    nhsRegionArea.code,
                    nhsRegionArea.areaType
                )
            }
            else -> {
                healthcareUseCaseFacade.syncHospitalData(areaCode, areaType)
            }
        }
    }

    private suspend fun syncAreaCases(
        areaCode: String,
        areaType: AreaType
    ): Boolean {
        return try {
            areaDataSynchroniser.performSync(areaCode, areaType)
            true
        } catch (error: Throwable) {
            false
        }
    }
}
