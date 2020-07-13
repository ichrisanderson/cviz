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
import com.chrisa.covid19.features.home.domain.models.AreaCase
import com.chrisa.covid19.features.home.domain.models.AreaCaseList
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@FlowPreview
class LoadSavedAreaCasesUseCase @Inject constructor(
    private val homeDataSource: HomeDataSource
) {
    fun execute(): Flow<List<AreaCaseList>> {
        return homeDataSource.savedAreaCases()
            .map { cases ->
                cases.groupBy { Pair(it.areaCode, it.areaName) }
                    .map { group ->

                        val allCases = group.value

                        AreaCaseList(
                            areaCode = group.key.first,
                            areaName = group.key.second,
                            cases = allCases.map { case ->
                                AreaCase(
                                    dailyLabConfirmedCases = case.dailyLabConfirmedCases,
                                    date = case.date
                                )
                            })
                    }.sortedBy { it.areaName }
            }
    }

//    casesInTheLatestWeek
//    increaseSinceWeekBefore
//    totalCases
//    rate
//    rateDifference
}
