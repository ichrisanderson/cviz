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

package com.chrisa.cviz.core.data.synchronisation

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import javax.inject.Inject

internal class SavedAreaDataSynchroniser @Inject constructor(
    private val areaDataSynchroniser: AreaDataSynchroniser,
    private val soaDataSynchroniser: SoaDataSynchroniser,
    private val appDatabase: AppDatabase
) {

    suspend fun performSync() {
        val areas = listOf(
            AreaEntity(Constants.UK_AREA_CODE, Constants.UK_AREA_NAME, AreaType.OVERVIEW),
            AreaEntity(Constants.ENGLAND_AREA_CODE, Constants.ENGLAND_AREA_NAME, AreaType.NATION),
            AreaEntity(
                Constants.NORTHERN_IRELAND_AREA_CODE,
                Constants.NORTHERN_IRELAND_AREA_NAME,
                AreaType.NATION
            ),
            AreaEntity(Constants.SCOTLAND_AREA_CODE, Constants.SCOTLAND_AREA_NAME, AreaType.NATION),
            AreaEntity(Constants.WALES_AREA_CODE, Constants.WALES_AREA_NAME, AreaType.NATION)
        )
            .plus(appDatabase.areaDao().allSavedAreas())

        var error: Throwable? = null
        val areasWithoutSoa = areas.filterNot { it.areaType == AreaType.MSOA }
        areasWithoutSoa.forEach { area ->
            try {
                areaDataSynchroniser.performSync(area.areaCode, area.areaType)
            } catch (throwable: Throwable) {
                if (error == null) error = throwable
            }
        }
        val areasWithSoa = areas.filter { it.areaType == AreaType.MSOA }
        areasWithSoa.forEach { area ->
            try {
                soaDataSynchroniser.performSync(area.areaCode)
            } catch (throwable: Throwable) {
                if (error == null) error = throwable
            }
        }
        error?.let { throw it }
    }
}
