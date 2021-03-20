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
import com.google.common.truth.Truth.assertThat
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
class UnusedDataCleanerTest {

    private lateinit var db: AppDatabase
    private lateinit var sut: UnusedDataCleaner

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        seedAreas()
        seedSoaData()
        seedAreaData()
        seedAlertLevels()
        seedHealthcare()
        seedAreaLookups()

        sut = UnusedDataCleaner(db)
    }

    private fun seedAreas() {
        db.areaDao().insertAll(
            listOf(
                AreaEntity(
                    areaCode = centralWestminsterAreaLookup.msoaCode,
                    areaName = centralWestminsterAreaLookup.msoaName!!,
                    areaType = AreaType.MSOA
                ),
                AreaEntity(
                    areaCode = centralWestminsterAreaLookup.utlaCode,
                    areaName = centralWestminsterAreaLookup.utlaName,
                    areaType = AreaType.UTLA
                ),
                AreaEntity(
                    areaCode = centralWestminsterAreaLookup.regionCode!!,
                    areaName = centralWestminsterAreaLookup.regionName!!,
                    areaType = AreaType.REGION
                ),
                AreaEntity(
                    areaCode = centralWestminsterAreaLookup.nhsTrustCode!!,
                    areaName = centralWestminsterAreaLookup.nhsTrustName!!,
                    areaType = AreaType.NHS_TRUST
                ),
                AreaEntity(
                    areaCode = centralWestminsterAreaLookup.nhsRegionCode!!,
                    areaName = centralWestminsterAreaLookup.nhsRegionName!!,
                    areaType = AreaType.NHS_REGION
                ),
                AreaEntity(
                    areaCode = marlyboneAreaLookup.msoaCode,
                    areaName = marlyboneAreaLookup.msoaName!!,
                    areaType = AreaType.MSOA
                ),
                AreaEntity(
                    areaCode = marlyboneAreaLookup.nhsTrustCode!!,
                    areaName = marlyboneAreaLookup.nhsTrustName!!,
                    areaType = AreaType.NHS_TRUST
                ),
                AreaEntity(
                    areaCode = oxfordCentralAreaLookup.msoaCode,
                    areaName = oxfordCentralAreaLookup.msoaName!!,
                    areaType = AreaType.MSOA
                ),
                AreaEntity(
                    areaCode = oxfordCentralAreaLookup.utlaCode,
                    areaName = oxfordCentralAreaLookup.utlaName,
                    areaType = AreaType.UTLA
                ),
                AreaEntity(
                    areaCode = oxfordCentralAreaLookup.regionCode!!,
                    areaName = oxfordCentralAreaLookup.regionName!!,
                    areaType = AreaType.REGION
                ),
                AreaEntity(
                    areaCode = oxfordCentralAreaLookup.nhsTrustCode!!,
                    areaName = oxfordCentralAreaLookup.nhsTrustName!!,
                    areaType = AreaType.NHS_TRUST
                ),
                AreaEntity(
                    areaCode = oxfordCentralAreaLookup.nhsRegionCode!!,
                    areaName = oxfordCentralAreaLookup.nhsRegionName!!,
                    areaType = AreaType.NHS_REGION
                ),
                AreaEntity(
                    areaCode = aberdeenCityAreaLookup.utlaCode,
                    areaName = aberdeenCityAreaLookup.utlaName,
                    areaType = AreaType.UTLA
                )
            ).plus(nations)
        )
    }

    private fun seedSoaData() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        val westminsterData =
            SoaDataEntity(
                areaCode = centralWestminsterAreaLookup.msoaCode,
                metadataId = MetadataIds.areaCodeId(centralWestminsterAreaLookup.msoaCode),
                change = -4,
                changePercentage = -17.6,
                date = LocalDate.of(2021, 2, 14),
                rollingRate = 55.9,
                rollingSum = 9
            )
        val soaData = listOf(
            westminsterData,
            westminsterData.copy(
                areaCode = marlyboneAreaLookup.msoaCode,
                metadataId = MetadataIds.areaCodeId(marlyboneAreaLookup.msoaCode)
            ),
            westminsterData.copy(
                areaCode = oxfordCentralAreaLookup.msoaCode,
                metadataId = MetadataIds.areaCodeId(oxfordCentralAreaLookup.msoaCode)
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetadataIds.areaCodeId(centralWestminsterAreaLookup.msoaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetadataIds.areaCodeId(marlyboneAreaLookup.msoaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetadataIds.areaCodeId(oxfordCentralAreaLookup.msoaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )
        db.soaDataDao().insertAll(soaData)
        assertThat(db.soaDataDao().all()).isNotEmpty()
    }

    private fun seedAreaData() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        val westminsterData = AreaDataEntity(
            areaCode = centralWestminsterAreaLookup.utlaCode,
            metadataId = MetadataIds.areaCodeId(centralWestminsterAreaLookup.utlaCode),
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
        val westminsterMetadata = MetadataEntity(
            id = MetadataIds.areaCodeId(centralWestminsterAreaLookup.utlaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetadataIds.areaCodeId(oxfordCentralAreaLookup.utlaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )
        val areaData = listOf(
            westminsterData,
            westminsterData.copy(
                areaCode = oxfordCentralAreaLookup.utlaCode,
                metadataId = MetadataIds.areaCodeId(oxfordCentralAreaLookup.utlaCode)
            )
        )
        db.areaDataDao().insertAll(areaData)
    }

    private fun seedAlertLevels() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        val westminsterAlertLevel = AlertLevelEntity(
            areaCode = centralWestminsterAreaLookup.utlaCode,
            metadataId = MetadataIds.alertLevelId(centralWestminsterAreaLookup.utlaCode),
            date = LocalDate.of(2021, 2, 14),
            alertLevel = 2,
            alertLevelName = "Stay Alert",
            alertLevelUrl = "http://acme.com",
            alertLevelValue = 2
        )
        val westminsterMetadata = MetadataEntity(
            id = MetadataIds.alertLevelId(centralWestminsterAreaLookup.utlaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetadataIds.alertLevelId(marlyboneAreaLookup.utlaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetadataIds.alertLevelId(oxfordCentralAreaLookup.utlaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )
        val alertLevels = listOf(
            westminsterAlertLevel,
            westminsterAlertLevel.copy(
                areaCode = marlyboneAreaLookup.utlaCode,
                metadataId = MetadataIds.alertLevelId(marlyboneAreaLookup.utlaCode)
            ),
            westminsterAlertLevel.copy(
                areaCode = oxfordCentralAreaLookup.utlaCode,
                metadataId = MetadataIds.alertLevelId(oxfordCentralAreaLookup.utlaCode)
            )
        )
        db.alertLevelDao().insertAll(alertLevels)
    }

    private fun seedHealthcare() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        val westminsterHealthcareEntity = HealthcareEntity(
            areaCode = centralWestminsterAreaLookup.nhsTrustCode!!,
            metadataId = MetadataIds.healthcareId(centralWestminsterAreaLookup.nhsTrustCode!!),
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
                areaCode = marlyboneAreaLookup.nhsTrustCode!!,
                metadataId = MetadataIds.healthcareId(marlyboneAreaLookup.nhsTrustCode!!)
            ),
            westminsterHealthcareEntity.copy(
                areaCode = oxfordCentralAreaLookup.nhsTrustCode!!,
                metadataId = MetadataIds.healthcareId(oxfordCentralAreaLookup.nhsTrustCode!!)
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetadataIds.healthcareId(centralWestminsterAreaLookup.nhsTrustCode!!),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetadataIds.healthcareId(marlyboneAreaLookup.nhsTrustCode!!),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetadataIds.healthcareId(oxfordCentralAreaLookup.nhsTrustCode!!),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )
        db.healthcareDao().insertAll(healthcare)
    }

    private fun seedAreaLookups() {
        db.areaLookupDao().insertAll(
            listOf(
                marlyboneAreaLookup,
                centralWestminsterAreaLookup,
                oxfordCentralAreaLookup,
                aberdeenCityAreaLookup
            )
        )
    }

    @Test
    fun `GIVEN no saved areas WHEN removeUnusedData called THEN all data cleared`() {
        runBlocking { sut.execute() }

        assertThat(db.soaDataDao().all()).isEmpty()
        assertThat(db.areaDataDao().all()).isEmpty()
        assertThat(db.alertLevelDao().all()).isEmpty()
        assertThat(db.healthcareDao().all()).isEmpty()
        assertThat(db.metadataDao().all()).isEmpty()
        assertThat(db.areaLookupDao().all()).isEmpty()
    }

    @Test
    fun `GIVEN saved area with no associations WHEN removeUnusedData called THEN saved area data not cleared`() {
        db.savedAreaDao().insert(SavedAreaEntity(centralWestminsterAreaLookup.msoaCode))

        runBlocking { sut.execute() }

        assertThat(db.soaDataDao().all().map { it.areaCode })
            .isEqualTo(listOf(centralWestminsterAreaLookup.msoaCode))
        assertThat(db.areaDataDao().all()).isEmpty()
        assertThat(db.alertLevelDao().all()).isEmpty()
        assertThat(db.healthcareDao().all()).isEmpty()
        assertThat(db.metadataDao().all().map { it.id })
            .isEqualTo(listOf(MetadataIds.areaCodeId(centralWestminsterAreaLookup.msoaCode)))
        assertThat(db.areaLookupDao().all()).isEmpty()
    }

    @Test
    fun `GIVEN area saved with association data WHEN removeUnusedData called THEN area and association data not cleared`() {
        db.savedAreaDao().insert(SavedAreaEntity(centralWestminsterAreaLookup.msoaCode))
        db.areaAssociationDao().insert(
            AreaAssociation(
                centralWestminsterAreaLookup.msoaCode,
                centralWestminsterAreaLookup.lsoaCode,
                AreaAssociationType.AREA_LOOKUP
            )
        )
        db.areaAssociationDao().insert(
            AreaAssociation(
                centralWestminsterAreaLookup.msoaCode,
                centralWestminsterAreaLookup.utlaCode,
                AreaAssociationType.AREA_DATA
            )
        )
        db.areaAssociationDao().insert(
            AreaAssociation(
                centralWestminsterAreaLookup.msoaCode,
                centralWestminsterAreaLookup.utlaCode,
                AreaAssociationType.ALERT_LEVEL
            )
        )
        db.areaAssociationDao().insert(
            AreaAssociation(
                centralWestminsterAreaLookup.msoaCode,
                centralWestminsterAreaLookup.nhsTrustCode!!,
                AreaAssociationType.HEALTHCARE_DATA
            )
        )

        runBlocking { sut.execute() }

        assertThat(db.soaDataDao().all().map { it.areaCode })
            .isEqualTo(listOf(centralWestminsterAreaLookup.msoaCode))
        assertThat(db.areaDataDao().all().map { it.areaCode })
            .isEqualTo(listOf(centralWestminsterAreaLookup.utlaCode))
        assertThat(db.alertLevelDao().all().map { it.areaCode })
            .isEqualTo(listOf(centralWestminsterAreaLookup.utlaCode))
        assertThat(db.healthcareDao().all().map { it.areaCode })
            .isEqualTo(listOf(centralWestminsterAreaLookup.nhsTrustCode!!))
        assertThat(db.metadataDao().all().map { it.id })
            .isEqualTo(
                listOf(
                    MetadataIds.areaCodeId(centralWestminsterAreaLookup.msoaCode),
                    MetadataIds.areaCodeId(centralWestminsterAreaLookup.utlaCode),
                    MetadataIds.alertLevelId(centralWestminsterAreaLookup.utlaCode),
                    MetadataIds.healthcareId(centralWestminsterAreaLookup.nhsTrustCode!!)
                )
            )
        assertThat(db.areaLookupDao().all())
            .isEqualTo(listOf(centralWestminsterAreaLookup))
    }

    @Test
    fun `GIVEN nation area data saved WHEN removeUnusedData called THEN nation area data not cleared`() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        val westminsterNationMetadata = MetadataEntity(
            id = MetadataIds.areaCodeId(centralWestminsterAreaLookup.nationCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        val westminsterNationData = AreaDataEntity(
            areaCode = centralWestminsterAreaLookup.nationCode,
            metadataId = MetadataIds.areaCodeId(centralWestminsterAreaLookup.nationCode),
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
        db.metadataDao().insertAll(
            nations.map { nation ->
                westminsterNationMetadata.copy(id = MetadataIds.areaCodeId(nation.areaCode))
            }
        )
        db.areaDataDao().insertAll(
            nations.map { nation ->
                westminsterNationData.copy(
                    areaCode = nation.areaCode,
                    metadataId = MetadataIds.areaCodeId(nation.areaCode)
                )
            }
        )

        runBlocking { sut.execute() }

        assertThat(db.soaDataDao().all()).isEmpty()
        assertThat(db.areaDataDao().all().map { it.areaCode })
            .isEqualTo(nations.map { it.areaCode })
        assertThat(db.alertLevelDao().all()).isEmpty()
        assertThat(db.healthcareDao().all()).isEmpty()
        assertThat(db.metadataDao().all().map { it.id })
            .isEqualTo(nations.map { MetadataIds.areaCodeId(it.areaCode) })
    }

    companion object {
        val marlyboneAreaLookup = AreaLookupEntity(
            postcode = "NW1 4LJ",
            trimmedPostcode = "NW14LJ",
            lsoaCode = "E01004716",
            lsoaName = "Westminster 011C",
            msoaCode = "E02000970",
            msoaName = "Marylebone & Park Lane",
            ltlaCode = "E09000033",
            ltlaName = "Westminster",
            utlaCode = "E09000033",
            utlaName = "Westminster",
            nhsRegionCode = "E40000003",
            nhsRegionName = "London",
            nhsTrustCode = "RYJ",
            nhsTrustName = "Imperial College Healthcare NHS Trust",
            regionCode = "E12000007",
            regionName = "London",
            nationCode = "E92000001",
            nationName = "England"
        )
        val centralWestminsterAreaLookup = AreaLookupEntity(
            postcode = "W1 1AA",
            trimmedPostcode = "W11AA",
            lsoaCode = "E01004733",
            lsoaName = "Westminster 020C",
            msoaCode = "E02000979",
            msoaName = "Central Westminster",
            ltlaCode = "E09000033",
            ltlaName = "Westminster",
            utlaCode = "E09000033",
            utlaName = "Westminster",
            nhsRegionCode = "E40000003",
            nhsRegionName = "London",
            nhsTrustCode = "RJ1",
            nhsTrustName = "Guy's and St Thomas' NHS Foundation Trust",
            regionCode = "E12000007",
            regionName = "London",
            nationCode = "E92000001",
            nationName = "England"
        )
        val oxfordCentralAreaLookup = AreaLookupEntity(
            postcode = "OX1 1AA",
            trimmedPostcode = "OX11AA",
            lsoaCode = "E01028522",
            lsoaName = "Oxford 008B",
            msoaCode = "E02005947",
            msoaName = "Oxford Central",
            ltlaCode = "E07000178",
            ltlaName = "Oxford",
            utlaCode = "E10000025",
            utlaName = "Oxfordshire",
            nhsRegionCode = "E40000005",
            nhsRegionName = "South East",
            nhsTrustCode = "RTH",
            nhsTrustName = "Oxford University Hospitals NHS Foundation Trust",
            regionCode = "E12000008",
            regionName = "South East",
            nationCode = "E92000001",
            nationName = "England"
        )
        val aberdeenCityAreaLookup = AreaLookupEntity(
            postcode = "AB10 1AB",
            trimmedPostcode = "AB101AB",
            lsoaCode = "S01006646",
            lsoaName = null,
            msoaCode = "S02001261",
            msoaName = null,
            ltlaCode = "S12000033",
            ltlaName = "Aberdeen City",
            utlaCode = "S12000033",
            utlaName = "Aberdeen City",
            nhsRegionCode = null,
            nhsRegionName = null,
            nhsTrustCode = null,
            nhsTrustName = null,
            regionCode = null,
            regionName = null,
            nationCode = "S92000003",
            nationName = "Scotland"
        )
        val nations = listOf(
            AreaEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaName = Constants.UK_AREA_NAME,
                areaType = AreaType.OVERVIEW
            ),
            AreaEntity(
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = Constants.ENGLAND_AREA_NAME,
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = Constants.NORTHERN_IRELAND_AREA_CODE,
                areaName = Constants.NORTHERN_IRELAND_AREA_NAME,
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = Constants.SCOTLAND_AREA_CODE,
                areaName = Constants.SCOTLAND_AREA_NAME,
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = Constants.WALES_AREA_CODE,
                areaName = Constants.WALES_AREA_NAME,
                areaType = AreaType.NATION
            )
        )
    }
}
