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
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class AreaSummaryEntityDaoTest {

    private lateinit var db: AppDatabase
    private val syncTime = LocalDateTime.of(2020, 2, 3, 0, 0)

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN area summary does not exist WHEN insertAll called THEN area summary entity is added`() =
        runBlocking {

            val areaSummaryEntity = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                areaName = "UK",
                date = syncTime.toLocalDate(),
                baseInfectionRate = 100.0,
                cumulativeCasesWeek1 = 100,
                cumulativeCaseInfectionRateWeek1 = 85.0,
                newCaseInfectionRateWeek1 = 25.0,
                newCasesWeek1 = 30,
                cumulativeCasesWeek2 = 80,
                cumulativeCaseInfectionRateWeek2 = 80.0,
                newCaseInfectionRateWeek2 = 22.0,
                newCasesWeek2 = 22,
                cumulativeCasesWeek3 = 66,
                cumulativeCaseInfectionRateWeek3 = 82.0,
                newCaseInfectionRateWeek3 = 33.0,
                newCasesWeek3 = 26,
                cumulativeCasesWeek4 = 50,
                cumulativeCaseInfectionRateWeek4 = 75.0
            )

            db.areaSummaryEntityDao().insertAll(listOf(areaSummaryEntity))

            assertThat(db.areaSummaryEntityDao().countAll()).isEqualTo(1)
        }

    @Test
    fun `GIVEN area summary does exist WHEN insertAll called THEN area summary entity is replaced`() =
        runBlocking {

            val areaSummaryEntity = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                areaName = "UK",
                date = syncTime.toLocalDate(),
                baseInfectionRate = 100.0,
                cumulativeCasesWeek1 = 100,
                cumulativeCaseInfectionRateWeek1 = 85.0,
                newCaseInfectionRateWeek1 = 25.0,
                newCasesWeek1 = 30,
                cumulativeCasesWeek2 = 80,
                cumulativeCaseInfectionRateWeek2 = 80.0,
                newCaseInfectionRateWeek2 = 22.0,
                newCasesWeek2 = 22,
                cumulativeCasesWeek3 = 66,
                cumulativeCaseInfectionRateWeek3 = 82.0,
                newCaseInfectionRateWeek3 = 33.0,
                newCasesWeek3 = 26,
                cumulativeCasesWeek4 = 50,
                cumulativeCaseInfectionRateWeek4 = 75.0
            )

            val updatedArea = areaSummaryEntity.copy(date = areaSummaryEntity.date.plusDays(1))
            db.areaSummaryEntityDao().insertAll(listOf(areaSummaryEntity))
            db.areaSummaryEntityDao().insertAll(listOf(updatedArea))

            assertThat(db.areaSummaryEntityDao().countAll()).isEqualTo(1)
            assertThat(db.areaSummaryEntityDao().byAreaCode(areaSummaryEntity.areaCode)).isEqualTo(
                updatedArea
            )
        }

    @Test
    fun `GIVEN area summary exists WHEN deleteAll called THEN area summary table is cleared`() =
        runBlocking {

            val ukSummary = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                areaName = "UK",
                date = syncTime.toLocalDate(),
                baseInfectionRate = 100.0,
                cumulativeCasesWeek1 = 100,
                cumulativeCaseInfectionRateWeek1 = 85.0,
                newCaseInfectionRateWeek1 = 25.0,
                newCasesWeek1 = 30,
                cumulativeCasesWeek2 = 80,
                cumulativeCaseInfectionRateWeek2 = 80.0,
                newCaseInfectionRateWeek2 = 22.0,
                newCasesWeek2 = 22,
                cumulativeCasesWeek3 = 66,
                cumulativeCaseInfectionRateWeek3 = 82.0,
                newCaseInfectionRateWeek3 = 33.0,
                newCasesWeek3 = 26,
                cumulativeCasesWeek4 = 50,
                cumulativeCaseInfectionRateWeek4 = 75.0
            )
            val englandSummary = ukSummary.copy(
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = "England",
                areaType = AreaType.REGION
            )

            db.areaSummaryEntityDao().insertAll(listOf(ukSummary, englandSummary))

            assertThat(db.areaSummaryEntityDao().countAll()).isEqualTo(2)

            db.areaSummaryEntityDao().deleteAll()

            assertThat(db.areaSummaryEntityDao().countAll()).isEqualTo(0)
        }

    @Test
    fun `GIVEN area summary exists WHEN topAreasByLastestCaseInfectionRateAsFlow called THEN area summary is emitted in correct order`() =
        runBlocking {

            val ukSummary = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                areaName = "UK",
                date = syncTime.toLocalDate(),
                baseInfectionRate = 100.0,
                cumulativeCasesWeek1 = 100,
                cumulativeCaseInfectionRateWeek1 = 85.0,
                newCaseInfectionRateWeek1 = 25.0,
                newCasesWeek1 = 30,
                cumulativeCasesWeek2 = 80,
                cumulativeCaseInfectionRateWeek2 = 80.0,
                newCaseInfectionRateWeek2 = 22.0,
                newCasesWeek2 = 22,
                cumulativeCasesWeek3 = 66,
                cumulativeCaseInfectionRateWeek3 = 82.0,
                newCaseInfectionRateWeek3 = 33.0,
                newCasesWeek3 = 26,
                cumulativeCasesWeek4 = 50,
                cumulativeCaseInfectionRateWeek4 = 75.0
            )
            val englandSummary = ukSummary.copy(
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = "England",
                areaType = AreaType.REGION,
                newCaseInfectionRateWeek1 = ukSummary.newCaseInfectionRateWeek1 * 2.0
            )

            val toInsert = listOf(ukSummary, englandSummary)

            db.areaSummaryEntityDao().topAreasByLastestCaseInfectionRateAsFlow().test {

                expectNoEvents()

                db.areaSummaryEntityDao().insertAll(toInsert)
                val emittedItems = expectItem()

                assertThat(emittedItems).isEqualTo(toInsert.sortedByDescending { it.newCaseInfectionRateWeek1 })

                cancel()
            }
        }

    @Test
    fun `GIVEN more than 10 area summaries exists WHEN topAreasByLastestCaseInfectionRateAsFlow called THEN area summary is emitted in with limit of 10`() =
        runBlocking {

            val ukSummary = AreaSummaryEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaType = AreaType.OVERVIEW,
                areaName = "UK",
                date = syncTime.toLocalDate(),
                baseInfectionRate = 100.0,
                cumulativeCasesWeek1 = 100,
                cumulativeCaseInfectionRateWeek1 = 85.0,
                newCaseInfectionRateWeek1 = 25.0,
                newCasesWeek1 = 30,
                cumulativeCasesWeek2 = 80,
                cumulativeCaseInfectionRateWeek2 = 80.0,
                newCaseInfectionRateWeek2 = 22.0,
                newCasesWeek2 = 22,
                cumulativeCasesWeek3 = 66,
                cumulativeCaseInfectionRateWeek3 = 82.0,
                newCaseInfectionRateWeek3 = 33.0,
                newCasesWeek3 = 26,
                cumulativeCasesWeek4 = 50,
                cumulativeCaseInfectionRateWeek4 = 75.0
            )

            val toInsert = (1..12).map { index ->
                ukSummary.copy(
                    areaCode = "${ukSummary.areaCode}_$index",
                    date = ukSummary.date.plusDays(index.toLong())
                )
            }

            db.areaSummaryEntityDao().topAreasByLastestCaseInfectionRateAsFlow().test {

                expectNoEvents()

                db.areaSummaryEntityDao().insertAll(toInsert)
                val emittedItems = expectItem()

                assertThat(emittedItems).isEqualTo(
                    toInsert.sortedByDescending { it.newCaseInfectionRateWeek1 }
                        .take(10)
                )

                cancel()
            }
        }
}
