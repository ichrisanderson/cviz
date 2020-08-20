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

package com.chrisa.covid19.features.startup.data

import androidx.room.withTransaction
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import javax.inject.Inject

class StartupDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {
    suspend fun clearNonSavedAreaDataCache() {
        appDatabase.withTransaction {
            val ukOverviewCode = listOf(Constants.UK_AREA_CODE)
            val nonAreaMetadataIds = listOf(MetaDataIds.areaListId(), MetaDataIds.ukOverviewId())
            val savedAreas = appDatabase.savedAreaDao().all()
            deleteNonSavedAreaData(ukOverviewCode, savedAreas.map { it.areaCode })
            deleteNonSavedMetadata(
                nonAreaMetadataIds,
                savedAreas.map { MetaDataIds.areaCodeId(it.areaCode) })
        }
    }

    private fun deleteNonSavedMetadata(
        nonAreaMetadataIds: List<String>,
        savedAreaMetadataIds: List<String>
    ) = appDatabase.metadataDao()
        .deleteAllNotInIds(nonAreaMetadataIds + savedAreaMetadataIds)

    private fun deleteNonSavedAreaData(
        ukOverviewCode: List<String>,
        savedAreasCodes: List<String>
    ) = appDatabase.areaDataDao()
        .deleteAllNotInAreaCodes(ukOverviewCode + savedAreasCodes)
}
