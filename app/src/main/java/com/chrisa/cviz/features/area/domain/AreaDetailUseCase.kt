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

import com.chrisa.cviz.features.area.data.AreaDataSource
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource
) {

    fun execute(areaCode: String): Flow<AreaDetailModel> {
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                AreaDetailModel.EMPTY
            } else {

                val areaData = areaDataSource.loadAreaData(areaCode)
                val caseDailyData = areaData.cases
                val deathDailyData = areaData.deaths

                AreaDetailModel(
                    areaType = areaData.areaType,
                    lastUpdatedAt = metadata.lastUpdatedAt,
                    lastSyncedAt = metadata.lastSyncTime,
                    cases = caseDailyData,
                    deaths = deathDailyData,
                    hospitalAdmissions = areaData.hospitalAdmissions
                )
            }
        }
    }
}
