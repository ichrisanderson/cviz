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
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.core.data.db.SoaDataEntity
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.Page
import com.chrisa.cviz.core.data.network.RollingChangeModel
import com.chrisa.cviz.core.data.network.SoaDataModel
import com.chrisa.cviz.core.data.network.Utils
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.core.util.DateUtils.formatAsGmt
import com.chrisa.cviz.core.util.DateUtils.toGmtDateTime
import com.chrisa.cviz.core.util.NetworkUtils
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import retrofit2.HttpException
import retrofit2.Response

interface SoaDataSynchroniser {
    suspend fun performSync(areaCode: String)
}

internal class SoaDataSynchroniserImpl @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) : SoaDataSynchroniser {

    override suspend fun performSync(
        areaCode: String
    ) {
        val areaMetadata = appDatabase.metadataDao().metadata(MetadataIds.areaCodeId(areaCode))
        val now = timeProvider.currentTime()
        if (areaMetadata != null && areaMetadata.lastSyncTime.plusMinutes(5).isAfter(now)) {
            return
        }

        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val soaDataModelResponse = api.soaData(
            modifiedDate = areaMetadata?.lastUpdatedAt?.formatAsGmt(),
            filters = SoaDataModel.maosFilter(areaCode)
        )
        if (soaDataModelResponse.isSuccessful) {
            val soaDataModel = soaDataModelResponse.body()!!
            val lastModified = soaDataModelResponse.headers()["Last-Modified"]
            cacheData(
                areaCode,
                soaDataModel,
                lastModified?.toGmtDateTime() ?: timeProvider.currentTime()
            )
        } else {
            throw HttpException(
                Response.error<Page<AreaDataModel>>(
                    soaDataModelResponse.errorBody() ?: Utils.emptyJsonResponse(),
                    soaDataModelResponse.raw()
                )
            )
        }
    }

    private suspend fun cacheData(
        areaCode: String,
        soaDataModel: SoaDataModel,
        lastModified: LocalDateTime
    ) {
        appDatabase.withTransaction {
            val metadataId = MetadataIds.areaCodeId(areaCode)
            appDatabase.soaDataDao().deleteAllByAreaCode(areaCode)
            appDatabase.areaDao().insert(
                AreaEntity(
                    soaDataModel.areaCode,
                    soaDataModel.areaName,
                    AreaType.from(soaDataModel.areaType)!!
                )
            )
            appDatabase.metadataDao().insert(
                metadata = MetadataEntity(
                    id = metadataId,
                    lastUpdatedAt = lastModified,
                    lastSyncTime = timeProvider.currentTime()
                )
            )
            appDatabase.soaDataDao().insertAll(
                soaDataModel.newCasesBySpecimenDate
                    .filter(::hasRollingData)
                    .map { rollingChangeModel ->
                        SoaDataEntity(
                            areaCode = soaDataModel.areaCode,
                            metadataId = metadataId,
                            date = rollingChangeModel.date,
                            rollingSum = rollingChangeModel.rollingSum!!,
                            rollingRate = rollingChangeModel.rollingRate!!,
                            change = rollingChangeModel.change!!,
                            changePercentage = rollingChangeModel.changePercentage!!
                        )
                    })
        }
    }

    private fun hasRollingData(rollingChangeModel: RollingChangeModel): Boolean =
        rollingChangeModel.change != null &&
            rollingChangeModel.changePercentage != null &&
            rollingChangeModel.rollingRate != null &&
            rollingChangeModel.rollingSum != null
}
