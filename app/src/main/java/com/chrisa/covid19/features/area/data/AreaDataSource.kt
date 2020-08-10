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
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.MetadataEntityMapper.toMetadataDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    suspend fun loadCases(areaCode: String, areaType: String): List<CaseDto> {

        val filter = "areaCode=$areaCode;areaType=$areaType"
        val pagedAreaCodeData = when (areaType) {
            "overview" -> covidApi.pagedAreaCodeDataByPublishDate(filter)
            else -> covidApi.pagedAreaCodeDataBySpeciminDate(filter)
        }

        // TODO: Extract this into a cache layer?
        // TODO: Clear out cached data for non saved items on start up
        appDatabase.withTransaction {
            appDatabase.areaDataDao().deleteAllByCode(areaCode)
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
        }

        return pagedAreaCodeData.data
            .map {
                CaseDto(
                    dailyLabConfirmedCases = it.newCases ?: 0,
                    totalLabConfirmedCases = it.cumulativeCases ?: 0,
                    date = it.date
                )
            }
    }

    fun loadAreaMetadata(): Flow<MetadataDto> {
        return appDatabase.metadataDao()
            // TODO: Save metadat for the area and reuse - this should be seperate from overview
            .metadataAsFlow(MetadataEntity.AREA_DATA_OVERVIEW_METADATA_ID)
            .map { it.toMetadataDto() }
    }
}
