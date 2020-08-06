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

package com.chrisa.covid19.features.area.domain.helper

import com.chrisa.covid19.features.area.data.dtos.CaseDto
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class RollingAverageHelper @Inject constructor() {
    fun average(currentCase: CaseDto, previousCase: CaseDto?): Double {
        if (previousCase == null) return 0.0
        val days = ChronoUnit.DAYS.between(previousCase.date, currentCase.date)
        return (currentCase.totalLabConfirmedCases - previousCase.totalLabConfirmedCases) / (days + 1).toDouble()
    }
}
