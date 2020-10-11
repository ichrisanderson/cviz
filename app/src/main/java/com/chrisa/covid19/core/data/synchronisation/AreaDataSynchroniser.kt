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
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AREA_DATA_FILTER
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaDataModelStructureMapper
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.data.time.TimeProvider
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

class AreaDataSynchroniser @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val areaDataModelStructureMapper: AreaDataModelStructureMapper,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) {

    suspend fun performSync(
        areaCode: String,
        areaType: AreaType
    ) {
        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val areaMetadata = appDatabase.metadataDao().metadata(MetaDataIds.areaCodeId(areaCode))

        val casesFromNetwork = api.pagedAreaDataResponse(
            modifiedDate = areaMetadata?.lastUpdatedAt?.formatAsGmt(),
            filters = AREA_DATA_FILTER(areaCode, areaType.value),
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
                lastModified?.toGmtDateTime() ?: timeProvider.currentTime()
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
                    cumulativeDeathsByDeathDateRate = it.cumulativeDeathsByDeathDateRate,
                    newAdmissions = it.newAdmissions,
                    cumulativeAdmissions = it.cumulativeAdmissions,
                    occupiedBeds = it.occupiedBeds
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
