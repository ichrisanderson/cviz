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

import com.chrisa.covid19.features.home.data.HomeDataSource
import com.chrisa.covid19.features.home.domain.models.SummaryModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
@FlowPreview
class LoadSavedAreasUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val savedAreaModelMapper: SavedAreaModelMapper
) {
    fun execute(): Flow<List<SummaryModel>> {
        return homeDataSource.savedAreaCases().map { savedAreaCases ->
            savedAreaCases.groupBy { Pair(it.areaCode, it.areaName) }
                .map { group ->
                    savedAreaModelMapper.mapSavedAreaModel(
                        group.key.first,
                        group.key.second,
                        group.value
                    )
                }
                .sortedBy { it.areaName }
                .mapIndexed { index, savedAreaModel ->
                    SummaryModel(
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
        }
    }
}
