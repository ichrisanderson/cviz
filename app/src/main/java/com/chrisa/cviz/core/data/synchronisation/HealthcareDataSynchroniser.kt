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
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.HealthcareEntity
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.network.AREA_DATA_FILTER
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.HealthcareData
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

interface HealthcareDataSynchroniser {
    suspend fun performSync(areaCode: String, areaType: AreaType)
}

internal class HealthcareDataSynchroniserImpl @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) : HealthcareDataSynchroniser {

    override suspend fun performSync(
        areaCode: String,
        areaType: AreaType
    ) {
        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val metadata = appDatabase.metadataDao().metadata(MetaDataIds.healthcareId(areaCode))
        val now = timeProvider.currentTime()
        if (metadata != null && metadata.lastSyncTime.plusMinutes(5).isAfter(now)) {
            return
        }

        val response = api.pagedHealthcareDataResponse(
            modifiedDate = metadata?.lastUpdatedAt?.formatAsGmt(),
            filters = AREA_DATA_FILTER(areaCode, areaType.value),
            structure = HealthcareData.STRUCTURE
        )

        if (response.isSuccessful) {
            val pagedAreaCodeData = response.body()!!
            val lastModified = response.headers()["Last-Modified"]
            cacheData(
                areaCode,
                pagedAreaCodeData,
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
        pagedAreaCodeData: Page<HealthcareData>,
        lastModified: LocalDateTime
    ) {
        appDatabase.withTransaction {
            val metadataId = MetaDataIds.healthcareId(areaCode)
            val dataToInsert = pagedAreaCodeData.data.filter { it.occupiedBeds != null }
            appDatabase.healthcareDao().deleteAllByAreaCode(areaCode)
            appDatabase.healthcareDao().insertAll(dataToInsert.map {
                HealthcareEntity(
                    areaCode = it.areaCode,
                    areaName = it.areaName,
                    areaType = AreaType.from(it.areaType)!!,
                    date = it.date,
                    newAdmissions = it.newAdmissions,
                    cumulativeAdmissions = it.cumulativeAdmissions,
                    occupiedBeds = it.occupiedBeds,
                    transmissionRateMin = it.transmissionRateMin,
                    transmissionRateMax = it.transmissionRateMax,
                    transmissionRateGrowthRateMin = it.transmissionRateGrowthRateMin,
                    transmissionRateGrowthRateMax = it.transmissionRateGrowthRateMax
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
