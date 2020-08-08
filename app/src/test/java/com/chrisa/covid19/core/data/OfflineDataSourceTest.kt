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
import com.chrisa.covid19.core.data.db.AreaDao
import com.chrisa.covid19.core.data.db.AreaEntity
import com.chrisa.covid19.core.data.db.CaseDao
import com.chrisa.covid19.core.data.db.DailyRecordDao
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.MetadataModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class OfflineDataSourceTest {

    private lateinit var offlineDataSource: OfflineDataSource

    private val appDatabase = mockk<AppDatabase>(relaxed = true)
    private val casesDao = mockk<CaseDao>(relaxed = true)
    private val areaDao = mockk<AreaDao>(relaxed = true)
    private val dailyRecordsDao = mockk<DailyRecordDao>(relaxed = true)
    private val metadataDao = mockk<MetadataDao>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {

        every { appDatabase.areaDao() } returns areaDao
        every { appDatabase.metadataDao() } returns metadataDao

        offlineDataSource = OfflineDataSource(
            appDatabase
        )
    }

    @Test
    fun `WHEN areaCount called THEN areaDao is queried for total`() {
        every { areaDao.count() } returns 100

        val cases = offlineDataSource.areaCount()

        assertThat(cases).isEqualTo(100)
    }

    @Test
    fun `GIVEN no area metadata exists WHEN areaMetadata called THEN area metadata is null`() {
        every { metadataDao.metadata(MetadataEntity.AREA_METADATA_ID) } returns null

        val metadata = offlineDataSource.areaMetadata()
        assertThat(metadata).isNull()
    }

    @Test
    fun `GIVEN area metadata exists WHEN areaMetadata called THEN area metadata is returned`() {
        val areaMetadataEntity = MetadataEntity(id = MetadataEntity.AREA_METADATA_ID, lastUpdatedAt = LocalDateTime.now())
        every { metadataDao.metadata(MetadataEntity.AREA_METADATA_ID) } returns areaMetadataEntity

        val metadata = offlineDataSource.areaMetadata()
        assertThat(metadata).isEqualTo(MetadataModel(lastUpdatedAt = areaMetadataEntity.lastUpdatedAt))
    }

    @Test
    fun `WHEN insertAreaMetadata called THEN metadataEntity is inserted`() {

        val metadataModel = MetadataModel(
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        offlineDataSource.insertAreaMetadata(metadataModel)

        verify(exactly = 1) {
            metadataDao.insertAll(
                listOf(
                    MetadataEntity(
                        id = MetadataEntity.AREA_METADATA_ID,
                        lastUpdatedAt = metadataModel.lastUpdatedAt
                    )
                )
            )
        }
    }
    @Test
    fun `WHEN insertAreas called THEN areaEntity is inserted`() {

        val areaModel = AreaModel(
            areaCode = "001",
            areaName = "UK",
            areaType = "overview"
        )

        offlineDataSource.insertAreas(listOf(areaModel))

        verify(exactly = 1) {
            areaDao.insertAll(
                listOf(
                    AreaEntity(
                        areaCode = areaModel.areaCode,
                        areaName = areaModel.areaName,
                        areaType = areaModel.areaType
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN withTransaction called THEN function runs in transaction`() =
        testDispatcher.runBlockingTest {
            mockkStatic("androidx.room.RoomDatabaseKt")
            val block: suspend () -> Unit = { }
            coEvery { appDatabase.withTransaction(block) } just Runs
            offlineDataSource.withTransaction(block)
            coVerify { appDatabase.withTransaction(block) }
        }
//
//    @Test
//    fun `WHEN deleteAllCases called THEN casesDao deletes all records`() {
//        offlineDataSource.deleteAllCases()
//        verify { casesDao.deleteAll() }
//    }
//
//    @Test
//    fun `WHEN insertCaseMetadata called THEN metadataEntity is inserted`() {
//
//        val metadataModel = MetadataModel(
//            disclaimer = "New metadata",
//            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
//        )
//
//        offlineDataSource.insertCaseMetadata(metadataModel)
//
//        verify(exactly = 1) {
//            metadataDao.insertAll(
//                listOf(
//                    MetadataEntity(
//                        id = CASE_METADATA_ID,
//                        disclaimer = metadataModel.disclaimer,
//                        lastUpdatedAt = metadataModel.lastUpdatedAt
//                    )
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `WHEN insertDailyRecord called THEN dailyRecordsEntity is inserted`() {
//
//        val lastUpdatedAt = LocalDate.ofEpochDay(0)
//        val dailyRecordModel = DailyRecordModel(
//            areaName = "UK",
//            dailyLabConfirmedCases = 12,
//            totalLabConfirmedCases = 33
//        )
//
//        offlineDataSource.insertDailyRecord(dailyRecordModel, lastUpdatedAt)
//
//        verify(exactly = 1) {
//            dailyRecordsDao.insertAll(
//                listOf(
//                    DailyRecordEntity(
//                        areaName = dailyRecordModel.areaName,
//                        date = lastUpdatedAt,
//                        dailyLabConfirmedCases = dailyRecordModel.dailyLabConfirmedCases,
//                        totalLabConfirmedCases = dailyRecordModel.totalLabConfirmedCases!!
//                    )
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `GIVEN totalLabConfirmedCases is null WHEN insertDailyRecord called THEN dailyRecordsEntity is inserted with 0 totalLabConfirmedCases`() {
//
//        val lastUpdatedAt = LocalDate.ofEpochDay(0)
//        val dailyRecordModel = DailyRecordModel(
//            areaName = "UK",
//            dailyLabConfirmedCases = 12,
//            totalLabConfirmedCases = null
//        )
//
//        offlineDataSource.insertDailyRecord(dailyRecordModel, lastUpdatedAt)
//
//        verify(exactly = 1) {
//            dailyRecordsDao.insertAll(
//                listOf(
//                    DailyRecordEntity(
//                        areaName = dailyRecordModel.areaName,
//                        date = lastUpdatedAt,
//                        dailyLabConfirmedCases = dailyRecordModel.dailyLabConfirmedCases,
//                        totalLabConfirmedCases = 0
//                    )
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `WHEN casesMetadata called THEN metadata is searched for CASE_METADATA_ID`() {
//
//        val testMetadataEntity = MetadataEntity(
//            id = CASE_METADATA_ID,
//            disclaimer = "Test case metadata disclaimer",
//            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
//        )
//
//        every { metadataDao.metadata(CASE_METADATA_ID) } returns listOf(testMetadataEntity)
//
//        val metadata = offlineDataSource.casesMetadata()
//
//        assertThat(metadata).isEqualTo(
//            MetadataModel(
//                disclaimer = testMetadataEntity.disclaimer,
//                lastUpdatedAt = testMetadataEntity.lastUpdatedAt
//            )
//        )
//    }
//
//    @Test
//    fun `WHEN casesCount called THEN casesCount is queried`() {
//        every { casesDao.casesCount() } returns 100
//
//        val cases = offlineDataSource.casesCount()
//
//        assertThat(cases).isEqualTo(100)
//    }
//
//    @Test
//    fun `WHEN insertCases called THEN casesEntity is inserted`() {
//
//        val caseModel = CaseModel(
//            areaCode = "001",
//            areaName = "UK",
//            specimenDate = LocalDate.ofEpochDay(0),
//            dailyLabConfirmedCases = 12,
//            totalLabConfirmedCases = 33,
//            dailyTotalLabConfirmedCasesRate = 100.0,
//            changeInDailyCases = 11,
//            changeInTotalCases = 22,
//            previouslyReportedTotalCases = 33,
//            previouslyReportedDailyCases = 44
//        )
//
//        offlineDataSource.insertCases(listOf(caseModel))
//
//        verify(exactly = 1) {
//            casesDao.insertAll(
//                listOf(
//                    CaseEntity(
//                        areaCode = caseModel.areaCode,
//                        areaName = caseModel.areaName,
//                        date = caseModel.specimenDate,
//                        dailyLabConfirmedCases = caseModel.dailyLabConfirmedCases!!,
//                        dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate!!,
//                        totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
//                    )
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `GIVEN dailyLabConfirmedCases is null WHEN insertCases called THEN casesEntity is inserted with 0 dailyLabConfirmedCases`() {
//
//        val caseModel = CaseModel(
//            areaCode = "001",
//            areaName = "UK",
//            specimenDate = LocalDate.ofEpochDay(0),
//            dailyLabConfirmedCases = null,
//            totalLabConfirmedCases = 33,
//            dailyTotalLabConfirmedCasesRate = 100.0,
//            changeInDailyCases = 0,
//            changeInTotalCases = 0,
//            previouslyReportedTotalCases = 0,
//            previouslyReportedDailyCases = 0
//        )
//
//        offlineDataSource.insertCases(listOf(caseModel))
//
//        verify(exactly = 1) {
//            casesDao.insertAll(
//                listOf(
//                    CaseEntity(
//                        areaCode = caseModel.areaCode,
//                        areaName = caseModel.areaName,
//                        date = caseModel.specimenDate,
//                        dailyLabConfirmedCases = 0,
//                        dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate!!,
//                        totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
//                    )
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `GIVEN dailyTotalLabConfirmedCasesRate is null WHEN insertCases called THEN casesEntity is inserted with 0 dailyTotalLabConfirmedCasesRate`() {
//
//        val caseModel = CaseModel(
//            areaCode = "001",
//            areaName = "UK",
//            specimenDate = LocalDate.ofEpochDay(0),
//            dailyLabConfirmedCases = null,
//            totalLabConfirmedCases = 33,
//            dailyTotalLabConfirmedCasesRate = null,
//            changeInDailyCases = 0,
//            changeInTotalCases = 0,
//            previouslyReportedTotalCases = 0,
//            previouslyReportedDailyCases = 0
//        )
//
//        offlineDataSource.insertCases(listOf(caseModel))
//
//        verify(exactly = 1) {
//            casesDao.insertAll(
//                listOf(
//                    CaseEntity(
//                        areaCode = caseModel.areaCode,
//                        areaName = caseModel.areaName,
//                        date = caseModel.specimenDate,
//                        dailyLabConfirmedCases = 0,
//                        dailyTotalLabConfirmedCasesRate = 0.0,
//                        totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
//                    )
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `GIVEN totalLabConfirmedCases is null WHEN insertCases called THEN casesEntity is inserted with 0 totalLabConfirmedCases`() {
//
//        val caseModel = CaseModel(
//            areaCode = "001",
//            areaName = "UK",
//            specimenDate = LocalDate.ofEpochDay(0),
//            dailyLabConfirmedCases = null,
//            totalLabConfirmedCases = null,
//            dailyTotalLabConfirmedCasesRate = null,
//            changeInDailyCases = 0,
//            changeInTotalCases = 0,
//            previouslyReportedTotalCases = 0,
//            previouslyReportedDailyCases = 0
//        )
//
//        offlineDataSource.insertCases(listOf(caseModel))
//
//        verify(exactly = 1) {
//            casesDao.insertAll(
//                listOf(
//                    CaseEntity(
//                        areaCode = caseModel.areaCode,
//                        areaName = caseModel.areaName,
//                        date = caseModel.specimenDate,
//                        dailyLabConfirmedCases = 0,
//                        dailyTotalLabConfirmedCasesRate = 0.0,
//                        totalLabConfirmedCases = 0
//                    )
//                )
//            )
//        }
//    }
}
