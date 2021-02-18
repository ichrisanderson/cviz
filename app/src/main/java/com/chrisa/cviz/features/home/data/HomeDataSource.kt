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

package com.chrisa.cviz.features.home.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cviz.features.home.data.dtos.DailyRecordDto
import com.chrisa.cviz.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.cviz.features.home.data.dtos.SavedSoaDataDto
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
                                areaCode = areaData.areaCode,
                                areaName = areaData.areaName,
                                areaType = areaData.areaType.value,
                                newCases = areaData.newCases,
                                cumulativeCases = areaData.cumulativeCases,
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
                        areaType = it.areaType,
                        date = it.date,
                        newCases = it.newCases,
                        cumulativeCases = it.cumulativeCases,
                        infectionRate = it.infectionRate
                    )
                }
            }
    }

    fun savedSoaData(): Flow<List<SavedSoaDataDto>> {
        return appDatabase.soaDataDao()
            .allAsFlow()
            .map { areaDataList ->
                areaDataList.map {
                    SavedSoaDataDto(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType,
                        date = it.date,
                        rollingSum = it.rollingSum,
                        rollingRate = it.rollingRate
                    )
                }
            }
    }

    fun areaSummaries(): Flow<List<AreaSummaryDto>> {
        return appDatabase.areaSummaryDao()
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
