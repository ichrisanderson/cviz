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
import kotlin.random.Random
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

            val toInsert = buildAreaSummaryList()

            db.areaSummaryEntityDao().insertAll(toInsert)
            assertThat(db.areaSummaryEntityDao().countAll()).isEqualTo(toInsert.size)

            db.areaSummaryEntityDao().deleteAll()

            assertThat(db.areaSummaryEntityDao().countAll()).isEqualTo(0)
        }

    @Test
    fun `GIVEN area summary exists WHEN allAsFlow called THEN area summary is emitted`() =
        runBlocking {

            val toInsert = buildAreaSummaryList()

            db.areaSummaryEntityDao().allAsFlow().test {

                expectNoEvents()

                db.areaSummaryEntityDao().insertAll(toInsert)
                val emittedItems = expectItem()

                assertThat(emittedItems).isEqualTo(
                    toInsert
                )

                cancel()
            }
        }

    private fun buildAreaSummaryList(): List<AreaSummaryEntity> {
        val toInsert = mutableListOf<AreaSummaryEntity>()
        val random = Random(0)
        for (index in 1..100) {
            toInsert.add(
                AreaSummaryEntity(
                    areaCode = "Area_$index",
                    areaType = AreaType.UTLA,
                    areaName = "Area_$index",
                    date = syncTime.toLocalDate(),
                    baseInfectionRate = random.nextDouble(),
                    cumulativeCasesWeek1 = random.nextInt(),
                    cumulativeCaseInfectionRateWeek1 = random.nextDouble(),
                    newCaseInfectionRateWeek1 = random.nextDouble(),
                    newCasesWeek1 = random.nextInt(),
                    cumulativeCasesWeek2 = random.nextInt(),
                    cumulativeCaseInfectionRateWeek2 = random.nextDouble(),
                    newCaseInfectionRateWeek2 = random.nextDouble(),
                    newCasesWeek2 = random.nextInt(),
                    cumulativeCasesWeek3 = random.nextInt(),
                    cumulativeCaseInfectionRateWeek3 = random.nextDouble(),
                    newCaseInfectionRateWeek3 = random.nextDouble(),
                    newCasesWeek3 = random.nextInt(),
                    cumulativeCasesWeek4 = random.nextInt(),
                    cumulativeCaseInfectionRateWeek4 = random.nextDouble()
                )
            )
        }
        return toInsert
    }
}

data class Foo(val areaName: String, val cases: Int)
