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
class DatabaseCleanerTest {

    private lateinit var db: AppDatabase
    private lateinit var sut: DatabaseCleaner

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        seedAreaLookups()
        seedAlertLevels()
        seedAreaData()
        seedSoaData()
        seedHealthcareLookups()
        seedHealthcare()
        seedMetadata()

        sut = DatabaseCleaner(db, SnapshotProvider())
    }

    private fun seedAreaLookups() {
        db.areaLookupDao().insert(aberdeenCityAreaLookup)
        db.areaLookupDao().insert(
            AreaLookupEntity(
                postcode = "CF10 1AR",
                trimmedPostcode = "CF101AR",
                lsoaCode = "W01001939",
                lsoaName = "Cardiff 032F",
                msoaCode = "W02000398",
                msoaName = "Cathays South & Bute Park",
                ltlaCode = "W06000015",
                ltlaName = "Cardiff",
                utlaCode = "W06000015",
                utlaName = "Cardiff",
                nhsRegionCode = null,
                nhsRegionName = null,
                nhsTrustCode = null,
                nhsTrustName = null,
                regionCode = null,
                regionName = null,
                nationCode = "W92000004",
                nationName = "Wales"
            )
        )
        db.areaLookupDao().insert(centralWestminsterAreaLookup)
        db.areaLookupDao().insert(oxfordCentralAreaLookup)
        db.areaLookupDao().insert(
            AreaLookupEntity(
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
        )
    }

    private fun seedSoaData() {
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
        db.soaDataDao().insertAll(
            listOf(
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
        )
    }

    private fun seedAreaData() {
        val ukAreaData = AreaDataEntity(
            areaCode = Constants.UK_AREA_CODE,
            areaName = Constants.UK_AREA_NAME,
            areaType = AreaType.OVERVIEW,
            metadataId = MetaDataIds.areaSummaryId(),
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
        db.areaDataDao().insertAll(
            listOf(
                ukAreaData,
                ukAreaData.copy(
                    areaCode = westministerArea.areaCode,
                    areaName = westministerArea.areaName,
                    areaType = westministerArea.areaType,
                    metadataId = MetaDataIds.areaCodeId(westministerArea.areaCode)
                ),
                ukAreaData.copy(
                    areaCode = oxfordshireArea.areaCode,
                    areaName = oxfordshireArea.areaName,
                    areaType = oxfordshireArea.areaType,
                    metadataId = MetaDataIds.areaCodeId(oxfordshireArea.areaCode)
                ),
                ukAreaData.copy(
                    areaCode = oxfordCentralAreaLookup.regionCode!!,
                    areaName = oxfordCentralAreaLookup.regionName!!,
                    areaType = AreaType.REGION,
                    metadataId = MetaDataIds.areaCodeId(oxfordCentralAreaLookup.regionCode!!)
                ),
                ukAreaData.copy(
                    areaCode = cardiffArea.areaCode,
                    areaName = cardiffArea.areaName,
                    areaType = cardiffArea.areaType,
                    metadataId = MetaDataIds.areaCodeId(cardiffArea.areaCode)
                ),
                ukAreaData.copy(
                    areaCode = aberdeenCityArea.areaCode,
                    areaName = aberdeenCityArea.areaName,
                    areaType = aberdeenCityArea.areaType,
                    metadataId = MetaDataIds.areaCodeId(aberdeenCityArea.areaCode)
                ),
                ukAreaData.copy(
                    areaCode = Constants.ENGLAND_AREA_CODE,
                    areaName = Constants.ENGLAND_AREA_NAME,
                    areaType = AreaType.NATION,
                    metadataId = MetaDataIds.areaCodeId(Constants.ENGLAND_AREA_CODE)
                ),
                ukAreaData.copy(
                    areaCode = Constants.SCOTLAND_AREA_CODE,
                    areaName = Constants.SCOTLAND_AREA_NAME,
                    areaType = AreaType.NATION,
                    metadataId = MetaDataIds.areaCodeId(Constants.SCOTLAND_AREA_CODE)
                ),
                ukAreaData.copy(
                    areaCode = Constants.NORTHERN_IRELAND_AREA_CODE,
                    areaName = Constants.NORTHERN_IRELAND_AREA_NAME,
                    areaType = AreaType.NATION,
                    metadataId = MetaDataIds.areaCodeId(Constants.NORTHERN_IRELAND_AREA_CODE)
                ),
                ukAreaData.copy(
                    areaCode = Constants.WALES_AREA_CODE,
                    areaName = Constants.WALES_AREA_CODE,
                    areaType = AreaType.NATION,
                    metadataId = MetaDataIds.areaCodeId(Constants.WALES_AREA_CODE)
                )
            )
        )
    }

    private fun seedAlertLevels() {
        val westminsterAlertLevel = AlertLevelEntity(
            areaCode = westministerArea.areaCode,
            areaName = westministerArea.areaName,
            areaType = westministerArea.areaType,
            date = LocalDate.of(2021, 2, 14),
            alertLevel = 2,
            alertLevelName = "Stay Alert",
            alertLevelUrl = "http://acme.com",
            alertLevelValue = 2
        )
        db.alertLevelDao().insertAll(
            listOf(
                westminsterAlertLevel,
                westminsterAlertLevel.copy(
                    areaCode = oxfordshireArea.areaCode,
                    areaName = oxfordshireArea.areaName
                )
            )
        )
    }

    private fun seedHealthcareLookups() {
        val westminsterTrusts = westminsterHealthcareTrusts.map {
            HealthcareLookupEntity(
                areaCode = westministerArea.areaCode,
                nhsTrustCode = it
            )
        }
        val oxfordTrusts = oxfordHealthcareTrusts.map {
            HealthcareLookupEntity(
                areaCode = oxfordshireArea.areaCode,
                nhsTrustCode = it
            )
        }

        db.healthcareLookupDao().insertAll(
            westminsterTrusts
                .plus(oxfordTrusts)
        )
    }

    private fun seedHealthcare() {
        val westminsterHealthcare = westminsterHealthcareTrusts.map {
            ukHealthcare.copy(
                areaCode = it,
                areaName = "Westminster Trust",
                areaType = AreaType.NHS_TRUST
            )
        }
        val oxfordHealthcare = oxfordHealthcareTrusts.map {
            ukHealthcare.copy(
                areaCode = it,
                areaName = "Oxford Trust",
                areaType = AreaType.NHS_TRUST
            )
        }
        db.healthcareDao().insertAll(
            listOf(
                ukHealthcare,
                englandHealthcare,
                englandHealthcare.copy(
                    areaCode = Constants.SCOTLAND_AREA_CODE,
                    areaName = Constants.SCOTLAND_AREA_NAME
                ),
                englandHealthcare.copy(
                    areaCode = Constants.WALES_AREA_CODE,
                    areaName = Constants.WALES_AREA_NAME
                ),
                englandHealthcare.copy(
                    areaCode = Constants.NORTHERN_IRELAND_AREA_CODE,
                    areaName = Constants.NORTHERN_IRELAND_AREA_CODE
                )
            )
                .plus(westminsterHealthcare)
                .plus(oxfordHealthcare)
                .plus(
                    listOf(
                        londonRegionHealthcare
                    )
                )
        )
    }

    private fun seedMetadata() {
        val ukOverviewMetadata = MetadataEntity(
            id = MetaDataIds.areaSummaryId(),
            lastUpdatedAt = LocalDateTime.of(2020, 2, 1, 0, 0),
            lastSyncTime = LocalDateTime.of(2020, 1, 1, 0, 0)
        )
        val healthcareAreaCodes = db.healthcareDao().all().map { it.areaCode }
        db.metadataDao().insertAll(
            listOf(
                ukOverviewMetadata,
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(centralWestminsterAreaLookup.msoaCode)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(centralWestminsterAreaLookup.utlaCode)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(centralWestminsterAreaLookup.regionCode!!)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(marlyboneArea.areaCode)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(oxfordCentralAreaLookup.msoaCode)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(oxfordCentralAreaLookup.utlaCode)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(oxfordCentralAreaLookup.regionCode!!)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(cardiffArea.areaCode)),
                ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(aberdeenCityArea.areaCode))
            )
                .plus(nations.map { ukOverviewMetadata.copy(id = MetaDataIds.areaCodeId(it)) })
                .plus(healthcareAreaCodes.map {
                    ukOverviewMetadata.copy(id = MetaDataIds.healthcareId(it))
                })
        )
    }

    @Test
    fun `GIVEN soa area saved WHEN removeUnusedData called THEN soa data retained`() {
        db.savedAreaDao().insertAll(
            listOf(
                SavedAreaEntity(centralWestminsterAreaLookup.msoaCode)
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedAlertLevelAreaCodes).isEqualTo(
            listOf(
                centralWestminsterAreaLookup.utlaCode
            )
        )
        assertThat(testSnapshot.retainedSoaAreaCodes).isEqualTo(listOf(centralWestminsterAreaLookup.msoaCode))
        assertThat(testSnapshot.retainedAreaLookupCodes).isEqualTo(
            listOf(
                centralWestminsterAreaLookup.lsoaCode
            )
        )
        assertThat(testSnapshot.retainedAreaDataAreaCodes).containsExactlyElementsIn(
            listOf(centralWestminsterAreaLookup.utlaCode)
                .plus(nations)
                .plus(Constants.UK_AREA_CODE)
        )
        assertThat(testSnapshot.retainedHealthcareAreaCodes).containsExactlyElementsIn(
            westminsterHealthcareTrusts
                .plus(centralWestminsterAreaLookup.nationCode)
                .plus(centralWestminsterAreaLookup.nhsRegionCode)
        )
        assertThat(testSnapshot.retainedMetadataIds).containsExactlyElementsIn(
            listOf(
                centralWestminsterAreaLookup.msoaCode,
                centralWestminsterAreaLookup.utlaCode,
                centralWestminsterAreaLookup.regionCode!!
            ).map { MetaDataIds.areaCodeId(it) }
                .plus(
                    westminsterHealthcareTrusts
                        .plus(centralWestminsterAreaLookup.nationCode)
                        .plus(centralWestminsterAreaLookup.nhsRegionCode!!)
                        .map { MetaDataIds.healthcareId(it) }
                )
                .plus(nations.map { MetaDataIds.areaCodeId(it) })
                .plus(MetaDataIds.areaSummaryId())
        )
    }

    @Test
    fun `GIVEN utla area saved WHEN removeUnusedData called THEN utla data retained`() {
        db.savedAreaDao().insertAll(
            listOf(
                SavedAreaEntity(aberdeenCityAreaLookup.utlaCode)
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedAlertLevelAreaCodes).isEmpty()
        assertThat(testSnapshot.retainedSoaAreaCodes).isEmpty()
        assertThat(testSnapshot.retainedAreaLookupCodes).isEqualTo(listOf(aberdeenCityAreaLookup.lsoaCode))
        assertThat(testSnapshot.retainedAreaDataAreaCodes).containsExactlyElementsIn(
            listOf(aberdeenCityAreaLookup.utlaCode)
                .plus(nations)
                .plus(Constants.UK_AREA_CODE)
        )
        assertThat(testSnapshot.retainedHealthcareAreaCodes).containsExactlyElementsIn(
            listOf(aberdeenCityAreaLookup.nationCode)
        )
        assertThat(testSnapshot.retainedMetadataIds).containsExactlyElementsIn(
            listOf(aberdeenCityArea.areaCode).map { MetaDataIds.areaCodeId(it) }
                .plus(
                    listOf(aberdeenCityAreaLookup.nationCode)
                        .map { MetaDataIds.healthcareId(it) }
                )
                .plus(nations.map { MetaDataIds.areaCodeId(it) })
                .plus(MetaDataIds.areaSummaryId())
        )
    }

    @Test
    fun `GIVEN region area saved WHEN removeUnusedData called THEN region data retained`() {
        db.savedAreaDao().insertAll(
            listOf(
                SavedAreaEntity(oxfordCentralAreaLookup.regionCode!!)
            )
        )

        runBlocking { sut.removeUnusedData() }

        val testSnapshot = TestSnapshot(db)
        assertThat(testSnapshot.retainedAlertLevelAreaCodes).isEqualTo(
            listOf(
                oxfordCentralAreaLookup.utlaCode
            )
        )
        assertThat(testSnapshot.retainedSoaAreaCodes).isEmpty()
        assertThat(testSnapshot.retainedAreaLookupCodes).isEqualTo(listOf(oxfordCentralAreaLookup.lsoaCode))
        assertThat(testSnapshot.retainedAreaDataAreaCodes).containsExactlyElementsIn(
            listOf(oxfordCentralAreaLookup.regionCode!!)
                .plus(nations)
                .plus(Constants.UK_AREA_CODE)
        )
        assertThat(testSnapshot.retainedHealthcareAreaCodes).containsExactlyElementsIn(
            listOf(
                oxfordCentralAreaLookup.nhsTrustCode!!,
                oxfordCentralAreaLookup.nationCode
            )
        )
        assertThat(testSnapshot.retainedMetadataIds).containsExactlyElementsIn(
            listOf(oxfordCentralAreaLookup.regionCode!!).map { MetaDataIds.areaCodeId(it) }
                .plus(
                    listOf(
                        oxfordCentralAreaLookup.nhsTrustCode!!,
                        oxfordCentralAreaLookup.nationCode
                    )
                        .map { MetaDataIds.healthcareId(it) }
                )
                .plus(nations.map { MetaDataIds.areaCodeId(it) })
                .plus(MetaDataIds.areaSummaryId())
        )
    }

    class TestSnapshot(db: AppDatabase) {
        val retainedAlertLevelAreaCodes = db.alertLevelDao().all().map { it.areaCode }
        val retainedSoaAreaCodes = db.soaDataDao().all().map { it.areaCode }
        val retainedAreaLookupCodes = db.areaLookupDao().all().map { it.lsoaCode }
        val retainedAreaDataAreaCodes = db.areaDataDao().all().map { it.areaCode }
        val retainedHealthcareAreaCodes = db.healthcareDao().all().map { it.areaCode }
        val retainedMetadataIds = db.metadataDao().all().map { it.id }
    }

    companion object {
        val nations = listOf(
            Constants.ENGLAND_AREA_CODE,
            Constants.NORTHERN_IRELAND_AREA_CODE,
            Constants.SCOTLAND_AREA_CODE,
            Constants.WALES_AREA_CODE
        )
        val ukHealthcare = HealthcareEntity(
            areaCode = Constants.UK_AREA_CODE,
            areaName = Constants.UK_AREA_NAME,
            areaType = AreaType.OVERVIEW,
            date = LocalDate.of(2021, 2, 11),
            newAdmissions = 10,
            cumulativeAdmissions = 100,
            occupiedBeds = 70,
            transmissionRateMin = 0.8,
            transmissionRateMax = 1.1,
            transmissionRateGrowthRateMin = 0.7,
            transmissionRateGrowthRateMax = 1.2
        )
        val englandHealthcare = ukHealthcare.copy(
            areaCode = Constants.ENGLAND_AREA_CODE,
            areaName = Constants.ENGLAND_AREA_NAME,
            areaType = AreaType.NATION
        )
        val londonRegionHealthcare = ukHealthcare.copy(
            areaCode = "E40000003",
            areaName = "London NHS Region",
            areaType = AreaType.NHS_REGION
        )
        val westministerArea = AreaEntity(
            areaCode = "E09000033",
            areaName = "Westminister",
            areaType = AreaType.UTLA
        )
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
        val oxfordCentralArea = AreaEntity(
            areaCode = "E02005947",
            areaName = "Oxford Central",
            areaType = AreaType.MSOA
        )
        val oxfordshireArea = AreaEntity(
            areaCode = "E10000025",
            areaName = "Oxfordshire",
            areaType = AreaType.UTLA
        )
        val cardiffArea = AreaEntity(
            areaCode = "W06000015",
            areaName = "Cardiff",
            areaType = AreaType.UTLA
        )
        val aberdeenCityArea = AreaEntity(
            areaCode = "S12000033",
            areaName = "Aberdeen City",
            areaType = AreaType.UTLA
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
        val westminsterHealthcareTrusts = listOf(
            "RV3",
            "RYJ",
            "RRV",
            "RJ1",
            "RWF",
            "RAL",
            "RYX"
        )

        val oxfordHealthcareTrusts = listOf(
            "RTH",
            "RNU",
            "RHW",
            "RXQ"
        )
    }
}
