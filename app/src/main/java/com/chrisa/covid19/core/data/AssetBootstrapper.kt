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

import com.chrisa.covid19.core.data.network.MetadataModel
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import java.time.LocalDateTime
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
            val bootstrapAreas = async { bootstrapAreas() }
            val bootstrapOverview = async { bootstrapOverview() }
            bootstrapAreas.await()
            bootstrapOverview.await()
        }
    }

    private fun bootstrapAreas() {
        val areaCount = offlineDataSource.areaCount()
        if (areaCount > 0) return
        val areas = assetDataSource.getAreas()
        offlineDataSource.insertAreas(areas)
        offlineDataSource.insertAreaMetadata(
            MetadataModel(
                lastUpdatedAt = LocalDateTime.now().minusDays(1)
            )
        )
    }

    private fun bootstrapOverview() {
        val areaCount = offlineDataSource.areaDataOverviewCount()
        if (areaCount > 0) return
        val areas = assetDataSource.getOverviewAreaData()
        offlineDataSource.insertAreaData(areas)
        offlineDataSource.insertAreaDataOverviewMetadata(MetadataModel(lastUpdatedAt = LocalDateTime.now().minusDays(1)))
    }

    private fun bootstrapCases() {
//        val casesCount = offlineDataSource.casesCount()
//        if (casesCount > 0) return
//
//        val cases = assetDataSource.getCases()
//        val allCases = cases.countries.union(cases.ltlas).union(cases.utlas).union(cases.regions)
//
//        offlineDataSource.insertCaseMetadata(cases.metadata)
//        offlineDataSource.insertDailyRecord(cases.dailyRecords, cases.metadata.lastUpdatedAt.toLocalDate())
//        offlineDataSource.insertCases(allCases)
    }
}
