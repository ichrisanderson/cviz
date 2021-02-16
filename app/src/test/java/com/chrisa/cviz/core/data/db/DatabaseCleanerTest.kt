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
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Before

class DatabaseCleanerTest {

    private lateinit var db: AppDatabase
    private lateinit var sut: DatabaseCleaner
    private val snapshotProvider: SnapshotProvider = mockk()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        sut = DatabaseCleaner(db, snapshotProvider)
    }

    private fun seedAlertLevels() {
        db.alertLevelDao().insert(
            AlertLevelEntity(
                areaCode = "E001",
                areaName = "Westminister",
                areaType = AreaType.UTLA,
                date = LocalDate.of(2020, 1, 1),
                alertLevel = 2,
                alertLevelName = "Stay Alert",
                alertLevelUrl = "http://acme.com",
                alertLevelValue = 2
            )
        )
        db.alertLevelDao().insert(
            AlertLevelEntity(
                areaCode = "E002",
                areaName = "Camden",
                areaType = AreaType.UTLA,
                date = LocalDate.of(2020, 1, 1),
                alertLevel = 2,
                alertLevelName = "Stay Alert",
                alertLevelUrl = "http://acme.com",
                alertLevelValue = 2
            )
        )
    }

    private fun seedMetadata() {
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.alertLevelId("E001"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 0),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 0)
            )
        )
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.alertLevelId("E002"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 0),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 0)
            )
        )
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.alertLevelId("E003"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 0),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 0)
            )
        )
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.areaCodeId("E001"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 1),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 1)
            )
        )
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.areaCodeId("E002"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 1),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 1)
            )
        )
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.areaCodeId("E003"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 1),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 1)
            )
        )
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.areaSummaryId(),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 0),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 0)
            )
        )
        nations.forEach { nationCode ->
            db.metadataDao().insert(
                MetadataEntity(
                    id = MetaDataIds.areaCodeId(nationCode),
                    lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 1),
                    lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 1)
                )
            )
        }
        db.metadataDao().insert(
            MetadataEntity(
                id = MetaDataIds.areaCodeId("SOA1"),
                lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 1),
                lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 1)
            )
        )
    }

    fun seedAreaLookups() {
        val areaLookupData = AreaLookupEntity(
            postcode = "W1 1AA",
            trimmedPostcode = "W11AA",
            lsoaCode = "E110111",
            lsoaName = "Soho",
            msoaCode = "E11011",
            msoaName = "Soho",
            ltlaCode = "E1101",
            ltlaName = "Westminster",
            utlaCode = "E1101",
            utlaName = "Westminster",
            nhsRegionCode = "E111",
            nhsRegionName = "London11",
            nhsTrustCode = "GUYS",
            nhsTrustName = "St Guys",
            regionCode = "E12000007",
            regionName = "London",
            nationCode = Constants.ENGLAND_AREA_CODE,
            nationName = Constants.ENGLAND_AREA_NAME
        )
    }

    companion object {
        val nations = listOf(
            Constants.ENGLAND_AREA_CODE,
            Constants.NORTHERN_IRELAND_AREA_CODE,
            Constants.SCOTLAND_AREA_CODE,
            Constants.WALES_AREA_CODE
        )
    }
}
