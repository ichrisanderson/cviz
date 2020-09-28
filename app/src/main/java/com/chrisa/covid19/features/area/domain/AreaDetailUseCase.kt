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
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource,
    private val rollingAverageHelper: RollingAverageHelper,
    private val caseChangeModelMapper: CaseChangeModelMapper
) {

    fun execute(areaCode: String): Flow<AreaDetailModel> {
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                emptyAreaDetailModel()
            } else {
                val areaData = areaDataSource.loadAreaData(areaCode)
                val allCases = mapAllCases(areaData.distinct().sortedBy { it.date })
                val caseChanges = caseChangeModelMapper.mapSavedAreaModel(allCases)
                AreaDetailModel(
                    lastUpdatedAt = metadata.lastUpdatedAt,
                    lastSyncedAt = metadata.lastSyncTime,
                    allCases = allCases,
                    latestCases = allCases.takeLast(14),
                    weeklyInfectionRate = caseChanges.weeklyInfectionRate,
                    changeInInfectionRate = caseChanges.changeInInfectionRate,
                    weeklyCases = caseChanges.weeklyCases,
                    changeInCases = caseChanges.changeInCases,
                    latestTotalCases = caseChanges.latestTotalCases
                )
            }
        }
    }

    private fun emptyAreaDetailModel(): AreaDetailModel = AreaDetailModel(
        lastUpdatedAt = null,
        lastSyncedAt = null,
        allCases = emptyList(),
        latestCases = emptyList(),
        latestTotalCases = 0,
        changeInCases = 0,
        weeklyCases = 0,
        weeklyInfectionRate = 0.0,
        changeInInfectionRate = 0.0
    )

    private fun mapAllCases(cases: List<CaseDto>): List<CaseModel> {
        return cases.mapIndexed { index, case ->
            CaseModel(
                baseRate = case.baseRate,
                cumulativeCases = case.cumulativeCases,
                newCases = case.newCases,
                rollingAverage = rollingAverageHelper.average(index, cases),
                date = case.date
            )
        }
    }
}

class CaseChangeModelMapper @Inject() constructor() {

    fun mapSavedAreaModel(
        allCases: List<CaseModel>
    ): CaseChangeModel {

        val offset = 3

        val lastCase = allCases.getOrNull(allCases.size - offset)
        val prevCase = allCases.getOrNull(allCases.size - (offset + 7))
        val prevCase1 = allCases.getOrNull(allCases.size - (offset + 14))

        val cumulativeCases0 = lastCase?.cumulativeCases ?: 0
        val cumulativeCases1 = prevCase?.cumulativeCases ?: 0
        val cumulativeCases2 = prevCase1?.cumulativeCases ?: 0

        val baseRate = lastCase?.baseRate ?: 0.0

        val casesThisWeek = (cumulativeCases0 - cumulativeCases1)
        val casesLastWeek = (cumulativeCases1 - cumulativeCases2)

        val infectionRateThisWeek = baseRate * casesThisWeek
        val infectionRateLastWeek = baseRate * casesLastWeek

        return CaseChangeModel(
            latestTotalCases = allCases.lastOrNull()?.cumulativeCases ?: 0,
            changeInCases = casesThisWeek - casesLastWeek,
            weeklyCases = casesThisWeek,
            changeInInfectionRate = infectionRateThisWeek - infectionRateLastWeek,
            weeklyInfectionRate = infectionRateThisWeek
        )
    }
}

data class CaseChangeModel(
    val latestTotalCases: Int,
    val changeInCases: Int,
    val weeklyCases: Int,
    val changeInInfectionRate: Double,
    val weeklyInfectionRate: Double
)
