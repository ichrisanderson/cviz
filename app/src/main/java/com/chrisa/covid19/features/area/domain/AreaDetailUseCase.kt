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

import com.chrisa.covid19.core.data.synchronisation.AreaData
import com.chrisa.covid19.core.data.synchronisation.AreaSummary
import com.chrisa.covid19.core.data.synchronisation.AreaSummaryMapper
import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.AreaCaseDto
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.DeathDto
import com.chrisa.covid19.features.area.domain.helper.RollingAverageHelper
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource,
    private val rollingAverageHelper: RollingAverageHelper,
    private val areaSummaryMapper: AreaSummaryMapper
) {

    fun execute(areaCode: String): Flow<AreaDetailModel> {
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                emptyAreaDetailModel()
            } else {
                val areaData = areaDataSource.loadAreaData(areaCode)
                val caseModels = mapAllCases(areaData.cases)
                val deathsByPublishedDate = mapAllDeaths(areaData.deathsByPublishedDate)
                val deathsByDeathDate = mapAllDeaths(areaData.deathsByDeathDate)
                val areaSummary = areaSummary(areaData)
                val cumulativeCases = areaData.cases.map { it.cumulativeCases }.lastOrNull() ?: 0
                AreaDetailModel(
                    lastUpdatedAt = metadata.lastUpdatedAt,
                    lastSyncedAt = metadata.lastSyncTime,
                    allCases = caseModels,
                    deathsByPublishedDate = deathsByPublishedDate,
                    deathsByDeathDate = deathsByDeathDate,
                    weeklyInfectionRate = areaSummary.currentInfectionRate,
                    changeInInfectionRate = areaSummary.changeInInfectionRate,
                    weeklyCases = areaSummary.currentNewCases,
                    changeInCases = areaSummary.changeInCases,
                    cumulativeCases = cumulativeCases
                )
            }
        }
    }

    private fun areaSummary(areaData: AreaCaseDto): AreaSummary {
        return areaSummaryMapper.mapAreaDataToAreaSummary(
            areaData.areaCode,
            areaData.areaName,
            areaData.areaType,
            areaData.cases.map {
                AreaData(
                    newCases = it.newCases,
                    cumulativeCases = it.cumulativeCases,
                    infectionRate = it.infectionRate,
                    date = it.date
                )
            }
        )
    }

    private fun emptyAreaDetailModel(): AreaDetailModel = AreaDetailModel(
        lastUpdatedAt = null,
        weeklyInfectionRate = 0.0,
        changeInInfectionRate = 0.0,
        weeklyCases = 0,
        changeInCases = 0,
        cumulativeCases = 0,
        lastSyncedAt = null,
        allCases = emptyList(),
        deathsByPublishedDate = emptyList(),
        deathsByDeathDate = emptyList()
    )

    private fun mapAllCases(cases: List<CaseDto>): List<CaseModel> {
        val caseValues = cases.map { it.newCases }
        return cases.mapIndexed { index, case ->
            CaseModel(
                baseRate = case.baseRate,
                cumulativeCases = case.cumulativeCases,
                newCases = case.newCases,
                rollingAverage = rollingAverageHelper.average(index, caseValues),
                date = case.date
            )
        }
    }

    private fun mapAllDeaths(deaths: List<DeathDto>): List<DeathModel> {
        val deathValues = deaths.map { it.newDeaths }
        return deaths.mapIndexed { index, case ->
            DeathModel(
                baseRate = case.baseRate,
                cumulativeDeaths = case.cumulativeDeaths,
                newDeaths = case.newDeaths,
                rollingAverage = rollingAverageHelper.average(index, deathValues),
                date = case.date
            )
        }
    }
}
