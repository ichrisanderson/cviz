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

package com.chrisa.covid19.core.data

import androidx.room.withTransaction
import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataEntity
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.MetadataModel
import javax.inject.Inject

class OfflineDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    suspend fun withTransaction(block: suspend () -> Unit) {
        return appDatabase.withTransaction(block)
    }

    fun areaCount(): Int {
        return appDatabase.areaDao().count()
    }

    fun areaMetadata(): MetadataModel? {
        val metadata =
            appDatabase.metadataDao().metadata(MetadataEntity.AREA_METADATA_ID) ?: return null
        return MetadataModel(
            lastUpdatedAt = metadata.lastUpdatedAt
        )
    }

    fun insertAreaMetadata(metadata: MetadataModel) {
        appDatabase.metadataDao().insertAll(
            listOf(
                MetadataEntity(
                    id = MetadataEntity.AREA_METADATA_ID,
                    lastUpdatedAt = metadata.lastUpdatedAt
                )
            )
        )
    }

    fun insertAreas(areas: Collection<AreaModel>) {
        appDatabase.areaDao().insertAll(areas.map {
            AreaEntity(
                areaCode = it.areaCode,
                areaName = it.areaName,
                areaType = it.areaType
            )
        })
    }

    fun areaDataOverviewCount(): Int {
        return appDatabase.areaDataDao().countAllByType("overview")
    }

    fun areaDataOverviewMetadata(): MetadataModel? {
        val metadata =
            appDatabase.metadataDao().metadata(MetadataEntity.AREA_DATA_OVERVIEW_METADATA_ID) ?: return null
        return MetadataModel(
            lastUpdatedAt = metadata.lastUpdatedAt
        )
    }

    fun insertAreaDataOverviewMetadata(metadata: MetadataModel) {
        appDatabase.metadataDao().insertAll(
            listOf(
                MetadataEntity(
                    id = MetadataEntity.AREA_DATA_OVERVIEW_METADATA_ID,
                    lastUpdatedAt = metadata.lastUpdatedAt
                )
            )
        )
    }

    fun insertAreaData(areas: Collection<AreaDataModel>) {
        appDatabase.areaDataDao().insertAll(areas.map {
            AreaDataEntity(
                areaCode = it.areaCode,
                areaName = it.areaName,
                areaType = it.areaType,
                newCases = it.newCases ?: 0,
                cumulativeCases = it.cumulativeCases ?: 0,
                date = it.date,
                infectionRate = it.infectionRate ?: 0.0
            )
        })
    }
//
//    fun deleteAllCases() {
//        caseDao.deleteAll()
//    }
//
//    fun casesCount(): Int {
//        return caseDao.casesCount()
//    }
//
//    fun casesMetadata(): MetadataModel? {
//        return metadataDao.metadata(CASE_METADATA_ID).map {
//            MetadataModel(
//                disclaimer = it.disclaimer,
//                lastUpdatedAt = it.lastUpdatedAt
//            )
//        }.firstOrNull()
//    }
//    fun insertCaseMetadata(metadata: MetadataModel) {
//    }
//
//    fun insertDailyRecord(dailyRecord: DailyRecordModel, date: LocalDate) {
//    }
//
//    fun insertCases(cases: Collection<CaseModel>) {
//    }
}
