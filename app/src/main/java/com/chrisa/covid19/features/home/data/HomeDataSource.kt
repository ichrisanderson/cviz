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

package com.chrisa.covid19.features.home.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.features.home.data.dtos.AreaSummaryDto
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun ukOverview(): Flow<List<DailyRecordDto>> {

        val sortOrder = mapOf(
            Constants.UK_AREA_CODE to 1,
            Constants.ENGLAND_AREA_CODE to 2,
            Constants.SCOTLAND_AREA_CODE to 3,
            Constants.WALES_AREA_CODE to 4,
            Constants.NORTHERN_IRELAND_AREA_CODE to 5
        )

        return appDatabase.areaDataDao()
            .latestWithMetadataByAreaCodeAsFlow(
                listOf(
                    Constants.UK_AREA_CODE,
                    Constants.ENGLAND_AREA_CODE,
                    Constants.NORTHERN_IRELAND_AREA_CODE,
                    Constants.SCOTLAND_AREA_CODE,
                    Constants.WALES_AREA_CODE
                )
            )
            .map { areaDataList ->
                areaDataList
                    .sortedBy { sortOrder[it.areaCode] }
                    .groupBy { it.areaCode }
                    .flatMap { areaDataGroup ->
                        areaDataGroup.value.take(1).map { areaData ->
                            DailyRecordDto(
                                areaName = areaData.areaName,
                                dailyLabConfirmedCases = areaData.newCases,
                                totalLabConfirmedCases = areaData.cumulativeCases,
                                lastUpdated = areaData.lastUpdatedAt
                            )
                        }
                    }
            }
    }

    fun savedAreaCases(): Flow<List<SavedAreaCaseDto>> {
        return appDatabase.areaDataDao()
            .allSavedAreaDataAsFlow()
            .map { areaDataList ->
                areaDataList.map {
                    SavedAreaCaseDto(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType.value,
                        date = it.date,
                        dailyLabConfirmedCases = it.newCases,
                        totalLabConfirmedCases = it.cumulativeCases,
                        dailyTotalLabConfirmedCasesRate = it.infectionRate
                    )
                }
            }
    }

    fun areaSummaryEntities(): Flow<List<AreaSummaryDto>> {
        return appDatabase.areaSummaryEntityDao()
            .allAsFlow()
            .map { areaDataList ->
                areaDataList.map {
                    AreaSummaryDto(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType.value,
                        currentInfectionRate = it.newCaseInfectionRateWeek1,
                        changeInInfectionRate = it.newCaseInfectionRateWeek1 - it.newCaseInfectionRateWeek2,
                        changeInCases = it.newCasesWeek1 - it.newCasesWeek2,
                        currentNewCases = it.newCasesWeek1
                    )
                }
            }
    }
}
