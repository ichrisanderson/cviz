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

package com.chrisa.covid19.core.data

import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class AssetBootstrapper @Inject constructor(
    private val assetDataSource: AssetDataSource,
    private val offlineDataSource: OfflineDataSource,
    private val coroutineDispatchers: CoroutineDispatchers
) : Bootstrapper {

    override suspend fun bootstrapData() {
        return withContext(coroutineDispatchers.io) {
            val bootstrapCases = async { bootstrapCases() }
            val bootstrapDeaths = async { bootstrapDeaths() }
            bootstrapCases.await()
            bootstrapDeaths.await()
        }
    }

    private fun bootstrapCases() {
        val casesCount = offlineDataSource.casesCount()
        if (casesCount > 0) return

        val cases = assetDataSource.getCases()
        val allCases = cases.countries.union(cases.ltlas).union(cases.utlas).union(cases.regions)

        offlineDataSource.insertCaseMetadata(cases.metadata)
        offlineDataSource.insertDailyRecord(cases.dailyRecords, cases.metadata.lastUpdatedAt)
        offlineDataSource.insertCases(allCases)
    }

    private fun bootstrapDeaths() {
        val deathsCount = offlineDataSource.deathsCount()
        if (deathsCount > 0) return

        val deaths = assetDataSource.getDeaths()
        val allDeaths = deaths.countries.union(deaths.overview)

        offlineDataSource.insertDeathMetadata(deaths.metadata)
        offlineDataSource.insertDeaths(allDeaths)
    }
}
