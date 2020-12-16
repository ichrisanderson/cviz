package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDeathData
import com.chrisa.cviz.core.data.synchronisation.DailyData
import javax.inject.Inject

class AreaDeathsByPublishedDateDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) : AreaDeathsDataSource {

    override fun deaths(areaCode: String): List<DailyData> =
        allDeaths(areaCode).filter(::hasPublishedDeaths).map { areaData ->
            DailyData(
                newValue = areaData.newDeathsByPublishedDate!!,
                cumulativeValue = areaData.cumulativeDeathsByPublishedDate!!,
                rate = areaData.cumulativeDeathsByPublishedDateRate!!,
                date = areaData.date
            )
        }

    private fun allDeaths(areaCode: String) =
        appDatabase.areaDataDao().allAreaDeathsByAreaCode(areaCode)

    private fun hasPublishedDeaths(areaDeathData: AreaDeathData): Boolean {
        return areaDeathData.cumulativeDeathsByPublishedDate != null &&
            areaDeathData.newDeathsByPublishedDate != null &&
            areaDeathData.cumulativeDeathsByPublishedDateRate != null
    }
}
