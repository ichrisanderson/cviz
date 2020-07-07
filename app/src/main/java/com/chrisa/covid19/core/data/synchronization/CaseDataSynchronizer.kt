package com.chrisa.covid19.core.data.synchronization

import com.chrisa.covid19.core.data.OfflineDataSource
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.util.DateUtils.addHours
import com.chrisa.covid19.core.util.DateUtils.toGmtDate
import javax.inject.Inject

class CaseDataSynchronizer @Inject constructor(
    private val offlineDataSource: OfflineDataSource,
    private val api: CovidApi
) {

    suspend fun performSync() {

        val caseMetadata = offlineDataSource.casesMetadata() ?: return
        val casesResponse = api.getCases(caseMetadata.lastUpdatedAt
            .addHours(1)
            .toGmtDate()
        )

        if (casesResponse.isSuccessful) {

            val cases = casesResponse.body()
            cases?.let {

                val allCases =
                    cases.countries.union(cases.ltlas).union(cases.utlas).union(cases.regions)

                offlineDataSource.insertCaseMetadata(cases.metadata)
                offlineDataSource.insertDailyRecord(
                    cases.dailyRecords,
                    cases.metadata.lastUpdatedAt
                )
                offlineDataSource.insertCases(allCases)
            }
        }
    }
}
