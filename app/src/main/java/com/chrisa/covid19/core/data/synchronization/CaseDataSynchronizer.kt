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
import com.chrisa.covid19.core.util.NetworkUtils
import java.time.LocalDateTime
import javax.inject.Inject
import timber.log.Timber

class CaseDataSynchronizer @Inject constructor(
    private val networkUtils: NetworkUtils,
    private val offlineDataSource: OfflineDataSource,
    private val api: CovidApi
) {

    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) return
        val caseMetadata = offlineDataSource.casesMetadata() ?: return

        val now = LocalDateTime.now()
        if (caseMetadata.lastUpdatedAt.plusHours(1).isAfter(now)) {
            return
        }

        runCatching {
            api.getCases(
                caseMetadata.lastUpdatedAt
                    .plusHours(1)
                    .formatAsGmt()
            )
        }.onSuccess { casesResponse ->
            if (casesResponse.isSuccessful) {
                val cases = casesResponse.body()
                cases?.let {

                    val allCases =
                        cases.countries.union(cases.ltlas).union(cases.utlas)
                            .union(cases.regions)

                    offlineDataSource.insertCaseMetadata(cases.metadata)
                    offlineDataSource.insertDailyRecord(
                        cases.dailyRecords,
                        cases.metadata.lastUpdatedAt.toLocalDate()
                    )
                    offlineDataSource.insertCases(allCases)
                }
            }
        }.onFailure { error ->
            Timber.e(error, "Error synchronizing cases")
        }
    }
}
