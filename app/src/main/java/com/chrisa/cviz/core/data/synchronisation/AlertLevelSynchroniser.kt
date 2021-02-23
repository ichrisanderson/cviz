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

package com.chrisa.cviz.core.data.synchronisation

import androidx.room.withTransaction
import com.chrisa.cviz.core.data.db.AlertLevelEntity
import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.network.AlertLevel
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.BodyPage
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.Page
import com.chrisa.cviz.core.data.network.Utils.emptyJsonResponse
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.core.util.DateUtils.formatAsGmt
import com.chrisa.cviz.core.util.DateUtils.toGmtDateTime
import com.chrisa.cviz.core.util.NetworkUtils
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import retrofit2.HttpException
import retrofit2.Response

interface AlertLevelSynchroniser {
    suspend fun performSync(areaCode: String, areaType: AreaType)
}

internal class AlertLevelSynchroniserImpl @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) : AlertLevelSynchroniser {

    override suspend fun performSync(
        areaCode: String,
        areaType: AreaType
    ) {
        val metadata = appDatabase.metadataDao().metadata(MetaDataIds.alertLevelId(areaCode))
        val now = timeProvider.currentTime()
        if (metadata != null && metadata.lastSyncTime.plusMinutes(5).isAfter(now)) {
            return
        }

        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val response = api.alertLevel(
            modifiedDate = metadata?.lastUpdatedAt?.formatAsGmt(),
            filters = mapOf(
                "areaType" to areaType.value,
                "areaCode" to areaCode,
                "metric" to "alertLevel",
                "format" to "json"
            )
        )

        if (response.isSuccessful) {
            val alertLevels = response.body()!!
            val lastModified = response.headers()["Last-Modified"]
            cacheData(
                areaCode,
                alertLevels,
                lastModified?.toGmtDateTime() ?: timeProvider.currentTime()
            )
        } else {
            throw HttpException(
                Response.error<Page<AreaDataModel>>(
                    response.errorBody() ?: emptyJsonResponse(),
                    response.raw()
                )
            )
        }
    }

    private suspend fun cacheData(
        areaCode: String,
        alertLevels: BodyPage<AlertLevel>,
        lastModified: LocalDateTime
    ) {
        appDatabase.withTransaction {
            val metadataId = MetaDataIds.alertLevelId(areaCode)
            val it = alertLevels.body.maxByOrNull { it.date }!!
            appDatabase.areaDao().insert(AreaEntity(
                areaCode = it.areaCode,
                areaName = it.areaName,
                areaType = AreaType.from(it.areaType)!!
            ))
            appDatabase.alertLevelDao().insert(
                AlertLevelEntity(
                    areaCode = it.areaCode,
                    date = it.date,
                    alertLevel = it.alertLevel,
                    alertLevelName = it.alertLevelName,
                    alertLevelUrl = it.alertLevelUrl,
                    alertLevelValue = it.alertLevelValue
                )
            )
            appDatabase.metadataDao().insert(
                metadata = MetadataEntity(
                    id = metadataId,
                    lastUpdatedAt = lastModified,
                    lastSyncTime = timeProvider.currentTime()
                )
            )
        }
    }
}
