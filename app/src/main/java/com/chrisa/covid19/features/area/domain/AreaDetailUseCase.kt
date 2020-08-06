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
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.domain.helper.RollingAverageHelper
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource,
    private val rollingAverageHelper: RollingAverageHelper
) {
    fun execute(areaCode: String): Flow<AreaDetailModel> {

        val metadata = areaDataSource.loadCaseMetadata()
        val allCases = areaDataSource.loadCases(areaCode)

        return combine(metadata, allCases) { metadata, cases ->
            val allCases = mapAllCases(cases)
            AreaDetailModel(
                lastUpdatedAt = metadata.lastUpdatedAt,
                allCases = allCases,
                latestCases = allCases.takeLast(14)
            )
        }
    }

    private fun mapAllCases(cases: List<CaseDto>): List<CaseModel> {
        return cases.mapIndexed { index, case ->
            val previousCase = cases.getOrNull(index - 7)
            val rollingAverage = rollingAverageHelper.average(case, previousCase)
            CaseModel(
                dailyLabConfirmedCases = case.dailyLabConfirmedCases,
                rollingAverage = rollingAverage,
                date = case.date
            )
        }
    }
}
