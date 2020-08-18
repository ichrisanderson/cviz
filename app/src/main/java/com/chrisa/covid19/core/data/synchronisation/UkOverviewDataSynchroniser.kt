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

package com.chrisa.covid19.core.data.synchronisation

import androidx.room.withTransaction
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.DateUtils.toGmtDateTime
import com.chrisa.covid19.core.util.NetworkUtils
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class UkOverviewDataSynchroniser @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val api: CovidApi
) {

    private val metadataDao = appDatabase.metadataDao()
    private val areaDataDao = appDatabase.areaDataDao()

    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) return
        val now = LocalDateTime.now()
        val areaMetadata =
            metadataDao.metadata(MetaDataIds.ukOverviewId()) ?: return

        if (areaMetadata.lastUpdatedAt.plusHours(1).isAfter(now)) {
            return
        }

        runCatching {
            api.pagedAreaDataResponse(
                modifiedDate = areaMetadata.lastUpdatedAt.formatAsGmt(),
                filters = CovidApi.AREA_DATA_FILTER(Constants.UK_AREA_CODE, "overview"),
                areaDataModelStructure = CovidApi.AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
            )
        }.onSuccess { areasResponse ->
            if (areasResponse.isSuccessful) {
                val lastModified = areasResponse.headers().get("Last-Modified")
                val areas = areasResponse.body() ?: return@onSuccess
                appDatabase.withTransaction {
                    areaDataDao.insertAll(areas.data.map {
                        AreaDataEntity(
                            areaCode = it.areaCode,
                            areaName = it.areaName,
                            areaType = it.areaType,
                            cumulativeCases = it.cumulativeCases ?: 0,
                            date = it.date,
                            newCases = it.newCases ?: 0,
                            infectionRate = it.infectionRate ?: 0.0
                        )
                    })
                    metadataDao.insert(
                        MetadataEntity(
                            id = MetaDataIds.ukOverviewId(),
                            lastUpdatedAt = lastModified?.toGmtDateTime() ?: LocalDateTime.now(),
                            lastSyncTime = LocalDateTime.now()
                        )
                    )
                }
            }
        }.onFailure { error ->
            Timber.e(error, "Error synchronizing areas")
        }
    }
}
