/*
 * Copyright 2021 Chris Anderson.
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

package com.chrisa.cviz.features.area.presentation.mappers

import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import javax.inject.Inject

class AdmissionsFilter @Inject constructor() {

    fun filterHospitalData(
        hospitalAdmissions: List<AreaDailyDataDto>,
        hospitalAdmissionFilter: Set<String>
    ): List<DailyData> {
        var total = 0
        return hospitalAdmissions
            .filter { hospitalAdmissionFilter.isEmpty() || hospitalAdmissionFilter.contains(it.name) }
            .flatMap { it.data }
            .groupBy { it.date }
            .map { data ->
                val newAdmissions = data.value.sumOf { it.newValue }
                total += newAdmissions
                DailyData(
                    date = data.key,
                    cumulativeValue = total,
                    newValue = newAdmissions,
                    rate = 0.0
                )
            }
    }
}
