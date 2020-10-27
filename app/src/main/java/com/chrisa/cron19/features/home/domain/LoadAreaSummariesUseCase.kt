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

package com.chrisa.cron19.features.home.domain

import com.chrisa.cron19.features.home.data.HomeDataSource
import com.chrisa.cron19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cron19.features.home.domain.models.SortOption
import com.chrisa.cron19.features.home.domain.models.SummaryModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
@FlowPreview
class LoadAreaSummariesUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val areaSummaryListSorter: AreaSummaryListSorter
) {
    fun execute(sortOption: SortOption): Flow<List<SummaryModel>> {
        return homeDataSource.areaSummaries().map { areaSummaries ->
            areaSummaryListSorter.sort(areaSummaries, sortOption)
                .mapIndexed { index, areaSummary ->
                    SummaryModel(
                        position = index + 1,
                        areaCode = areaSummary.areaCode,
                        areaName = areaSummary.areaName,
                        areaType = areaSummary.areaType,
                        changeInCases = areaSummary.changeInCases,
                        currentNewCases = areaSummary.currentNewCases,
                        changeInInfectionRate = areaSummary.changeInInfectionRate,
                        currentInfectionRate = areaSummary.currentInfectionRate
                    )
                }
        }
    }
}

class AreaSummaryListSorter @Inject constructor() {
    fun sort(list: List<AreaSummaryDto>, sortOption: SortOption): List<AreaSummaryDto> =
        when (sortOption) {
            SortOption.RisingCases -> list.sortedByDescending { it.changeInCases }
            SortOption.RisingInfectionRate -> list.sortedByDescending { it.changeInInfectionRate }
            SortOption.InfectionRate -> list.sortedByDescending { it.currentInfectionRate }
            SortOption.NewCases -> list.sortedByDescending { it.currentNewCases }
        }
}
