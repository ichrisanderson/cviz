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

package com.chrisa.covid19.core.data.synchronization

import com.chrisa.covid19.core.data.OfflineDataSource
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.data.network.MetadataModel
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import com.chrisa.covid19.core.util.NetworkUtils
import java.time.LocalDateTime
import javax.inject.Inject
import timber.log.Timber

class OverviewDataSynchronizer @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val offlineDataSource: OfflineDataSource,
    private val api: CovidApi
) {

    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) return
        val now = LocalDateTime.now()
        val areaMetadata = offlineDataSource.areaDataOverviewMetadata() ?: return

        if (areaMetadata.lastUpdatedAt.plusHours(1).isAfter(now)) {
            return
        }
        runCatching {
            api.pagedOverviewAreaDataResponse(areaMetadata.lastUpdatedAt.formatAsGmt())
        }.onSuccess { areasResponse ->
            if (areasResponse.isSuccessful) {
                val areas = areasResponse.body() ?: return@onSuccess
                offlineDataSource.withTransaction {
                    offlineDataSource.insertAreaData(areas.data)
                    offlineDataSource.insertAreaDataOverviewMetadata(MetadataModel(lastUpdatedAt = now))
                }
            }
        }.onFailure { error ->
            Timber.e(error, "Error synchronizing areas")
        }
    }
}
