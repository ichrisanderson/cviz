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
import javax.inject.Inject
import timber.log.Timber

class SavedAreaDataSynchroniser @Inject constructor(
    private val unsafeAreaDataSynchroniser: UnsafeAreaDataSynchroniser,
    private val appDatabase: AppDatabase
) {

    suspend fun performSync() {
        val areas = appDatabase.areaDao().allSavedAreas()
        areas.forEach { area ->
            runCatching {
                unsafeAreaDataSynchroniser.performSync(area.areaCode, area.areaType)
            }.onFailure { error ->
                Timber.e(error, "Error syncing saved area ${area.areaCode}")
            }
        }
    }
}
