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

import com.chrisa.covid19.core.data.synchronisation.DailyData
import com.chrisa.covid19.core.data.synchronisation.WeeklySummary
import com.chrisa.covid19.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.covid19.features.area.data.AreaDataSource
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
    private val weeklySummaryBuilder: WeeklySummaryBuilder
) {

    fun execute(areaCode: String): Flow<AreaDetailModel> {
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                emptyAreaDetailModel()
            } else {

                val areaData = areaDataSource.loadAreaData(areaCode)
                val cases = mapAllCases(areaData.cases)
                val deathsByPublishedDate = mapAllDeaths(areaData.deathsByPublishedDate)
                val weeklyCaseSummary = areaCaseWeeklySummary(areaData.cases.map(::toDailyData))
                val weeklyDeathSummary =
                    areaCaseWeeklySummary(areaData.deathsByPublishedDate.map(::toDailyData))
                val lastCase = areaData.cases.lastOrNull()

                AreaDetailModel(
                    areaType = areaData.areaType,
                    lastUpdatedAt = metadata.lastUpdatedAt,
                    lastSyncedAt = metadata.lastSyncTime,
                    cumulativeCases = lastCase?.cumulativeCases ?: 0,
                    newCases = lastCase?.newCases ?: 0,
                    allCases = cases,
                    weeklyCaseSummary = weeklyCaseSummary,
                    deathsByPublishedDate = deathsByPublishedDate,
                    weeklyDeathSummary = weeklyDeathSummary
                )
            }
        }
    }

    private fun areaCaseWeeklySummary(dailyData: List<DailyData>): WeeklySummary =
        weeklySummaryBuilder.buildWeeklySummary(dailyData)

    private fun toDailyData(caseDto: CaseDto): DailyData {
        return DailyData(
            newValue = caseDto.newCases,
            cumulativeValue = caseDto.cumulativeCases,
            rate = caseDto.infectionRate,
            date = caseDto.date
        )
    }

    private fun toDailyData(deathDto: DeathDto): DailyData {
        return DailyData(
            newValue = deathDto.newDeaths,
            cumulativeValue = deathDto.cumulativeDeaths,
            rate = deathDto.deathRate,
            date = deathDto.date
        )
    }

    private fun emptyAreaDetailModel(): AreaDetailModel =
        AreaDetailModel(
            areaType = null,
            lastUpdatedAt = null,
            lastSyncedAt = null,
            cumulativeCases = 0,
            newCases = 0,
            allCases = emptyList(),
            weeklyCaseSummary = emptySummary(),
            deathsByPublishedDate = emptyList(),
            weeklyDeathSummary = emptySummary()
        )

    private fun emptySummary(): WeeklySummary =
        WeeklySummary(
            weeklyTotal = 0,
            weeklyRate = 0.0,
            changeInTotal = 0,
            changeInRate = 0.0
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
