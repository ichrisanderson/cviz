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
import com.chrisa.covid19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.HomeScreenDataModel
import com.chrisa.covid19.features.home.domain.models.InfectionRateModel
import com.chrisa.covid19.features.home.domain.models.LatestUkDataModel
import com.chrisa.covid19.features.home.domain.models.NewCaseModel
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class LoadHomeDataUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val savedAreaModelMapper: SavedAreaModelMapper
) {
    fun execute(): Flow<HomeScreenDataModel> {

        val ukOverviewFlow = homeDataSource.ukOverview()
        val areaSummariesAsFlow = homeDataSource.areaSummaries()
        val savedAreaCasesFlow = homeDataSource.savedAreaCases()

        return combine(
            ukOverviewFlow,
            areaSummariesAsFlow,
            savedAreaCasesFlow
        ) { overview, areaSummaries, savedAreas ->
            HomeScreenDataModel(
                savedAreas = savedAreas(savedAreas),
                latestUkData = latestUkData(overview),
                topInfectionRates = mapInfectionRates(
                    areaSummaries
                        .sortedByDescending { it.currentInfectionRate }
                        .take(10)
                ),
                risingInfectionRates = mapInfectionRates(
                    areaSummaries
                        .sortedByDescending { it.changeInInfectionRate }
                        .take(10)
                ),
                topNewCases = mapNewCases(
                    areaSummaries
                        .sortedByDescending { it.currentNewCases }
                        .take(10)
                ),
                risingNewCases = mapNewCases(
                    areaSummaries
                        .sortedByDescending { it.changeInCases }
                        .take(10)
                )
            )
        }
    }

    private fun savedAreas(cases: List<SavedAreaCaseDto>): List<SavedAreaModel> {
        return cases.groupBy { Pair(it.areaCode, it.areaName) }
            .map { group ->
                savedAreaModelMapper.mapSavedAreaModel(
                    group.key.first,
                    group.key.second,
                    group.value
                )
            }
            .sortedBy { it.areaName }
    }

    private fun latestUkData(
        ukOverview: List<DailyRecordDto>
    ): List<LatestUkDataModel> {
        return ukOverview.map { dailyRecord ->
            LatestUkDataModel(
                areaCode = dailyRecord.areaCode,
                areaName = dailyRecord.areaName,
                areaType = dailyRecord.areaType,
                dailyLabConfirmedCases = dailyRecord.dailyLabConfirmedCases,
                totalLabConfirmedCases = dailyRecord.totalLabConfirmedCases,
                lastUpdated = dailyRecord.lastUpdated
            )
        }
    }

    private fun mapInfectionRates(areaSummaries: List<AreaSummaryDto>): List<InfectionRateModel> {
        return areaSummaries.mapIndexed { index, areaSummary ->
            InfectionRateModel(
                position = index + 1,
                areaCode = areaSummary.areaCode,
                areaName = areaSummary.areaName,
                areaType = areaSummary.areaType,
                changeInInfectionRate = areaSummary.changeInInfectionRate,
                currentInfectionRate = areaSummary.currentInfectionRate
            )
        }
    }

    private fun mapNewCases(areaSummaries: List<AreaSummaryDto>): List<NewCaseModel> {
        return areaSummaries.mapIndexed { index, areaSummary ->
            NewCaseModel(
                position = index + 1,
                areaCode = areaSummary.areaCode,
                areaName = areaSummary.areaName,
                areaType = areaSummary.areaType,
                changeInCases = areaSummary.changeInCases,
                currentNewCases = areaSummary.currentNewCases
            )
        }
    }
}
