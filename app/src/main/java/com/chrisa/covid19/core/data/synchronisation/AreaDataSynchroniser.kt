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
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AREA_DATA_FILTER
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaDataModelStructureMapper
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.DateUtils.toGmtDateTime
import com.chrisa.covid19.core.util.NetworkUtils
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

class AreaDataSynchroniser @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val areaDataModelStructureMapper: AreaDataModelStructureMapper,
    private val api: CovidApi
) {

    suspend fun performSync(areaCode: String, areaType: String, onError: (error: Throwable) -> Unit) {
        runCatching {
            syncArea(areaCode, areaType)
        }.onFailure { error ->
            onError(error)
            Timber.e(error, "Error syncing saved area $areaCode")
        }
    }

    private suspend fun syncArea(areaCode: String, areaType: String) {
        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val areaMetadata = appDatabase.metadataDao().metadata(MetaDataIds.areaCodeId(areaCode))

        val casesFromNetwork = api.pagedAreaDataResponse(
            modifiedDate = areaMetadata?.lastUpdatedAt?.formatAsGmt(),
            filters = AREA_DATA_FILTER(areaCode, areaType),
            structure = areaDataModelStructureMapper.mapAreaTypeToDataModel(
                areaType
            )
        )
        if (casesFromNetwork.isSuccessful) {
            val pagedAreaCodeData = casesFromNetwork.body()!!
            val lastModified = casesFromNetwork.headers().get("Last-Modified")
            cacheAreaData(
                areaCode,
                pagedAreaCodeData,
                lastModified?.toGmtDateTime() ?: LocalDateTime.now()
            )
        } else {
            throw HttpException(
                Response.error<Page<AreaDataModel>>(
                    casesFromNetwork.errorBody() ?: ResponseBody.create(
                        MediaType.get("application/json"), ""
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
}
