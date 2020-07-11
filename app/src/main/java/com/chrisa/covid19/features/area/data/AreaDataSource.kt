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

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.CaseEntityMapper.toCaseDto
import com.chrisa.covid19.features.area.data.mappers.MetadataEntityMapper.toMetadataDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AreaDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun isSaved(areaCode: String): Flow<Boolean> {
        return appDatabase.savedAreaDao().isSaved(areaCode)
    }

    fun saveArea(savedAreaDto: SavedAreaDto) {
        return appDatabase.savedAreaDao().insert(savedAreaDto.toSavedAreaEntity())
    }

    fun loadCases(areaCode: String): List<CaseDto> {
        return appDatabase.casesDao()
            .searchAllCases(areaCode).map { it.toCaseDto() }
    }

    fun loadCaseMetadata(): MetadataDto {
        return appDatabase.metadataDao()
            .searchMetadata(MetadataEntity.CASE_METADATA_ID).map { it.toMetadataDto() }
            .first()
    }
}
