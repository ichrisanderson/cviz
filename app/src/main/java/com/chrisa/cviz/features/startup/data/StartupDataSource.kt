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

package com.chrisa.cviz.features.startup.data

import com.chrisa.cviz.core.data.db.AppDatabase
import javax.inject.Inject

class StartupDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun dataCount(): AreaData {
        return AreaData(
            appDatabase.areaDataDao().countAll(),
            appDatabase.areaSummaryEntityDao().countAll()
        )
    }
}

data class AreaData(private val areaData: Int, private val areaSummaryEntities: Int) {
    fun isNotEmpty(): Boolean =
        areaSummaryEntities > 0 && areaData > 0
}
