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

package com.chrisa.cviz.core.data.db.legacy

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LegacyAppDatabaseHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasDatabase() =
        context.getDatabasePath(LegacyAppDatabase.databaseName).exists()

    fun savedAreaCodes(): List<String> =
        LegacyAppDatabase.buildDatabase(context)
            .savedAreaDao()
            .all()
            .map { it.areaCode }

    fun deleteDatabase() =
        context.deleteDatabase(LegacyAppDatabase.databaseName)
}
