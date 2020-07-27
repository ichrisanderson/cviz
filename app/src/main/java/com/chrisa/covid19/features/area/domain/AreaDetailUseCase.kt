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

package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.domain.mappers.CaseDtoMapper.toCaseModel
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AreaDetailUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource
) {
    fun execute(areaCode: String): Flow<AreaDetailModel> {

        val metadata = areaDataSource.loadCaseMetadata()
        val allCases = areaDataSource.loadCases(areaCode)

        return combine(metadata, allCases) { metadata, cases ->
            val allCases = cases.map { it.toCaseModel() }
            AreaDetailModel(
                lastUpdatedAt = metadata.lastUpdatedAt,
                allCases = allCases,
                latestCases = allCases.takeLast(14)
            )
        }
    }
}
