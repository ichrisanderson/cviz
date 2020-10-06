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

package com.chrisa.covid19.features.home.domain

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.synchronisation.AreaData
import com.chrisa.covid19.core.data.synchronisation.AreaSummary
import com.chrisa.covid19.core.data.synchronisation.AreaSummaryMapper
import com.chrisa.covid19.features.home.data.HomeDataSource
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.SummaryModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class LoadSavedAreasUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val areaSummaryMapper: AreaSummaryMapper
) {
    fun execute(): Flow<List<SummaryModel>> {
        return homeDataSource.savedAreaCases().map { savedAreaCases ->
            savedAreaCases.groupBy { Triple(it.areaCode, it.areaName, it.areaType) }
                .map(this::mapAreaSummary)
                .sortedBy(AreaSummary::areaName)
                .mapIndexed(this::mapSummaryModel)
        }
    }

    private fun mapSummaryModel(
        index: Int,
        savedAreaModel: AreaSummary
    ): SummaryModel {
        return SummaryModel(
            position = index + 1,
            areaType = savedAreaModel.areaType,
            areaCode = savedAreaModel.areaCode,
            areaName = savedAreaModel.areaName,
            currentNewCases = savedAreaModel.currentNewCases,
            changeInCases = savedAreaModel.changeInCases,
            currentInfectionRate = savedAreaModel.currentInfectionRate,
            changeInInfectionRate = savedAreaModel.changeInInfectionRate
        )
    }

    private fun mapAreaSummary(group: Map.Entry<Triple<String, String, AreaType>, List<SavedAreaCaseDto>>): AreaSummary {
        return areaSummaryMapper.mapAreaDataToAreaSummary(
            group.key.first,
            group.key.second,
            group.key.third.value,
            group.value.map(this::mapAreaData)
        )
    }

    private fun mapAreaData(it: SavedAreaCaseDto): AreaData {
        return AreaData(
            newCases = it.newCases,
            cumulativeCases = it.cumulativeCases,
            infectionRate = it.infectionRate,
            date = it.date
        )
    }
}
