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
    @PublishedDeaths private val publishedDeathsUseCase: AreaDeathsUseCase,
    @OnsDeaths private val onsDeathsUseCase: AreaDeathsUseCase,
    private val healthcareUseCase: HealthcareUseCase
) {

    suspend fun execute(areaCode: String, areaType: AreaType): Flow<AreaDetailModelResult> {
        syncAreaData(areaCode, areaType)
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                AreaDetailModelResult.NoData
            } else {
                val areaLookup = areaLookupUseCase.areaLookup(areaCode, areaType)

                val areaCases = areaCasesUseCase.cases(areaCode, areaType)
                val publishedDeaths = publishedDeathsUseCase.deaths(areaCode, areaType)
                val onsDeaths = onsDeathsUseCase.deaths(areaCode, areaType)
                val healthcareData = healthcareUseCase.healthcareData(areaCode, areaLookup)

                AreaDetailModelResult.Success(
                    AreaDetailModel(
                        lastUpdatedAt = metadata.lastUpdatedAt,
                        lastSyncedAt = metadata.lastSyncTime,
                        casesAreaName = areaCases.name,
                        cases = areaCases.data,
                        deathsByPublishedDateAreaName = publishedDeaths.name,
                        deathsByPublishedDate = publishedDeaths.data,
                        onsDeathAreaName = onsDeaths.name,
                        onsDeathsByRegistrationDate = onsDeaths.data,
                        hospitalAdmissionsRegionName = healthcareData.name,
                        hospitalAdmissions = healthcareData.data
                    )
                )
            }
        }
    }

    private suspend fun syncAreaData(
        areaCode: String,
        areaType: AreaType
    ) {
        syncAreaCases(areaCode, areaType)
        when (areaType) {
            AreaType.UTLA, AreaType.LTLA, AreaType.REGION -> {
                areaLookupUseCase.syncAreaLookup(areaCode, areaType)
                val areaLookup = areaLookupUseCase.areaLookup(areaCode, areaType)
                val nhsRegion = healthcareUseCase.healthCareRegion(areaCode, areaLookup)
                healthcareUseCase.syncHospitalData(nhsRegion.code, nhsRegion.regionType)
            }
            else -> {
                healthcareUseCase.syncHospitalData(areaCode, areaType)
            }
        }
    }

    private suspend fun syncAreaCases(areaCode: String, areaType: AreaType): Boolean {
        return try {
            areaDataSynchroniser.performSync(areaCode, areaType)
            true
        } catch (error: Throwable) {
            false
        }
    }
}

sealed class AreaDetailModelResult {
    object NoData : AreaDetailModelResult()
    data class Success(val data: AreaDetailModel) : AreaDetailModelResult()
}
