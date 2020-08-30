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

package com.chrisa.covid19.core.data

import androidx.room.withTransaction
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.time.TimeProvider
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class AssetBootstrapper @Inject constructor(
    private val appDatabase: AppDatabase,
    private val assetDataSource: AssetDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val timeProvider: TimeProvider
) : Bootstrapper {

    override suspend fun bootstrapData() {
        return withContext(coroutineDispatchers.io) {
            val bootstrapAreas = async { bootstrapAreas() }
            val bootstrapOverview = async { bootstrapOverview() }
            bootstrapAreas.await()
            bootstrapOverview.await()
        }
    }

    private suspend fun bootstrapAreas() {
        val areaCount = appDatabase.areaDao().count()
        if (areaCount > 0) return
        val areas = assetDataSource.getAreas()
        appDatabase.withTransaction {
            appDatabase.areaDao().insertAll(areas.map {
                AreaEntity(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = AreaType.from(it.areaType)!!
                )
            })
            appDatabase.metadataDao().insert(
                MetadataEntity(
                    id = MetaDataIds.areaListId(),
                    lastUpdatedAt = BOOTSTRAP_DATA_TIMESTAMP,
                    lastSyncTime = timeProvider.currentTime()
                )
            )
        }
    }

    private suspend fun bootstrapOverview() {
        val areaCount = appDatabase.areaDataDao().countAllByAreaType(AreaType.OVERVIEW)
        if (areaCount > 0) return
        val areas = assetDataSource.getOverviewAreaData()
        appDatabase.withTransaction {
            appDatabase.areaDataDao().insertAll(areas.map {
                AreaDataEntity(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = AreaType.from(it.areaType)!!,
                    cumulativeCases = it.cumulativeCases ?: 0,
                    date = it.date,
                    newCases = it.newCases ?: 0,
                    infectionRate = it.infectionRate ?: 0.0
                )
            })
            appDatabase.metadataDao().insert(
                MetadataEntity(
                    id = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                    lastUpdatedAt = BOOTSTRAP_DATA_TIMESTAMP,
                    lastSyncTime = timeProvider.currentTime()
                )
            )
        }
    }

    companion object {
        val BOOTSTRAP_DATA_TIMESTAMP = LocalDateTime.of(2020, 8, 7, 0, 0)
    }
}
