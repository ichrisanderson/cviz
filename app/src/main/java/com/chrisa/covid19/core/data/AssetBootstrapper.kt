package com.chrisa.covid19.core.data

import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

