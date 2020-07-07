package com.chrisa.covid19.core.data

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
import java.util.Date
import javax.inject.Inject

class OfflineDataSource @Inject constructor(
    private val caseDao: CaseDao,
    private val deathDao: DeathDao,
    private val dailyRecordDao: DailyRecordDao,
    private val metadataDao: MetadataDao,
    private val caseModelMapper: CaseModelMapper,
    private val dailyRecordModelMapper: DailyRecordModelMapper,
    private val deathModelMapper: DeathModelMapper,
    private val metadataModelMapper: MetadataModelMapper
) {

    fun casesCount(): Int {
        return caseDao.casesCount()
    }

    fun casesMetadata(): MetadataModel? {
        return metadataDao.searchMetadata(CASE_METADATA_ID).map {
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

    fun insertDailyRecord(dailyRecord: DailyRecordModel, date: Date) {
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
        return metadataDao.searchMetadata(DEATH_METADATA_ID).map {
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

