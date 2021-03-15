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
import com.chrisa.cviz.core.data.time.TimeProvider
import javax.inject.Inject

class ExpiredDataCleaner @Inject constructor(
    private val appDatabase: AppDatabase,
    private val snapshotProvider: SnapshotProvider,
    private val timeProvider: TimeProvider
) {
    suspend fun execute() =
        appDatabase.withTransaction {
            val snapshot = snapshotProvider.getSnapshot(appDatabase)
            val cutoffDate = timeProvider.currentTime().minusDays(2)

            val outOfDateMetadata =
                snapshot.metadata.filter { it.lastSyncTime < cutoffDate }

            val outOfDateMetadataIds = outOfDateMetadata.map { it.id }.toSet()

            val outOfDateSoaData =
                snapshot.soaDataAreaCodes.filter {
                    outOfDateMetadataIds.contains(
                        MetadataIds.areaCodeId(
                            it
                        )
                    )
                }
            val outOfDateAlertLevels =
                snapshot.alertLevelAreaCodes.filter {
                    outOfDateMetadataIds.contains(
                        MetadataIds.alertLevelId(
                            it
                        )
                    )
                }
            val outOfDateAreaData =
                snapshot.areaDataAreaCodes.filter {
                    outOfDateMetadataIds.contains(
                        MetadataIds.areaCodeId(
                            it
                        )
                    )
                }
            val outOfDateHealthCare =
                snapshot.healthcareAreaCodes.filter {
                    outOfDateMetadataIds.contains(
                        MetadataIds.healthcareId(
                            it
                        )
                    )
                }

            appDatabase.soaDataDao().deleteAllInAreaCode(outOfDateSoaData)
            appDatabase.areaDataDao().deleteAllInAreaCode(outOfDateAreaData)
            appDatabase.alertLevelDao().deleteAllInAreaCode(outOfDateAlertLevels)
            appDatabase.healthcareDao().deleteAllInAreaCode(outOfDateHealthCare)
            appDatabase.metadataDao().deleteAllInId(outOfDateMetadataIds)
        }
}
