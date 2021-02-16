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

package com.chrisa.cviz.core.data.db

import androidx.room.withTransaction
import javax.inject.Inject

class DatabaseCleaner @Inject constructor(
    private val appDatabase: AppDatabase,
    private val snapshotProvider: SnapshotProvider
) {
    suspend fun removeUnusedData() =
        appDatabase.withTransaction {
            val snapshot = snapshotProvider.getSnapshot(appDatabase)

            appDatabase.areaLookupDao().deleteAllNotInLsoaCode(snapshot.lsoaAreaCodes)
            appDatabase.soaDataDao().deleteAllNotInAreaCode(snapshot.msoaAreaCodes)
            appDatabase.alertLevelDao().deleteAllNotInAreaCode(snapshot.alertLevelAreaCodes)
            appDatabase.areaDataDao().deleteAllNotInAreaCode(snapshot.localAndNationalAreaDataCodes)
            appDatabase.healthcareDao().deleteAllNotInAreaCode(snapshot.healthcareAreaCodes)
            appDatabase.metadataDao().deleteAllNotInId(snapshot.metadataIds)
        }
}
