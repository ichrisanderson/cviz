package com.chrisa.covid19.core.data.synchronization

import com.chrisa.covid19.core.data.OfflineDataSource
import com.chrisa.covid19.core.data.network.CovidApi
import com.chrisa.covid19.core.util.DateUtils.addHours
import com.chrisa.covid19.core.util.DateUtils.toGmtDate
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
            deathMetadata.lastUpdatedAt
                .addHours(1)
                .toGmtDate()
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
