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
import com.chrisa.covid19.features.home.domain.helpers.PastTwoWeekCaseBreakdownHelper
import com.chrisa.covid19.features.home.domain.helpers.WeeklyCaseDifferenceHelper
import com.chrisa.covid19.features.home.domain.models.AreaCaseListModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@FlowPreview
class LoadSavedAreaCasesUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource,
    private val pastTwoWeekCaseBreakdownHelper: PastTwoWeekCaseBreakdownHelper,
    private val weeklyCaseDifferenceHelper: WeeklyCaseDifferenceHelper
) {
    fun execute(): Flow<List<AreaCaseListModel>> {
        return homeDataSource.savedAreaCases()
            .map { cases ->
                cases.groupBy { Pair(it.areaCode, it.areaName) }
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

                        AreaCaseListModel(
                            areaCode = group.key.first,
                            areaName = group.key.second,
                            changeInTotalLabConfirmedCases = weeklyCaseDifference.changeInWeeklyLabConfirmedCases,
                            changeInDailyTotalLabConfirmedCasesRate = weeklyCaseDifference.changeInTotalLabConfirmedCasesRate,
                            dailyTotalLabConfirmedCasesRate = pastTwoWeekCaseBreakdown.weekTwoData.totalLabConfirmedCasesRate,
                            totalLabConfirmedCases = pastTwoWeekCaseBreakdown.weekTwoData.totalLabConfirmedCases,
                            totalLabConfirmedCasesLastWeek = pastTwoWeekCaseBreakdown.weekTwoData.casesInWeek
                        )
                    }.sortedBy { it.areaName }
            }
    }
}
