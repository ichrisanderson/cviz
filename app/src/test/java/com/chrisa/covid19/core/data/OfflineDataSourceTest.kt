package com.chrisa.covid19.core.data

import com.chrisa.covid19.core.data.db.*
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
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.*

class OfflineDataSourceTest {

    private lateinit var offlineDataSource: OfflineDataSource

    private val casesDao = mockk<CaseDao>(relaxed = true)
    private val deathsDao = mockk<DeathDao>(relaxed = true)
    private val dailyRecordsDao = mockk<DailyRecordDao>(relaxed = true)
    private val metadataDao = mockk<MetadataDao>(relaxed = true)
    private val caseModelMapper = CaseModelMapper()
    private val deathsModelMapper = DeathModelMapper()
    private val dailyRecordModelMapper = DailyRecordModelMapper()
    private val metadataModelMapper = MetadataModelMapper()

    @Before
    fun setup() {

        every { metadataDao.searchMetadata(CASE_METADATA_ID) } returns emptyList()
        every { metadataDao.searchMetadata(DEATH_METADATA_ID) } returns emptyList()

        offlineDataSource = OfflineDataSource(
            casesDao,
            deathsDao,
            dailyRecordsDao,
            metadataDao,
            caseModelMapper,
            dailyRecordModelMapper,
            deathsModelMapper,
            metadataModelMapper
        )
    }

    @Test
    fun `WHEN insertCaseMetadata called THEN metadataEntity is inserted`() {

        val metadataModel = MetadataModel(
            disclaimer = "New metadata",
            lastUpdatedAt = Date(1)
        )

        offlineDataSource.insertCaseMetadata(metadataModel)

        verify(exactly = 1) {
            metadataDao.insertAll(
                listOf(
                    MetadataEntity(
                        id = CASE_METADATA_ID,
                        disclaimer = metadataModel.disclaimer,
                        lastUpdatedAt = metadataModel.lastUpdatedAt
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN insertDailyRecord called THEN dailyRecordsEntity is inserted`() {

        val lastUpdatedAt = Date(1)
        val dailyRecordModel = DailyRecordModel(
            areaName = "UK",
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33
        )

        offlineDataSource.insertDailyRecord(dailyRecordModel, lastUpdatedAt)

        verify(exactly = 1) {
            dailyRecordsDao.insertAll(
                listOf(
                    DailyRecordEntity(
                        areaName = dailyRecordModel.areaName,
                        date = lastUpdatedAt,
                        dailyLabConfirmedCases = dailyRecordModel.dailyLabConfirmedCases,
                        totalLabConfirmedCases = dailyRecordModel.totalLabConfirmedCases!!
                    )
                )
            )
        }
    }

    @Test
    fun `GIVEN totalLabConfirmedCases is null WHEN insertDailyRecord called THEN dailyRecordsEntity is inserted with 0 totalLabConfirmedCases`() {

        val lastUpdatedAt = Date(1)
        val dailyRecordModel = DailyRecordModel(
            areaName = "UK",
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = null
        )

        offlineDataSource.insertDailyRecord(dailyRecordModel, lastUpdatedAt)

        verify(exactly = 1) {
            dailyRecordsDao.insertAll(
                listOf(
                    DailyRecordEntity(
                        areaName = dailyRecordModel.areaName,
                        date = lastUpdatedAt,
                        dailyLabConfirmedCases = dailyRecordModel.dailyLabConfirmedCases,
                        totalLabConfirmedCases = 0
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN casesMetadata called THEN metadata is searched for CASE_METADATA_ID`() {

        val testMetadataEntity = MetadataEntity(
            id = CASE_METADATA_ID,
            disclaimer = "Test case metadata disclaimer",
            lastUpdatedAt = Date(1)
        )

        every { metadataDao.searchMetadata(CASE_METADATA_ID) } returns listOf(testMetadataEntity)

        val metadata = offlineDataSource.casesMetadata()

        assertThat(metadata).isEqualTo(
            MetadataModel(
                disclaimer = testMetadataEntity.disclaimer,
                lastUpdatedAt = testMetadataEntity.lastUpdatedAt
            )
        )
    }

    @Test
    fun `WHEN casesCount called THEN casesCount is queried`() {
        every { casesDao.casesCount() } returns 100

        val cases = offlineDataSource.casesCount()

        assertThat(cases).isEqualTo(100)
    }

    @Test
    fun `WHEN insertCases called THEN casesEntity is inserted`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0,
            changeInDailyCases = 11,
            changeInTotalCases = 22,
            previouslyReportedTotalCases = 33,
            previouslyReportedDailyCases = 44
        )

        offlineDataSource.insertCases(listOf(caseModel))

        verify(exactly = 1) {
            casesDao.insertAll(
                listOf(
                    CaseEntity(
                        areaCode = caseModel.areaCode,
                        areaName = caseModel.areaName,
                        date = caseModel.specimenDate,
                        dailyLabConfirmedCases = caseModel.dailyLabConfirmedCases!!,
                        dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate!!,
                        totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
                    )
                )
            )
        }
    }

    @Test
    fun `GIVEN dailyLabConfirmedCases is null WHEN insertCases called THEN casesEntity is inserted with 0 dailyLabConfirmedCases`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = null,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0,
            changeInDailyCases = 0,
            changeInTotalCases = 0,
            previouslyReportedTotalCases = 0,
            previouslyReportedDailyCases = 0
        )

        offlineDataSource.insertCases(listOf(caseModel))

        verify(exactly = 1) {
            casesDao.insertAll(
                listOf(
                    CaseEntity(
                        areaCode = caseModel.areaCode,
                        areaName = caseModel.areaName,
                        date = caseModel.specimenDate,
                        dailyLabConfirmedCases = 0,
                        dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate!!,
                        totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
                    )
                )
            )
        }
    }

    @Test
    fun `GIVEN dailyTotalLabConfirmedCasesRate is null WHEN insertCases called THEN casesEntity is inserted with 0 dailyTotalLabConfirmedCasesRate`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = null,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = null,
            changeInDailyCases = 0,
            changeInTotalCases = 0,
            previouslyReportedTotalCases = 0,
            previouslyReportedDailyCases = 0
        )

