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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chrisa.cviz.core.data.time.TimeProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class DatabaseCleanerTest {

    private lateinit var db: AppDatabase
    private lateinit var sut: DatabaseCleaner
    private val timeProvider: TimeProvider = mockk()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        sut = DatabaseCleaner(db, SnapshotProvider(), timeProvider)
    }

    @Test
    fun `GIVEN soa data is out of date WHEN removeUnusedData called THEN out of date soa data is deleted`() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        every { timeProvider.currentTime() } returns currentTime
        val westminsterData =
            SoaDataEntity(
                areaCode = centralWestminsterArea.areaCode,
                areaName = centralWestminsterArea.areaName,
                areaType = centralWestminsterArea.areaType,
                change = -4,
                changePercentage = -17.6,
                date = LocalDate.of(2021, 2, 14),
                rollingRate = 55.9,
                rollingSum = 9
            )
        val soaData = listOf(
            westminsterData,
            westminsterData.copy(
                areaCode = marlyboneArea.areaCode,
                areaName = marlyboneArea.areaName
            ),
            westminsterData.copy(
                areaCode = oxfordCentralArea.areaCode,
                areaName = oxfordCentralArea.areaName
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetaDataIds.areaCodeId(centralWestminsterArea.areaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.soaDataDao().insertAll(soaData)
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetaDataIds.areaCodeId(marlyboneArea.areaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetaDataIds.areaCodeId(oxfordCentralArea.areaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedSoaAreaCodes).isEqualTo(
            listOf(
                centralWestminsterArea.areaCode,
                oxfordCentralArea.areaCode
            )
        )
        assertThat(testSnapshot.retainedMetadataIds).isEqualTo(
            listOf(
                MetaDataIds.areaCodeId(centralWestminsterArea.areaCode),
                MetaDataIds.areaCodeId(oxfordCentralArea.areaCode)
            )
        )
    }

    @Test
    fun `GIVEN area data is out of date WHEN removeUnusedData called THEN out of date area data is deleted`() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        every { timeProvider.currentTime() } returns currentTime
        val westminsterData = AreaDataEntity(
            areaCode = centralWestminsterArea.areaCode,
            areaName = centralWestminsterArea.areaName,
            areaType = centralWestminsterArea.areaType,
            metadataId = MetaDataIds.areaCodeId(centralWestminsterArea.areaCode),
            date = LocalDate.of(2021, 2, 12),
            cumulativeCases = 222,
            infectionRate = 122.0,
            newCases = 122,
            newDeathsByPublishedDate = 15,
            cumulativeDeathsByPublishedDate = 20,
            cumulativeDeathsByPublishedDateRate = 30.0,
            newDeathsByDeathDate = 40,
            cumulativeDeathsByDeathDate = 50,
            cumulativeDeathsByDeathDateRate = 60.0,
            newOnsDeathsByRegistrationDate = 10,
            cumulativeOnsDeathsByRegistrationDate = 53,
            cumulativeOnsDeathsByRegistrationDateRate = 62.0
        )
        val areaData = listOf(
            westminsterData,
            westminsterData.copy(
                areaCode = marlyboneArea.areaCode,
                areaName = marlyboneArea.areaName,
                metadataId = MetaDataIds.areaCodeId(marlyboneArea.areaCode)
            ),
            westminsterData.copy(
                areaCode = oxfordCentralArea.areaCode,
                areaName = oxfordCentralArea.areaName,
                metadataId = MetaDataIds.areaCodeId(oxfordCentralArea.areaCode)
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetaDataIds.areaCodeId(centralWestminsterArea.areaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.areaDataDao().insertAll(areaData)
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetaDataIds.areaCodeId(marlyboneArea.areaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetaDataIds.areaCodeId(oxfordCentralArea.areaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedAreaDataAreaCodes).isEqualTo(
            listOf(
                centralWestminsterArea.areaCode,
                oxfordCentralArea.areaCode
            )
        )
        assertThat(testSnapshot.retainedMetadataIds).isEqualTo(
            listOf(
                MetaDataIds.areaCodeId(centralWestminsterArea.areaCode),
                MetaDataIds.areaCodeId(oxfordCentralArea.areaCode)
            )
        )
    }

    @Test
    fun `GIVEN alert level data is out of date WHEN removeUnusedData called THEN out of date alert level data is deleted`() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        every { timeProvider.currentTime() } returns currentTime
        val westminsterAlertLevel = AlertLevelEntity(
            areaCode = centralWestminsterArea.areaCode,
            areaName = centralWestminsterArea.areaName,
            areaType = centralWestminsterArea.areaType,
            date = LocalDate.of(2021, 2, 14),
            alertLevel = 2,
            alertLevelName = "Stay Alert",
            alertLevelUrl = "http://acme.com",
            alertLevelValue = 2
        )
        val alertLevels = listOf(
            westminsterAlertLevel,
            westminsterAlertLevel.copy(
                areaCode = marlyboneArea.areaCode,
                areaName = marlyboneArea.areaName
            ),
            westminsterAlertLevel.copy(
                areaCode = oxfordCentralArea.areaCode,
                areaName = oxfordCentralArea.areaName
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetaDataIds.alertLevelId(centralWestminsterArea.areaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.alertLevelDao().insertAll(alertLevels)
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetaDataIds.alertLevelId(marlyboneArea.areaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetaDataIds.alertLevelId(oxfordCentralArea.areaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedAlertLevelAreaCodes).isEqualTo(
            listOf(
                centralWestminsterArea.areaCode,
                oxfordCentralArea.areaCode
            )
        )
        assertThat(testSnapshot.retainedMetadataIds).isEqualTo(
            listOf(
                MetaDataIds.alertLevelId(centralWestminsterArea.areaCode),
                MetaDataIds.alertLevelId(oxfordCentralArea.areaCode)
            )
        )
    }

    @Test
    fun `GIVEN healthcare data is out of date WHEN removeUnusedData called THEN out of date healthcare data is deleted`() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        every { timeProvider.currentTime() } returns currentTime
        val westminsterHealthcareEntity = HealthcareEntity(
            areaCode = centralWestminsterArea.areaCode,
            areaName = centralWestminsterArea.areaName,
            areaType = centralWestminsterArea.areaType,
            date = LocalDate.of(2021, 2, 11),
            newAdmissions = 10,
            cumulativeAdmissions = 100,
            occupiedBeds = 70,
            transmissionRateMin = 0.8,
            transmissionRateMax = 1.1,
            transmissionRateGrowthRateMin = 0.7,
            transmissionRateGrowthRateMax = 1.2
        )
        val healthcare = listOf(
            westminsterHealthcareEntity,
            westminsterHealthcareEntity.copy(
                areaCode = marlyboneArea.areaCode,
                areaName = marlyboneArea.areaName
            ),
            westminsterHealthcareEntity.copy(
                areaCode = oxfordCentralArea.areaCode,
                areaName = oxfordCentralArea.areaName
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetaDataIds.healthcareId(centralWestminsterArea.areaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.healthcareDao().insertAll(healthcare)
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetaDataIds.healthcareId(marlyboneArea.areaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetaDataIds.healthcareId(oxfordCentralArea.areaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedHealthcareAreaCodes).isEqualTo(
            listOf(
                centralWestminsterArea.areaCode,
                oxfordCentralArea.areaCode
            )
        )
        assertThat(testSnapshot.retainedMetadataIds).isEqualTo(
            listOf(
                MetaDataIds.healthcareId(centralWestminsterArea.areaCode),
                MetaDataIds.healthcareId(oxfordCentralArea.areaCode)
            )
        )
    }

    class TestSnapshot(db: AppDatabase) {
        val retainedSoaAreaCodes = db.soaDataDao().all().map { it.areaCode }
        val retainedAreaDataAreaCodes = db.areaDataDao().all().map { it.areaCode }
        val retainedHealthcareAreaCodes = db.healthcareDao().all().map { it.areaCode }
        val retainedAlertLevelAreaCodes = db.alertLevelDao().all().map { it.areaCode }
        val retainedMetadataIds = db.metadataDao().all().map { it.id }
    }

    companion object {
        val marlyboneArea = AreaEntity(
            areaCode = "E02000970",
            areaName = "Marylebone & Park Lane",
            areaType = AreaType.MSOA
        )
        val centralWestminsterArea = AreaEntity(
            areaCode = "E02000979",
            areaName = "Central Westminster",
            areaType = AreaType.MSOA
        )
        val oxfordCentralArea = AreaEntity(
            areaCode = "E02005947",
            areaName = "Oxford Central",
            areaType = AreaType.MSOA
        )
    }
}
