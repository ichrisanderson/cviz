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
import com.chrisa.covid19.core.util.DateUtils.formatAsGmt
import javax.inject.Inject

class DeathDataSynchronizer @Inject constructor(
    private val offlineDataSource: OfflineDataSource,
    private val api: CovidApi
) {

    suspend fun performSync() {
        syncDeaths()
    }

    private suspend fun syncDeaths() {

        val deathMetadata = offlineDataSource.deathsMetadata() ?: return
        val deathsResponse = api.getDeaths(
            deathMetadata.lastUpdatedAt.plusHours(1)
                .formatAsGmt()
        )

        if (deathsResponse.isSuccessful) {
            val deaths = deathsResponse.body()
            deaths?.let {
                val allDeaths = deaths.countries.union(deaths.overview)
                offlineDataSource.insertDeathMetadata(deaths.metadata)
                offlineDataSource.insertDeaths(allDeaths)
            }
        }
    }
}