        offlineDataSource.insertCases(listOf(caseModel))

        verify(exactly = 1) {
            casesDao.insertAll(
                listOf(
                    CaseEntity(
                        areaCode = caseModel.areaCode,
                        areaName = caseModel.areaName,
                        date = caseModel.specimenDate,
                        dailyLabConfirmedCases = 0,
                        dailyTotalLabConfirmedCasesRate = 0.0,
                        totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN insertDeathMetadata called THEN metadataEntity is inserted`() {

        val metadataModel = MetadataModel(
            disclaimer = "New metadata",
            lastUpdatedAt = Date(1)
        )

        offlineDataSource.insertDeathMetadata(metadataModel)

        verify(exactly = 1) {
            metadataDao.insertAll(
                listOf(
                    MetadataEntity(
                        id = DEATH_METADATA_ID,
                        disclaimer = metadataModel.disclaimer,
                        lastUpdatedAt = metadataModel.lastUpdatedAt
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN deathsMetadata called THEN metadata is searched for DEATH_METADATA_ID`() {

        val testMetadataEntity = MetadataEntity(
            id = DEATH_METADATA_ID,
            disclaimer = "Test case metadata disclaimer",
            lastUpdatedAt = Date(1)
        )

        every { metadataDao.searchMetadata(DEATH_METADATA_ID) } returns listOf(testMetadataEntity)

        val metadata = offlineDataSource.deathsMetadata()

        assertThat(metadata).isEqualTo(
            MetadataModel(
                disclaimer = testMetadataEntity.disclaimer,
                lastUpdatedAt = testMetadataEntity.lastUpdatedAt
            )
        )
    }

    @Test
    fun `WHEN deathsCount called THEN deathsCount is queried`() {
        every { deathsDao.deathsCount() } returns 1000

        val deathsCount = offlineDataSource.deathsCount()

        assertThat(deathsCount).isEqualTo(1000)
    }

    @Test
    fun `WHEN insertDeaths called THEN deathsEntity is inserted`() {

        val deathModel = DeathModel(
            areaCode = "001",
            areaName = "UK",
            reportingDate = Date(0),
            cumulativeDeaths = 110,
            dailyChangeInDeaths = 222
        )

        offlineDataSource.insertDeaths(listOf(deathModel))

        verify(exactly = 1) {
            deathsDao.insertAll(
                listOf(
                    DeathEntity(
                        areaCode = deathModel.areaCode,
                        areaName = deathModel.areaName,
                        date = deathModel.reportingDate,
                        cumulativeDeaths = deathModel.cumulativeDeaths,
                        dailyChangeInDeaths = deathModel.dailyChangeInDeaths!!
                    )
                )
            )
        }
    }

    @Test
    fun `GIVEN dailyChangeInDeaths is null WHEN insertDeaths called THEN deathsEntity is inserted with 0 dailyChangeInDeaths`() {

        val deathModel = DeathModel(
            areaCode = "001",
            areaName = "UK",
            reportingDate = Date(0),
            cumulativeDeaths = 110,
            dailyChangeInDeaths = null
        )

        offlineDataSource.insertDeaths(listOf(deathModel))

        verify(exactly = 1) {
            deathsDao.insertAll(
                listOf(
                    DeathEntity(
                        areaCode = deathModel.areaCode,
                        areaName = deathModel.areaName,
                        date = deathModel.reportingDate,
                        cumulativeDeaths = deathModel.cumulativeDeaths,
                        dailyChangeInDeaths = 0
                    )
                )
            )
        }
    }
}


