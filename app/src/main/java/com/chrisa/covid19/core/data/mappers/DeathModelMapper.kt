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

package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.DeathEntity
import com.chrisa.covid19.core.data.network.DeathModel
import javax.inject.Inject

class DeathModelMapper @Inject constructor() {

    fun mapToDeathsEntity(deathModel: DeathModel): DeathEntity {
        return DeathEntity(
            areaCode = deathModel.areaCode,
            areaName = deathModel.areaName,
            date = deathModel.reportingDate,
            dailyChangeInDeaths = deathModel.dailyChangeInDeaths ?: 0,
            cumulativeDeaths = deathModel.cumulativeDeaths
        )
    }
}
