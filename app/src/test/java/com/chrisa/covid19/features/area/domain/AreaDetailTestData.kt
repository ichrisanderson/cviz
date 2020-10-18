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

package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.synchronisation.DailyData
import com.chrisa.covid19.core.data.synchronisation.WeeklySummary
import com.chrisa.covid19.features.area.data.dtos.AreaDetailDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel

data class AreaDetailTestData(
    val metadata: MetadataDto,
    val areaCode: String,
    val areaName: String,
    val areaType: AreaType,
    val cases: List<DailyData>,
    val caseSummary: WeeklySummary,
    val deaths: List<DailyData>,
    val deathSummary: WeeklySummary
) {

    val areaDetail: AreaDetailDto
        get() = AreaDetailDto(
            areaCode,
            areaName,
            areaType.value,
            cases,
            deaths
        )

    val areaDetailModel: AreaDetailModel
        get() = AreaDetailModel(
            areaType = areaType.value,
            lastSyncedAt = metadata.lastSyncTime,
            allCases = cases,
            caseSummary = caseSummary,
            allDeaths = deaths,
            deathSummary = deathSummary
        )
}
