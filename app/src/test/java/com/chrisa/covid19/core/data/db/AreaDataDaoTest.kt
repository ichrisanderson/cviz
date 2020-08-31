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

package com.chrisa.covid19.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.runtime.coroutines.test
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime

@InternalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class AreaDataDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no area data WHEN count called THEN count is zero`() {
        val count = db.areaDataDao().countAll()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `GIVEN area data exists WHEN count called THEN count is not zero`() {
        db.areaDataDao().insertAll(
            listOf(
                AreaDataEntity(
                    metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                    areaCode = "1234",
                    areaName = "UK",
                    areaType = AreaType.OVERVIEW,
                    infectionRate = 11.0,
                    newCases = 1,
                    date = LocalDate.ofEpochDay(0),
                    cumulativeCases = 1
                )
            )
        )

        val count = db.areaDataDao().countAll()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN area data exists WHEN countAllByAreaType called THEN count is not zero`() {
        val area =
            AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "1234",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                infectionRate = 11.0,
                newCases = 1,
                date = LocalDate.ofEpochDay(0),
                cumulativeCases = 1
            )

        db.areaDataDao().insertAll(
            listOf(
                area,
                area.copy(areaCode = "1", areaName = "Liverpool", areaType = AreaType.UTLA)
            )
        )

        val count = db.areaDataDao().countAllByAreaType(AreaType.UTLA)
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN area data exists WHEN deleteAllByAreaCode called THEN data with area code is deleted`() {

        val area =
            AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "1234",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                infectionRate = 11.0,
                newCases = 1,
                date = LocalDate.ofEpochDay(0),
                cumulativeCases = 1
            )

        db.areaDataDao().insertAll(
            listOf(
                area,
                area.copy(areaCode = "1", areaName = "Liverpool", areaType = AreaType.UTLA)
            )
        )

        assertThat(db.areaDataDao().countAll()).isEqualTo(2)

        db.areaDataDao().deleteAllByAreaCode(area.areaCode)

        assertThat(db.areaDataDao().countAll()).isEqualTo(1)
    }

    @Test
    fun `GIVEN area data exists WHEN deleteAllNotInAreaCodes called THEN data not in area code is deleted`() {

        val area =
            AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "1234",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                infectionRate = 11.0,
                newCases = 1,
                date = LocalDate.ofEpochDay(0),
                cumulativeCases = 1
            )

        db.areaDataDao().insertAll(
            listOf(
                area,
                area.copy(areaCode = "1", areaName = "Liverpool", areaType = AreaType.UTLA)
            )
        )

        assertThat(db.areaDataDao().countAll()).isEqualTo(2)

        db.areaDataDao().deleteAllNotInAreaCodes(listOf(area.areaCode))

        assertThat(db.areaDataDao().countAll()).isEqualTo(1)
        assertThat(db.areaDataDao().allByAreaCode(area.areaCode)).isEqualTo(listOf(area))
    }

    @Test
    fun `GIVEN area data exists WHEN allByAreaCode called THEN data in area code is returned`() {

        val area =
            AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "1234",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                infectionRate = 11.0,
                newCases = 1,
                date = LocalDate.ofEpochDay(0),
                cumulativeCases = 1
            )

        val allAreas = listOf(
            area,
            area.copy(date = area.date.plusDays(1))
        )

        db.areaDataDao().insertAll(allAreas)

        assertThat(db.areaDataDao().allByAreaCode(area.areaCode)).isEqualTo(allAreas)
    }

    @Test
    fun `GIVEN area data does not exist WHEN insertAll called THEN area data is added`() =
        runBlocking {

            val newCaseEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 12,
                cumulativeCases = 33,
                infectionRate = 100.0
            )

            val toAdd = listOf(
                newCaseEntity, newCaseEntity.copy(
                    areaCode = "002",
                    areaName = "England"
                )
            )

            db.areaDataDao().allByAreaCodeAsFlow(newCaseEntity.areaCode).test {
                expectNoEvents()

                db.areaDataDao().insertAll(toAdd)

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(newCaseEntity)

                cancel()
            }
        }

    @Test
    fun `GIVEN area data does exist WHEN insertAll called with same date and area code THEN area data is updated`() =
        runBlocking {

            val oldCaseEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 9,
                cumulativeCases = 9,
                infectionRate = 9.0
            )

            db.areaDataDao().insertAll(listOf(oldCaseEntity))

            val newCaseEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 12,
                cumulativeCases = 33,
                infectionRate = 100.0
            )

            db.areaDataDao().allByAreaCodeAsFlow(oldCaseEntity.areaCode).test {
                expectNoEvents()

                db.areaDataDao().insertAll(listOf(newCaseEntity))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(newCaseEntity)

                cancel()
            }
        }

    @Test
    fun `GIVEN area data does exist WHEN insertAll called with same area code and different date THEN area data is added`() =
        runBlocking {

            val oldCaseEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 9,
                cumulativeCases = 9,
                infectionRate = 9.0
            )

            db.areaDataDao().insertAll(listOf(oldCaseEntity))

            val newCaseEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(1),
                newCases = 12,
                cumulativeCases = 33,
                infectionRate = 100.0
            )

            db.areaDataDao().allByAreaCodeAsFlow(oldCaseEntity.areaCode).test {
                expectNoEvents()

                db.areaDataDao().insertAll(listOf(newCaseEntity))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(2)
                assertThat(emittedItems[0]).isEqualTo(oldCaseEntity)
                assertThat(emittedItems[1]).isEqualTo(newCaseEntity)

                cancel()
            }
        }

    @Test
    fun `GIVEN no saved areas WHEN allSavedAreaData called THEN no area data are returned`() =
        runBlocking {

            val areaDataEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 9,
                cumulativeCases = 9,
                infectionRate = 9.0
            )

            db.areaDataDao().allSavedAreaDataAsFlow().test {
                expectNoEvents()

                db.areaDataDao().insertAll(listOf(areaDataEntity))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(0)

                cancel()
            }
        }

    @Test
    fun `GIVEN saved area exist WHEN searchAllSavedAreaCases called THEN area area data are returned`() =
        runBlocking {

            val areaDataEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 9,
                cumulativeCases = 9,
                infectionRate = 9.0
            )

            val insertedCases = listOf(
                areaDataEntity,
                areaDataEntity.copy(areaCode = "002", areaName = "England")
            )

            db.areaDataDao().allSavedAreaDataAsFlow().test {
                expectNoEvents()

                db.areaDataDao().insertAll(insertedCases)

                assertThat(expectItem().size).isEqualTo(0)

                db.savedAreaDao().insert(SavedAreaEntity(areaCode = areaDataEntity.areaCode))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(insertedCases[0])

                cancel()
            }
        }

    @Test
    fun `GIVEN all case areas are saved WHEN searchAllSavedAreaCases called THEN all area data are returned`() =
        runBlocking {

            val areaDataEntity = AreaDataEntity(
                metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                areaCode = "001",
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = LocalDate.ofEpochDay(0),
                newCases = 9,
                cumulativeCases = 9,
                infectionRate = 9.0
            )

            val toInsert = listOf(
                areaDataEntity,
                areaDataEntity.copy(areaCode = "002", areaName = "England")
            )

            val insertedCasesAsSavedAreaEntities =
                toInsert.map { SavedAreaEntity(areaCode = it.areaCode) }

            db.areaDataDao().allSavedAreaDataAsFlow().test {

                expectNoEvents()

                db.areaDataDao().insertAll(toInsert)

                assertThat(expectItem().size).isEqualTo(0)

                db.savedAreaDao().insert(insertedCasesAsSavedAreaEntities.first())

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(toInsert.first())

                cancel()
            }
        }

    @Test
    fun `GIVEN area data exists WHEN latestWithMetadataByAreaCodeAsFlow called THEN area data metadata is emitted`() =
        runBlocking {

            val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

            val ukMetadataEntity = MetadataEntity(
                id = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
                lastUpdatedAt = syncTime,
                lastSyncTime = syncTime
            )

            val englandMetadataEntity = MetadataEntity(
                id = MetaDataIds.areaCodeId(Constants.ENGLAND_AREA_CODE),
                lastUpdatedAt = syncTime.plusDays(1),
                lastSyncTime = syncTime.plusDays(1)
            )

            val ukCaseEntity = AreaDataEntity(
                metadataId = ukMetadataEntity.id,
                areaCode = Constants.UK_AREA_CODE,
                areaName = "UK",
                areaType = AreaType.OVERVIEW,
                date = ukMetadataEntity.lastUpdatedAt.toLocalDate(),
                newCases = 12,
                cumulativeCases = 33,
                infectionRate = 100.0
            )
            val englandCaseEntity = ukCaseEntity.copy(
                metadataId = englandMetadataEntity.id,
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = "England",
                areaType = AreaType.REGION,
                date = englandMetadataEntity.lastUpdatedAt.toLocalDate()
            )

            val toAdd = listOf(
                ukCaseEntity,
                englandCaseEntity,
                ukCaseEntity.copy(date = ukCaseEntity.date.minusDays(21)),
                ukCaseEntity.copy(date = ukCaseEntity.date.minusDays(7)),
                ukCaseEntity.copy(date = ukCaseEntity.date.minusDays(14))
            )

            db.metadataDao().insert(ukMetadataEntity)
            db.metadataDao().insert(englandMetadataEntity)

            val metadataMap = listOf(ukMetadataEntity, englandMetadataEntity).associateBy { it.id }

            val expectedItems = toAdd
                .sortedByDescending { it.date }
                .map {
                    AreaDataMetadataTuple(
                        lastUpdatedAt = metadataMap[it.metadataId]!!.lastUpdatedAt,
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        areaType = it.areaType,
                        date = it.date,
                        cumulativeCases = it.cumulativeCases,
                        infectionRate = it.infectionRate,
                        newCases = it.newCases
                    )
                }
            db.areaDataDao().insertAll(toAdd)

            db.areaDataDao().latestWithMetadataByAreaCodeAsFlow(
                listOf(
                    Constants.UK_AREA_CODE,
                    Constants.ENGLAND_AREA_CODE,
                    Constants.NORTHERN_IRELAND_AREA_CODE,
                    Constants.SCOTLAND_AREA_CODE,
                    Constants.WALES_AREA_CODE
                )
            )
                .test {

                    val emittedItems = expectItem()

                    assertThat(emittedItems).isEqualTo(expectedItems)

                    cancel()
                }
        }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
