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

package com.chrisa.covid19.features.area.data

import androidx.room.withTransaction
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.util.DateUtils.toGmtDateTime
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

class AreaDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val covidApi: CovidApi
) {

    fun isSaved(areaCode: String): Flow<Boolean> {
        return appDatabase.savedAreaDao().isSaved(areaCode)
    }

    fun insertSavedArea(savedAreaDto: SavedAreaDto) {
        return appDatabase.savedAreaDao().insert(savedAreaDto.toSavedAreaEntity())
    }

    fun deleteSavedArea(savedAreaDto: SavedAreaDto): Int {
        return appDatabase.savedAreaDao().delete(savedAreaDto.toSavedAreaEntity())
    }

    fun loadAreaData(areaCode: String, areaType: String): List<CaseDto> {
        return appDatabase.areaDataDao().allByAreaCode(areaCode)
            .map {
                CaseDto(
                    dailyLabConfirmedCases = it.newCases,
                    totalLabConfirmedCases = it.cumulativeCases,
                    date = it.date
                )
            }
    }

    suspend fun syncAreaData(areaCode: String, areaType: String) {

        val casesFromNetwork = getResponse(areaCode, areaType)

        if (casesFromNetwork.isSuccessful) {
            val pagedAreaCodeData = casesFromNetwork.body()!!
            val lastModified = casesFromNetwork.headers().get("Last-Modified")
            cacheAreaData(
                areaCode,
                pagedAreaCodeData,
                lastModified?.toGmtDateTime() ?: LocalDateTime.now()
            )
        } else {
            throw IOException()
        }
    }

    private suspend fun getResponse(
        areaCode: String,
        areaType: String
    ): Response<Page<AreaDataModel>> {
        val filter = CovidApi.AREA_DATA_FILTER(areaCode, areaType)
        return when (areaType) {
            "overview" -> covidApi.pagedAreaDataResponse(
                modifiedDate = null,
                filters = filter,
                areaDataModelStructure = CovidApi.AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
            )
            "nation" -> covidApi.pagedAreaDataResponse(
                modifiedDate = null,
                filters = filter,
                areaDataModelStructure = CovidApi.AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
            )
            else -> covidApi.pagedAreaDataResponse(
                modifiedDate = null,
                filters = filter,
                areaDataModelStructure = CovidApi.AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE
            )
        }
    }

    private suspend fun cacheAreaData(
        areaCode: String,
        pagedAreaCodeData: Page<AreaDataModel>,
        lastModified: LocalDateTime
    ) {
        appDatabase.withTransaction {
            appDatabase.areaDataDao().deleteAllByAreaCode(areaCode)
            appDatabase.areaDataDao().insertAll(pagedAreaCodeData.data.map {
                AreaDataEntity(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = it.areaType,
                    cumulativeCases = it.cumulativeCases ?: 0,
                    date = it.date,
                    infectionRate = it.infectionRate ?: 0.0,
                    newCases = it.newCases ?: 0
                )
            })
            appDatabase.metadataDao().insert(
                metadata = MetadataEntity(
                    id = MetaDataIds.areaCodeId(areaCode),
                    lastUpdatedAt = lastModified,
                    lastSyncTime = LocalDateTime.now()
                )
            )
        }
    }

    fun loadAreaMetadata(areaCode: String, areaType: String): Flow<MetadataDto?> {
        return appDatabase.metadataDao()
            .metadataAsFlow(MetaDataIds.areaCodeId(areaCode))
            .map {
                it?.let {
                    MetadataDto(
                        lastUpdatedAt = it.lastUpdatedAt,
                        lastSyncTime = it.lastSyncTime
                    )
                }
            }
    }
}
