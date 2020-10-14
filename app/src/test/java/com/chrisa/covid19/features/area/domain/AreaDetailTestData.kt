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
import com.chrisa.covid19.core.data.synchronisation.WeeklySummary
import com.chrisa.covid19.features.area.data.dtos.AreaDetailDto
import com.chrisa.covid19.features.area.data.dtos.CaseDto
import com.chrisa.covid19.features.area.data.dtos.DeathDto
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.domain.models.DeathModel

data class AreaDetailTestData(
    val metadata: MetadataDto,
    val areaCode: String,
    val areaName: String,
    val areaType: AreaType,
    val weeklySummary: WeeklySummary,
    val cases: List<CaseDto>,
    val deaths: List<DeathDto>
) {
    private val lastCase = cases.lastOrNull()

    val areaDetail: AreaDetailDto
        get() = AreaDetailDto(
            areaCode,
            areaName,
            areaType.value,
            cases,
            deaths
        )

    val cumulativeCases: Int
        get() = lastCase?.cumulativeCases ?: 0
    val newCases: Int
        get() = lastCase?.newCases ?: 0
    val caseModels: List<CaseModel>
        get() = cases.map {
            CaseModel(
                newCases = it.newCases,
                date = it.date,
                rollingAverage = 1.0,
                cumulativeCases = it.cumulativeCases,
                baseRate = 0.0
            )
        }

    val deathModels: List<DeathModel>
        get() = deaths.map {
            DeathModel(
                newDeaths = it.newDeaths,
                date = it.date,
                rollingAverage = 1.0,
                cumulativeDeaths = it.cumulativeDeaths,
                baseRate = 0.0
            )
        }

    val areaDetailModel: AreaDetailModel
        get() = AreaDetailModel(
            areaType = areaType.value,
            lastUpdatedAt = metadata.lastUpdatedAt,
            lastSyncedAt = metadata.lastSyncTime,
            cumulativeCases = cumulativeCases,
            newCases = newCases,
            allCases = caseModels,
            weeklyCaseSummary = weeklySummary,
            deathsByPublishedDate = deathModels,
            weeklyDeathSummary = weeklySummary
        )
}
