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
import com.chrisa.cviz.core.data.db.HealthcareWithArea
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import javax.inject.Inject

class AdmissionsDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun admissionsForArea(areaCode: String): List<DailyData> =
        allHealthcareData(areaCode).filter(::hasAdmissions).map(::mapDailyData)

    private fun allHealthcareData(areaCode: String) =
        appDatabase.healthcareDao().withAreaByAreaCodes(listOf(areaCode))

    private fun hasAdmissions(it: HealthcareWithArea): Boolean =
        it.healthcare.cumulativeAdmissions != null && it.healthcare.newAdmissions != null

    fun admissionsForAreaCodes(areaCodes: List<String>): List<AreaDailyDataDto> =
        allAdmissionsInAreaCodes(areaCodes)
            .filter(::hasAdmissions)
            .groupBy { it.areaName }
            .map { AreaDailyDataDto(it.key, it.value.map(::mapDailyData)) }

    private fun mapDailyData(healthcareWithArea: HealthcareWithArea): DailyData {
        return DailyData(
            newValue = healthcareWithArea.healthcare.newAdmissions!!,
            cumulativeValue = healthcareWithArea.healthcare.cumulativeAdmissions!!,
            rate = 0.0,
            date = healthcareWithArea.healthcare.date
        )
    }

    private fun allAdmissionsInAreaCodes(areaCodes: List<String>) =
        appDatabase.healthcareDao().withAreaByAreaCodes(areaCodes)
}
