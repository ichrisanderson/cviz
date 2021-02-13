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

package com.chrisa.cviz.features.home.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cviz.features.home.data.HomeDataSource
import com.chrisa.cviz.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.cviz.features.home.data.dtos.SavedSoaDataDto
import com.chrisa.cviz.features.home.domain.models.SavedAreaSummaryModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
@FlowPreview
class LoadSavedAreasUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val weeklySummaryBuilder: WeeklySummaryBuilder
) {
    fun execute(): Flow<List<SavedAreaSummaryModel>> {
        return combine(savedAreaData(), savedSoaData()) { savedAreaData, savedSoaData ->
            savedAreaData.plus(savedSoaData).sortedBy { it.areaName }
        }
    }

    private fun savedAreaData(): Flow<List<SavedAreaSummaryModel>> {
        return homeDataSource.savedAreaCases().map { savedAreaCases ->
            savedAreaCases.groupBy { Area(it.areaCode, it.areaName, it.areaType) }
                .map(this::areaData)
                .map(this::mapSummaryModel)
        }
    }

    private fun savedSoaData(): Flow<List<SavedAreaSummaryModel>> {
        return homeDataSource.savedSoaData().map { savedAreaCases ->
            savedAreaCases.groupBy { Area(it.areaCode, it.areaName, it.areaType) }
                .map(this::mapSummaryModel)
        }
    }

    private fun mapSummaryModel(
        group: Map.Entry<Area, List<SavedSoaDataDto>>
    ): SavedAreaSummaryModel {
        val data = group.value
        val lastCaseIndex = data.lastIndex

        val latestData = data.getOrNull(lastCaseIndex)
        val latestCaseValue = latestData?.rollingSum ?: 0
        val latestCaseRate = latestData?.rollingRate?.toInt() ?: 0

        val previousData = data.getOrNull(lastCaseIndex - 1)
        val previousCaseValue = previousData?.rollingSum ?: 0
        val previousCaseRate = previousData?.rollingRate?.toInt() ?: 0

        return SavedAreaSummaryModel(
            areaType = group.key.areaType.value,
            areaCode = group.key.areaCode,
            areaName = group.key.areaName,
            currentNewCases = latestCaseValue,
            changeInCases = latestCaseValue - previousCaseValue,
            currentInfectionRate = latestCaseRate.toDouble(),
            changeInInfectionRate = (latestCaseRate - previousCaseRate).toDouble()
        )
    }

    private fun areaData(group: Map.Entry<Area, List<SavedAreaCaseDto>>): AreaData {
        val area = group.key
        return AreaData(
            area.areaCode,
            area.areaName,
            area.areaType.value,
            mapDailyDataToWeeklySummary(group.value.map(this::mapDailyData))
        )
    }

    private fun mapDailyDataToWeeklySummary(dailyData: List<DailyData>): WeeklySummary =
        weeklySummaryBuilder.buildWeeklySummary(dailyData)

    private fun mapSummaryModel(
        areaData: AreaData
    ): SavedAreaSummaryModel {
        return SavedAreaSummaryModel(
            areaType = areaData.areaType,
            areaCode = areaData.areaCode,
            areaName = areaData.areaName,
            currentNewCases = areaData.weeklyCaseSummary.weeklyTotal,
            changeInCases = areaData.weeklyCaseSummary.changeInTotal,
            currentInfectionRate = areaData.weeklyCaseSummary.weeklyRate,
            changeInInfectionRate = areaData.weeklyCaseSummary.changeInRate
        )
    }

    private fun mapDailyData(it: SavedAreaCaseDto): DailyData {
        return DailyData(
            newValue = it.newCases,
            cumulativeValue = it.cumulativeCases,
            rate = it.infectionRate,
            date = it.date
        )
    }

    private data class Area(val areaCode: String, val areaName: String, val areaType: AreaType)
    private data class AreaData(
        val areaCode: String,
        val areaName: String,
        val areaType: String,
        val weeklyCaseSummary: WeeklySummary
    )
}
