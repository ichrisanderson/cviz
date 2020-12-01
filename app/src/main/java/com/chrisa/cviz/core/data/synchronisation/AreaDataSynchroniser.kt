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
import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDataEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.network.AREA_DATA_FILTER
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.AreaDataModelStructureMapper
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.Page
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.core.util.DateUtils.formatAsGmt
import com.chrisa.cviz.core.util.DateUtils.toGmtDateTime
import com.chrisa.cviz.core.util.NetworkUtils
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

interface AreaDataSynchroniser {
    suspend fun performSync(areaCode: String, areaType: AreaType)
}

internal class AreaDataSynchroniserImpl @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val areaDataModelStructureMapper: AreaDataModelStructureMapper,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) : AreaDataSynchroniser {

    override suspend fun performSync(
        areaCode: String,
        areaType: AreaType
    ) {
        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val areaMetadata = appDatabase.metadataDao().metadata(MetaDataIds.areaCodeId(areaCode))
        val now = timeProvider.currentTime()
        if (areaMetadata != null && areaMetadata.lastSyncTime.plusMinutes(5).isAfter(now)) {
            return
        }

        val casesFromNetwork = api.pagedAreaDataResponse(
            modifiedDate = areaMetadata?.lastUpdatedAt?.formatAsGmt(),
            filters = AREA_DATA_FILTER(areaCode, areaType.value),
            structure = areaDataModelStructureMapper.mapAreaTypeToDataModel(
                areaType
            )
        )
        if (casesFromNetwork.isSuccessful) {
            val pagedAreaCodeData = casesFromNetwork.body()!!
            val lastModified = casesFromNetwork.headers()["Last-Modified"]
            cacheAreaData(
                areaCode,
                pagedAreaCodeData,
                lastModified?.toGmtDateTime() ?: timeProvider.currentTime()
            )
        } else {
            throw HttpException(
                Response.error<Page<AreaDataModel>>(
                    casesFromNetwork.errorBody() ?: ResponseBody.create(
                        "application/json".toMediaType(), ""
                    ),
                    casesFromNetwork.raw()
                )
            )
        }
    }

    private suspend fun cacheAreaData(
        areaCode: String,
        pagedAreaCodeData: Page<AreaDataModel>,
        lastModified: LocalDateTime
    ) {
        appDatabase.withTransaction {
            val metadataId = MetaDataIds.areaCodeId(areaCode)
            val dataToInsert = pagedAreaCodeData.data.filter { it.cumulativeCases != null }
            appDatabase.areaDataDao().deleteAllByAreaCode(areaCode)
            appDatabase.areaDataDao().insertAll(dataToInsert.map {
                AreaDataEntity(
                    metadataId = metadataId,
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = AreaType.from(it.areaType)!!,
                    cumulativeCases = it.cumulativeCases ?: 0,
                    date = it.date,
                    infectionRate = it.infectionRate ?: 0.0,
                    newCases = it.newCases ?: 0,
                    newDeathsByPublishedDate = it.newDeathsByPublishedDate,
                    cumulativeDeathsByPublishedDate = it.cumulativeDeathsByPublishedDate,
                    cumulativeDeathsByPublishedDateRate = it.cumulativeDeathsByPublishedDateRate,
                    newDeathsByDeathDate = it.newDeathsByDeathDate,
                    cumulativeDeathsByDeathDate = it.cumulativeDeathsByDeathDate,
                    cumulativeDeathsByDeathDateRate = it.cumulativeDeathsByDeathDateRate
                )
            })
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
