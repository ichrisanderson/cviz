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
import com.chrisa.cviz.core.data.synchronisation.AreaLookupDataSynchroniser
import com.chrisa.cviz.core.data.synchronisation.HealthcareDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.data.AreaLookupDataSource
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSynchroniser: AreaDataSynchroniser,
    private val areaLookupDataSynchroniser: AreaLookupDataSynchroniser,
    private val healthcareDataSynchroniser: HealthcareDataSynchroniser,
    private val areaDataSource: AreaDataSource,
    private val areaLookupDataSource: AreaLookupDataSource
) {

    suspend fun execute(areaCode: String, areaType: AreaType): Flow<AreaDetailModelResult> {
        syncAreaData(areaCode, areaType)
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                AreaDetailModelResult.NoData
            } else {
                val nhsRegion = areaLookupDataSource.healthCareArea(areaCode, areaType)
                val healthCareData = areaDataSource.healthcareData(nhsRegion.code)
                val areaData = areaDataSource.loadAreaData(areaCode)
                val caseDailyData = areaData.cases
                val deathDailyData = areaData.deaths
                AreaDetailModelResult.Success(
                    AreaDetailModel(
                        areaType = areaData.areaType,
                        lastUpdatedAt = metadata.lastUpdatedAt,
                        lastSyncedAt = metadata.lastSyncTime,
                        cases = caseDailyData,
                        deaths = deathDailyData,
                        hospitalAdmissionsRegion = nhsRegion.name,
                        hospitalAdmissions = healthCareData
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
                syncAreaLookup(areaCode, areaType)
                val nhsRegion = areaLookupDataSource.healthCareArea(areaCode, areaType)
                syncHospitalData(nhsRegion.code, nhsRegion.regionType)
            }
            else -> {
                syncHospitalData(areaCode, areaType)
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

    private suspend fun syncAreaLookup(areaCode: String, areaType: AreaType) {
        try {
            areaLookupDataSynchroniser.performSync(areaCode, areaType)
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    private suspend fun syncHospitalData(areaCode: String, areaType: AreaType) {
        try {
            healthcareDataSynchroniser.performSync(areaCode, areaType)
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }
}

sealed class AreaDetailModelResult {
    object NoData : AreaDetailModelResult()
    data class Success(val data: AreaDetailModel) : AreaDetailModelResult()
}
