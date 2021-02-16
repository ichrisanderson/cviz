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

package com.chrisa.cviz.core.data.db

import com.chrisa.cviz.core.data.db.hospitallookups.HospitalLookupHelper
import com.chrisa.cviz.core.data.db.legacy.LegacyAppDatabaseHelper
import javax.inject.Inject

class Bootstrapper @Inject constructor(
    private val appDatabase: AppDatabase,
    private val legacyAppDatabaseHelper: LegacyAppDatabaseHelper,
    private val hospitalLookupHelper: HospitalLookupHelper,
    private val databaseCleaner: DatabaseCleaner
) {
    suspend fun execute() {
        val didCopyOldData = copyOldData()
        insertAreaData(didCopyOldData)
        hospitalLookupHelper.insertHospitalLookupData()
        databaseCleaner.removeUnusedData()
    }

    private fun copyOldData(): Boolean {
        var copiedOldData = false
        if (legacyAppDatabaseHelper.hasDatabase()) {
            val items = appDatabase.savedAreaDao().countAll()
            if (items == 0) {
                val savedAreas =
                    legacyAppDatabaseHelper.savedAreas()
                appDatabase.areaDao().insertAll(savedAreas.map {
                    AreaEntity(
                        it.areaCode,
                        it.areaName,
                        AreaType.from(it.areaType)!!
                    )
                })
                appDatabase.savedAreaDao().insertAll(savedAreas
                    .map { SavedAreaEntity(it.areaCode) }
                )
                copiedOldData = true
            }
            legacyAppDatabaseHelper.deleteDatabase()
        }
        return copiedOldData
    }

    private fun insertAreaData(forceRefresh: Boolean) {
        if (!forceRefresh && appDatabase.areaDao().count() > 0) return
        appDatabase.areaDao().insertAll(BootstrapData.areaData())
    }
}
