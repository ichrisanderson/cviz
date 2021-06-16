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

import com.chrisa.cviz.features.home.data.HomeDataSource
import com.chrisa.cviz.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cviz.features.home.data.dtos.DailyRecordDto
import com.chrisa.cviz.features.home.domain.models.CaseMapModel
import com.chrisa.cviz.features.home.domain.models.DashboardDataModel
import com.chrisa.cviz.features.home.domain.models.LatestUkDataModel
import com.chrisa.cviz.features.home.domain.models.SummaryModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ExperimentalCoroutinesApi
@FlowPreview
class LoadDashboardDataUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource
) {
    fun execute(): Flow<DashboardDataModel> {

        val ukOverviewFlow = homeDataSource.ukOverview()
        val areaSummariesAsFlow = homeDataSource.areaSummaries()
        val nationMapDate = homeDataSource.nationMapDate()

        return combine(
            ukOverviewFlow,
            areaSummariesAsFlow,
            nationMapDate
        ) { overview, areaSummaries, nationMapDate ->
            DashboardDataModel(
                latestUkData = latestUkData(overview),
                topInfectionRates = mapSummaryModel(
                    areaSummaries.summaryBy { it.currentInfectionRate }
                ),
                risingInfectionRates = mapSummaryModel(
                    areaSummaries.summaryBy { it.changeInInfectionRate }
                ),
                topNewCases = mapSummaryModel(
                    areaSummaries.summaryBy { it.currentNewCases }
                ),
                risingNewCases = mapSummaryModel(
                    areaSummaries.summaryBy { it.changeInCases }
                ),
                nationMap = mapNationCaseMapModel(nationMapDate)
            )
        }
    }

    private fun mapNationCaseMapModel(nationMapDate: LocalDate?): CaseMapModel? =
        nationMapDate?.let {
            CaseMapModel(
                lastUpdated = nationMapDate,
                imageUri = NATION_MAP_IMAGE_URI,
                redirectUri = NATION_MAP_REDIRECT_URI
            )
        }

    private fun latestUkData(
        ukOverview: List<DailyRecordDto>
    ): List<LatestUkDataModel> {
        return ukOverview.map { dailyRecord ->
            LatestUkDataModel(
                areaCode = dailyRecord.areaCode,
                areaName = dailyRecord.areaName,
                areaType = dailyRecord.areaType,
                newCases = dailyRecord.newCases,
                cumulativeCases = dailyRecord.cumulativeCases,
                lastUpdated = dailyRecord.lastUpdated
            )
        }
    }

    private fun mapSummaryModel(areaSummaries: List<AreaSummaryDto>): List<SummaryModel> {
        return areaSummaries.mapIndexed { index, areaSummary ->
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

    private inline fun <R : Comparable<R>> List<AreaSummaryDto>.summaryBy(crossinline selector: (AreaSummaryDto) -> R?): List<AreaSummaryDto> =
        this.sortedByDescending(selector)
            .take(10)

    private companion object {
        private const val NATION_MAP_IMAGE_URI =
            "https://coronavirus.data.gov.uk/public/assets/frontpage/images/map.png"
        private const val NATION_MAP_REDIRECT_URI =
            "https://coronavirus.data.gov.uk/details/interactive-map"
    }
}
