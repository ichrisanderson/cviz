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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.data.dtos.SavedAreaDto
import com.chrisa.cviz.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AreaDataSource @Inject constructor(
    private val appDatabase: AppDatabase
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

    fun metadataAsFlow(areaCode: String): Flow<MetadataDto?> {
        return appDatabase.metadataDao()
            .metadataAsFlow(MetadataIds.areaCodeId(areaCode))
            .map {
                it?.let {
                    MetadataDto(
                        lastUpdatedAt = it.lastUpdatedAt,
                        lastSyncTime = it.lastSyncTime
                    )
                }
            }
    }

    fun metadata(areaCode: String): MetadataDto? {
        return appDatabase.metadataDao()
            .metadata(MetadataIds.areaCodeId(areaCode))
            ?.let {
                MetadataDto(
                    lastUpdatedAt = it.lastUpdatedAt,
                    lastSyncTime = it.lastSyncTime
                )
            }
    }
}
