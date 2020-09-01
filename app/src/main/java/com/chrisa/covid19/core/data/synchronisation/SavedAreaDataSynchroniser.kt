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

package com.chrisa.covid19.core.data.synchronisation

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import javax.inject.Inject

class SavedAreaDataSynchroniser @Inject constructor(
    private val areaDataSynchroniser: AreaDataSynchroniser,
    private val appDatabase: AppDatabase
) {

    suspend fun performSync() {
        val areas = listOf(
            AreaEntity(Constants.UK_AREA_CODE, "UK", AreaType.OVERVIEW),
            AreaEntity(Constants.ENGLAND_AREA_CODE, "England", AreaType.NATION),
            AreaEntity(Constants.NORTHERN_IRELAND_AREA_CODE, "Northern Ireland", AreaType.NATION),
            AreaEntity(Constants.SCOTLAND_AREA_CODE, "Scotland", AreaType.NATION),
            AreaEntity(Constants.WALES_AREA_CODE, "Wales", AreaType.NATION)
        )
            .plus(appDatabase.areaDao().allSavedAreas())

        areas.forEach { area ->
            try {
                areaDataSynchroniser.performSync(area.areaCode, area.areaType)
            } catch (throwable: Throwable) {
                throw throwable
            }
        }
    }
}
