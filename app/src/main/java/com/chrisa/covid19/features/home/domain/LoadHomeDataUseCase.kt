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
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.MetadataDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.helpers.PastTwoWeekCaseBreakdownHelper
import com.chrisa.covid19.features.home.domain.helpers.WeeklyCaseDifferenceHelper
import com.chrisa.covid19.features.home.domain.models.HomeScreenDataModel
import com.chrisa.covid19.features.home.domain.models.LatestUkData
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@ExperimentalCoroutinesApi
@FlowPreview
class LoadHomeDataUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val pastTwoWeekCaseBreakdownHelper: PastTwoWeekCaseBreakdownHelper,
    private val weeklyCaseDifferenceHelper: WeeklyCaseDifferenceHelper
) {
    fun execute(): Flow<HomeScreenDataModel> {

        val metadata = homeDataSource.overviewMetadata()
        val ukOverview = homeDataSource.ukOverview()
        val savedAreaCases = homeDataSource.savedAreaCases()

        return combine(metadata, ukOverview, savedAreaCases) { metadata, ukOverview, cases ->
            HomeScreenDataModel(
                savedAreas = savedAreas(cases),
                latestUkData = latestUkData(ukOverview, metadata)
            )
        }
    }

    private fun savedAreas(cases: List<SavedAreaCaseDto>): List<SavedAreaModel> {
        return cases.groupBy { Pair(it.areaCode, it.areaName) }
            .map { group ->

                val dateNow = LocalDate.from(OffsetDateTime.now(ZoneOffset.UTC))
                val allCases = group.value

                // TODO: Reported data needs cleaning up on import, will be better
                // to calculate averages when inputting into DB - the totalLabConfirmedCases
                // are undereported - we need to calcualte the difference in total figures to get the
                // real reported numbers.
                val pastTwoWeekCaseBreakdown =
                    pastTwoWeekCaseBreakdownHelper.pastTwoWeekCaseBreakdown(
                        dateNow,
                        allCases
                    )

                val weeklyCaseDifference = weeklyCaseDifferenceHelper.weeklyCaseDifference(
                    pastTwoWeekCaseBreakdown.weekOneData,
                    pastTwoWeekCaseBreakdown.weekTwoData
                )

                SavedAreaModel(
                    areaCode = group.key.first,
                    areaName = group.key.second,
                    areaType = group.value.first().areaType,
                    changeInTotalLabConfirmedCases = weeklyCaseDifference.changeInWeeklyLabConfirmedCases,
                    changeInDailyTotalLabConfirmedCasesRate = weeklyCaseDifference.changeInTotalLabConfirmedCasesRate,
                    dailyTotalLabConfirmedCasesRate = pastTwoWeekCaseBreakdown.weekTwoData.totalLabConfirmedCasesRate,
                    totalLabConfirmedCases = pastTwoWeekCaseBreakdown.weekTwoData.totalLabConfirmedCases,
                    totalLabConfirmedCasesLastWeek = pastTwoWeekCaseBreakdown.weekTwoData.casesInWeek
                )
            }.sortedBy { it.areaName }
    }

    private fun latestUkData(
        ukOverview: List<DailyRecordDto>,
        metadata: MetadataDto
    ): LatestUkData {
        return ukOverview.takeLast(1).map { dailyRecord ->
            LatestUkData(
                areaName = dailyRecord.areaName,
                dailyLabConfirmedCases = dailyRecord.dailyLabConfirmedCases,
                totalLabConfirmedCases = dailyRecord.totalLabConfirmedCases,
                lastUpdated = metadata.lastUpdatedAt
            )
        }.firstOrNull() ?: LatestUkData(
            areaName = "83498rjfvjkbv",
            dailyLabConfirmedCases = 0,
            totalLabConfirmedCases = 0,
            lastUpdated = metadata.lastUpdatedAt
        )
    }
}
