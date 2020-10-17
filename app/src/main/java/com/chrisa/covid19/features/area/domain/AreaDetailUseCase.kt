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
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class AreaDetailUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource,
    private val weeklySummaryBuilder: WeeklySummaryBuilder
) {

    fun execute(areaCode: String): Flow<AreaDetailModel> {
        val metadataFlow = areaDataSource.loadAreaMetadata(areaCode)
        return metadataFlow.map { metadata ->
            if (metadata == null) {
                emptyAreaDetailModel()
            } else {

                val areaData = areaDataSource.loadAreaData(areaCode)
                val caseDailyData = areaData.cases
                val deathDailyData = areaData.deaths
                val areaCaseSummary = areaCaseSummary(caseDailyData)
                val areaDeathSummaryModel = areaDeathSummaryModel(deathDailyData)

                AreaDetailModel(
                    areaType = areaData.areaType,
                    lastSyncedAt = metadata.lastSyncTime,
                    allCases = caseDailyData,
                    caseSummary = areaCaseSummary,
                    allDeaths = deathDailyData,
                    deathSummary = areaDeathSummaryModel
                )
            }
        }
    }

    private fun areaCaseSummary(dailyData: List<DailyData>): WeeklySummary =
        weeklySummaryBuilder.buildWeeklySummary(dailyData)

    private fun areaDeathSummaryModel(dailyData: List<DailyData>): WeeklySummary =
        weeklySummaryBuilder.buildWeeklySummary(dailyData)

    private fun emptyAreaDetailModel(): AreaDetailModel =
        AreaDetailModel(
            areaType = null,
            lastSyncedAt = null,
            allCases = emptyList(),
            caseSummary = emptyWeeklySummary,
            allDeaths = emptyList(),
            deathSummary = emptyWeeklySummary
        )

    private val emptyWeeklySummary =
        WeeklySummary(
            lastDate = null,
            currentTotal = 0,
            dailyTotal = 0,
            weeklyTotal = 0,
            changeInTotal = 0,
            weeklyRate = 0.0,
            changeInRate = 0.0
        )
}
