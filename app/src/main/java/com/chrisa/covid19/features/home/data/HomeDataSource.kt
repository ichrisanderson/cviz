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
import com.chrisa.covid19.core.data.db.MetaDataHelper
import com.chrisa.covid19.features.home.data.dtos.DailyRecordDto
import com.chrisa.covid19.features.home.data.dtos.MetadataDto
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun overviewMetadata(): Flow<MetadataDto> {
        return appDatabase.metadataDao()
            .metadataAsFlow(MetaDataHelper.ukOverviewKey())
            .map {
                MetadataDto(
                    lastUpdatedAt = it?.lastUpdatedAt ?: LocalDateTime.now()
                )
            }
    }

    fun ukOverview(): Flow<List<DailyRecordDto>> {
        return appDatabase.areaDataDao()
            .allByAreaCodeFlow(Constants.UK_AREA_CODE)
            .map { areaDataList ->
                areaDataList.map { areaData ->
                    DailyRecordDto(
                        areaName = areaData.areaName,
                        dailyLabConfirmedCases = areaData.newCases,
                        totalLabConfirmedCases = areaData.cumulativeCases,
                        date = areaData.date
                    )
                }
            }
    }

    fun savedAreaCases(): Flow<List<SavedAreaCaseDto>> {
        return appDatabase.areaDataDao()
            .allSavedAreaData()
            .map { areaDataList ->
                areaDataList.map {
                    SavedAreaCaseDto(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType,
                        date = it.date,
                        dailyLabConfirmedCases = it.newCases,
                        totalLabConfirmedCases = it.cumulativeCases,
                        dailyTotalLabConfirmedCasesRate = it.infectionRate
                    )
                }
            }
    }
}
