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

package com.chrisa.cviz.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.runtime.coroutines.test
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
        db.areaDataDao().insertAll(listOf(areaData))

        val count = db.areaDataDao().countAll()

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN area data exists WHEN countAllByAreaType called THEN count is not zero`() {
        db.areaDataDao().insertAll(
            listOf(
                areaData,
                areaData.copy(areaCode = "1", areaName = "Liverpool", areaType = AreaType.UTLA)
            )
        )

        val count = db.areaDataDao().countAllByAreaType(AreaType.UTLA)

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN area data exists WHEN deleteAllByAreaCode called THEN data with area code is deleted`() {
        db.areaDataDao().insertAll(
            listOf(
                areaData,
                areaData.copy(areaCode = "1", areaName = "Liverpool", areaType = AreaType.UTLA)
            )
        )
        assertThat(db.areaDataDao().countAll()).isEqualTo(2)

        db.areaDataDao().deleteAllByAreaCode(areaData.areaCode)

        assertThat(db.areaDataDao().countAll()).isEqualTo(1)
    }

    @Test
    fun `GIVEN area data exists WHEN deleteAllNotInAreaCodes called THEN data not in area code is deleted`() {
        db.areaDataDao().insertAll(
            listOf(
                areaData,
                areaData.copy(areaCode = "1", areaName = "Liverpool", areaType = AreaType.UTLA)
            )
        )
        assertThat(db.areaDataDao().countAll()).isEqualTo(2)

        db.areaDataDao().deleteAllNotInAreaCode(listOf(areaData.areaCode))

        assertThat(db.areaDataDao().countAll()).isEqualTo(1)
        assertThat(db.areaDataDao().allByAreaCode(areaData.areaCode)).isEqualTo(listOf(areaData))
    }

    @Test
    fun `GIVEN area data exists WHEN allByAreaCode called THEN data in area code is returned`() {
        val allAreas = listOf(
            areaData,
            areaData.copy(date = areaData.date.plusDays(1))
        )

        db.areaDataDao().insertAll(allAreas)

        assertThat(db.areaDataDao().allByAreaCode(areaData.areaCode)).isEqualTo(allAreas)
    }

    @Test
    fun `GIVEN area data does not exist WHEN insertAll called THEN area data is added`() =
        runBlocking {
            val toAdd = listOf(
                areaData,
                areaData.copy(areaCode = "002", areaName = Constants.ENGLAND_AREA_NAME)
            )

            db.areaDataDao().allByAreaCodeAsFlow(areaData.areaCode).test {
                expectNoEvents()

                db.areaDataDao().insertAll(toAdd)

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(areaData)

                cancel()
            }
        }

    @Test
    fun `GIVEN area data does exist WHEN insertAll called with same date and area code THEN area data is updated`() =
        runBlocking {
            db.areaDataDao().insertAll(listOf(areaData))

            val newArea = areaData.copy(
                newCases = 1,
                cumulativeCases = areaData.cumulativeCases + 1
            )

            db.areaDataDao().allByAreaCodeAsFlow(areaData.areaCode).test {
                expectNoEvents()

                db.areaDataDao().insertAll(listOf(newArea))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(newArea)

                cancel()
            }
        }

    @Test
    fun `GIVEN area data does exist WHEN insertAll called with same area code and different date THEN area data is added`() =
        runBlocking {
            db.areaDataDao().insertAll(listOf(areaData))
            val newArea = areaData.copy(
                date = areaData.date.plusDays(1),
                cumulativeCases = 0
            )

            db.areaDataDao().allByAreaCodeAsFlow(areaData.areaCode).test {
                expectNoEvents()

                db.areaDataDao().insertAll(listOf(newArea))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(2)
                assertThat(emittedItems[0]).isEqualTo(areaData)
                assertThat(emittedItems[1]).isEqualTo(newArea)

                cancel()
            }
        }

    @Test
    fun `GIVEN no saved areas WHEN allSavedAreaData called THEN no area data are returned`() =
        runBlocking {
            db.areaDataDao().allSavedAreaDataAsFlow().test {
                expectNoEvents()

                db.areaDataDao().insertAll(listOf(areaData))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(0)

                cancel()
            }
        }

    @Test
    fun `GIVEN saved area exist WHEN searchAllSavedAreaCases called THEN area area data are returned`() =
        runBlocking {
            val insertedCases = listOf(
                areaData,
                areaData.copy(areaCode = "002", areaName = Constants.ENGLAND_AREA_NAME)
            )

            db.areaDataDao().allSavedAreaDataAsFlow().test {
                expectNoEvents()

                db.areaDataDao().insertAll(insertedCases)

                assertThat(expectItem().size).isEqualTo(0)

                db.savedAreaDao().insert(SavedAreaEntity(areaCode = areaData.areaCode))

                val emittedItems = expectItem()

                assertThat(emittedItems.size).isEqualTo(1)
                assertThat(emittedItems[0]).isEqualTo(insertedCases[0])

                cancel()
            }
        }

    @Test
    fun `GIVEN all case areas are saved WHEN searchAllSavedAreaCases called THEN all area data are returned`() =
        runBlocking {
            val toInsert = listOf(
                areaData,
                areaData.copy(areaCode = "002", areaName = Constants.ENGLAND_AREA_NAME)
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
            val syncTime = areaData.date.atStartOfDay()

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

            val englandArea = areaData.copy(
                metadataId = englandMetadataEntity.id,
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = Constants.ENGLAND_AREA_NAME,
                areaType = AreaType.REGION,
                date = englandMetadataEntity.lastUpdatedAt.toLocalDate()
            )

            val toAdd = listOf(
                areaData,
                englandArea,
                areaData.copy(date = areaData.date.minusDays(21)),
                areaData.copy(date = areaData.date.minusDays(7)),
                areaData.copy(date = areaData.date.minusDays(14))
            )

            db.metadataDao().insert(ukMetadataEntity)
            db.metadataDao().insert(englandMetadataEntity)

            val metadataMap = listOf(ukMetadataEntity, englandMetadataEntity).associateBy { it.id }

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

                    assertThat(emittedItems).isEqualTo(toAdd
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
                        })

                    cancel()
                }
        }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)
        private val areaData = AreaDataEntity(
            areaCode = Constants.UK_AREA_CODE,
            areaName = Constants.UK_AREA_NAME,
            areaType = AreaType.OVERVIEW,
            metadataId = MetaDataIds.areaCodeId(Constants.UK_AREA_CODE),
            date = syncDate.toLocalDate(),
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
    }
}
