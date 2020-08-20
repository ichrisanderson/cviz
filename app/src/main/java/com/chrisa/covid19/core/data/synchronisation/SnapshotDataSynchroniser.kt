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
import com.chrisa.covid19.core.data.network.AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.DAILY_AREA_DATA_FILTER
import com.chrisa.covid19.core.util.DateUtils.formatAsIso8601
import com.chrisa.covid19.core.util.NetworkUtils
import java.time.LocalDate
import javax.inject.Inject
import timber.log.Timber

class SnapshotDataSynchroniser @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val api: CovidApi
) {

    private val areaDataDao = appDatabase.areaDataDao()

    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) return

        val now = LocalDate.now()

        val syncDates = listOf(
            now,
            now.minusDays(1),
            now.minusDays(7),
            now.minusDays(8),
            now.minusDays(14),
            now.minusDays(15)
        )

        syncDates.forEach { syncDate ->
            runCatching {
                api.pagedAreaDataResponse(
                    modifiedDate = null,
                    filters = DAILY_AREA_DATA_FILTER(syncDate.formatAsIso8601(), "ltla"),
                    areaDataModelStructure = AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
                )
            }.onSuccess { areasResponse ->
                if (areasResponse.isSuccessful) {
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
                    }
                }
            }.onFailure { error ->
                Timber.e(error, "Error synchronizing areas for date $syncDate")
            }
        }
    }
}
