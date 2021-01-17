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
import com.chrisa.cviz.core.data.db.AreaCaseData
import com.chrisa.cviz.core.data.synchronisation.DailyData
import javax.inject.Inject

class AreaCasesDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun areaName(areaCode: String): String =
        appDatabase.areaDao().byAreaCode(areaCode)!!.areaName

    fun cases(areaCode: String): List<DailyData> =
        allCases(areaCode).map(::mapDailyData)

    private fun allCases(areaCode: String) =
        appDatabase.areaDataDao().allAreaCasesByAreaCode(areaCode)

    private fun mapDailyData(areaData: AreaCaseData): DailyData {
        return DailyData(
            newValue = areaData.newCases,
            cumulativeValue = areaData.cumulativeCases,
            rate = areaData.infectionRate,
            date = areaData.date
        )
    }
}
