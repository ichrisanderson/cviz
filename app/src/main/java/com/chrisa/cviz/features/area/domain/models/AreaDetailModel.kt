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

package com.chrisa.cviz.features.area.domain.models

import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import java.time.LocalDateTime

data class AreaDetailModel(
    val lastUpdatedAt: LocalDateTime?,
    val lastSyncedAt: LocalDateTime?,
    val casesAreaName: String,
    val cases: List<DailyData>,
    val deathsByPublishedDateAreaName: String,
    val deathsByPublishedDate: List<DailyData>,
    val onsDeathAreaName: String,
    val onsDeathsByRegistrationDate: List<DailyData>,
    val hospitalAdmissionsAreaName: String,
    val hospitalAdmissions: List<AreaDailyDataDto>
)
