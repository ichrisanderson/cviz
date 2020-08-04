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
import com.chrisa.covid19.core.data.db.CaseDao
import com.chrisa.covid19.core.data.db.DailyRecordDao
import com.chrisa.covid19.core.data.db.DeathDao
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.MetadataEntity.Companion.CASE_METADATA_ID
import com.chrisa.covid19.core.data.db.MetadataEntity.Companion.DEATH_METADATA_ID
import com.chrisa.covid19.core.data.mappers.CaseModelMapper
import com.chrisa.covid19.core.data.mappers.DailyRecordModelMapper
import com.chrisa.covid19.core.data.mappers.DeathModelMapper
import com.chrisa.covid19.core.data.mappers.MetadataModelMapper
import com.chrisa.covid19.core.data.network.CaseModel
import com.chrisa.covid19.core.data.network.DailyRecordModel
import com.chrisa.covid19.core.data.network.DeathModel
import com.chrisa.covid19.core.data.network.MetadataModel
import java.time.LocalDate
import javax.inject.Inject

class OfflineDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val caseDao: CaseDao,
    private val deathDao: DeathDao,
    private val dailyRecordDao: DailyRecordDao,
    private val metadataDao: MetadataDao,
    private val caseModelMapper: CaseModelMapper,
    private val dailyRecordModelMapper: DailyRecordModelMapper,
    private val deathModelMapper: DeathModelMapper,
    private val metadataModelMapper: MetadataModelMapper
) {
    suspend fun withTransaction(block: suspend () -> Unit) {
        return appDatabase.withTransaction(block)
    }

    fun deleteAllCases() {
        caseDao.deleteAll()
    }

    fun casesCount(): Int {
        return caseDao.casesCount()
    }

    fun casesMetadata(): MetadataModel? {
        return metadataDao.metadata(CASE_METADATA_ID).map {
            MetadataModel(
                disclaimer = it.disclaimer,
                lastUpdatedAt = it.lastUpdatedAt
            )
        }.firstOrNull()
    }

    fun insertCaseMetadata(metadata: MetadataModel) {
        val metadataEntity = metadataModelMapper.mapToMetadataEntity(CASE_METADATA_ID, metadata)
        metadataDao.insertAll(listOf(metadataEntity))
    }

    fun insertDailyRecord(dailyRecord: DailyRecordModel, date: LocalDate) {
        val dailyRecordsEntity = dailyRecordModelMapper.mapToDailyRecordsEntity(dailyRecord, date)
        dailyRecordDao.insertAll(listOf(dailyRecordsEntity))
    }

    fun insertCases(cases: Collection<CaseModel>) {
        val casesEntityList = cases.map { caseModelMapper.mapToCasesEntity(it) }
        caseDao.insertAll(casesEntityList)
    }

    fun deathsCount(): Int {
        return deathDao.deathsCount()
    }

    fun insertDeathMetadata(metadata: MetadataModel) {
        val metadataEntity = metadataModelMapper.mapToMetadataEntity(DEATH_METADATA_ID, metadata)
        metadataDao.insertAll(listOf(metadataEntity))
    }

    fun deathsMetadata(): MetadataModel? {
        return metadataDao.metadata(DEATH_METADATA_ID).map {
            MetadataModel(
                disclaimer = it.disclaimer,
                lastUpdatedAt = it.lastUpdatedAt
            )
        }.firstOrNull()
    }

    fun insertDeaths(deaths: Collection<DeathModel>) {
        val deathEntityList = deaths.map { deathModelMapper.mapToDeathsEntity(it) }
        deathDao.insertAll(deathEntityList)
    }
}
