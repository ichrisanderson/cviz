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

package com.chrisa.cviz.core.data.db.hospitallookups

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.HealthcareLookupEntity
import javax.inject.Inject

class HospitalLookupHelper @Inject constructor(
    appDatabase: AppDatabase,
    private val hospitalLookupsDataSource: HospitalLookupsAssetDataSource
) {
    private val healthcareLookupDao = appDatabase.healthcareLookupDao()

    fun insertHospitalLookupData() {
        val items = healthcareLookupDao.countAll()
        if (items > 0) return

        val hospitalLookupList = hospitalLookupsDataSource.getItems()

        hospitalLookupList.forEach {
            healthcareLookupDao.insert(
                HealthcareLookupEntity(areaCode = it.areaCode, nhsTrustCode = it.trustCode)
            )
        }
    }
}
