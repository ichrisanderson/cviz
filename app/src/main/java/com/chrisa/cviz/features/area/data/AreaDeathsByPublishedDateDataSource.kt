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
