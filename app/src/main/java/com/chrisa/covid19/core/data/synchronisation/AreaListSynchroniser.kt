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
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.MetaDataHelper
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.DateUtils.toGmtDateTime
import com.chrisa.covid19.core.util.NetworkUtils
import java.time.LocalDateTime
import javax.inject.Inject
import timber.log.Timber

class AreaListSynchroniser @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val api: CovidApi
) {

    private val metadataDao = appDatabase.metadataDao()
    private val areaDao = appDatabase.areaDao()

    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) return

        val now = LocalDateTime.now()
        val areaMetadata = metadataDao.metadata(MetaDataHelper.areaListKey()) ?: return

        if (areaMetadata.lastUpdatedAt.plusHours(1).isAfter(now) ||
            areaMetadata.lastSyncTime.plusHours(1).isAfter(now)) {
            return
        }
        runCatching {
            api.pagedAreaResponse(areaMetadata.lastUpdatedAt.formatAsGmt())
        }.onSuccess { areasResponse ->
            if (areasResponse.isSuccessful) {
                val lastModified = areasResponse.headers().get("Last-Modified")
                val areas = areasResponse.body() ?: return@onSuccess
                appDatabase.withTransaction {
                    areaDao.insertAll(areas.data.map {
                        AreaEntity(
                            areaType = it.areaType,
                            areaName = it.areaName,
                            areaCode = it.areaCode
                        )
                    })
                    metadataDao.insert(
                        MetadataEntity(
                            id = MetaDataHelper.areaListKey(),
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
