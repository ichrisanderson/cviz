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
import com.chrisa.cviz.core.data.db.HealthcareEntity
import com.chrisa.cviz.core.data.synchronisation.DailyData
import javax.inject.Inject

class HealthcareDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun healthcareData(areaCode: String): List<DailyData> {
        return allAdmissions(areaCode).filter(::hasHealthcareData)
            .map { areaData ->
                DailyData(
                    newValue = areaData.newAdmissions!!,
                    cumulativeValue = areaData.cumulativeAdmissions!!,
                    rate = 0.0,
                    date = areaData.date
                )
            }
    }

    private fun allAdmissions(areaCode: String) =
        appDatabase.healthcareDao().byAreaCode(areaCode)

    private fun hasHealthcareData(it: HealthcareEntity): Boolean {
        return it.cumulativeAdmissions != null &&
            it.newAdmissions != null
    }
}
