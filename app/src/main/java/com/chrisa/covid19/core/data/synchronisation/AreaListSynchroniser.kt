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
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AREA_FILTER
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaModel.Companion.AREA_MODEL_STRUCTURE
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.Page
import com.chrisa.covid19.core.data.time.TimeProvider
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.DateUtils.toGmtDateTime
import com.chrisa.covid19.core.util.NetworkUtils
import java.io.IOException
import javax.inject.Inject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response

class AreaListSynchroniser @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) {

    private val metadataDao = appDatabase.metadataDao()
    private val areaDao = appDatabase.areaDao()

    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) throw IOException()

        val now = timeProvider.currentTime()
        val areaMetadata = metadataDao.metadata(MetaDataIds.areaListId()) ?: return

        if (areaMetadata.lastUpdatedAt.plusHours(1).isAfter(now)) {
            return
        }

        val areasResponse = api.pagedAreaResponse(
            areaMetadata.lastUpdatedAt.formatAsGmt(),
            AREA_FILTER,
            AREA_MODEL_STRUCTURE
        )

        if (areasResponse.isSuccessful) {
            val lastModified = areasResponse.headers().get("Last-Modified")
            val areas = areasResponse.body()!!
            appDatabase.withTransaction {
                areaDao.insertAll(areas.data.map {
                    AreaEntity(
                        areaType = AreaType.from(it.areaType)!!,
                        areaName = it.areaName,
                        areaCode = it.areaCode
                    )
                })
                metadataDao.insert(
                    MetadataEntity(
                        id = MetaDataIds.areaListId(),
                        lastUpdatedAt = lastModified?.toGmtDateTime() ?: timeProvider.currentTime(),
                        lastSyncTime = timeProvider.currentTime()
                    )
                )
            }
        } else {
            throw HttpException(
                Response.error<Page<AreaDataModel>>(
                    areasResponse.errorBody() ?: ResponseBody.create(
                        "application/json".toMediaType(), ""
                    ),
                    areasResponse.raw()
                )
            )
        }
    }
}
