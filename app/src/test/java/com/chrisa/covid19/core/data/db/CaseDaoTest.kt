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
import com.chrisa.covid19.core.util.test
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.time.LocalDate
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
class CaseDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no cases WHEN count called THEN count is zero`() {
        val count = db.casesDao().count()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `GIVEN cases exist WHEN count called THEN count is not zero`() {
        db.casesDao().insertAll(
            listOf(
                CaseEntity(
                    areaCode = "1234",
                    areaName = "UK",
                    dailyTotalLabConfirmedCasesRate = 11.0,
                    dailyLabConfirmedCases = 1,
                    date = LocalDate.ofEpochDay(0),
                    totalLabConfirmedCases = 1
                )
            )
        )

        val count = db.casesDao().count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN case does not exist WHEN insertCases called THEN case is added`() = runBlocking {

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        val toAdd = listOf(
            newCaseEntity, newCaseEntity.copy(
                areaCode = "002",
                areaName = "England"
            )
        )

        db.casesDao().areaCases(newCaseEntity.areaCode).test {
            expectNoEvents()

            db.casesDao().insertAll(toAdd)

            val emittedItems = expectItem()

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems[0]).isEqualTo(newCaseEntity)

            cancel()
        }
    }

    @Test
    fun `GIVEN case does exist WHEN insertCases called with same date and area code THEN case is updated`() = runBlocking {

        val oldCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().insertAll(listOf(oldCaseEntity))

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().areaCases(oldCaseEntity.areaCode).test {
            expectNoEvents()

            db.casesDao().insertAll(listOf(newCaseEntity))

            val emittedItems = expectItem()

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems[0]).isEqualTo(newCaseEntity)

            cancel()
        }
    }

    @Test
    fun `GIVEN case does exist WHEN insertCases called with same area code and different date THEN case is added`() = runBlocking {

        val oldCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().insertAll(listOf(oldCaseEntity))

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(1),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().areaCases(oldCaseEntity.areaCode).test {
            expectNoEvents()

            db.casesDao().insertAll(listOf(newCaseEntity))

            val emittedItems = expectItem()

            assertThat(emittedItems.size).isEqualTo(2)
            assertThat(emittedItems[0]).isEqualTo(oldCaseEntity)
            assertThat(emittedItems[1]).isEqualTo(newCaseEntity)

            cancel()
        }
    }

    @Test
    fun `GIVEN no saved areas WHEN searchAllSavedAreaCases called THEN no cases are returned`() = runBlocking {

        val caseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().savedAreaCases().test {
            expectNoEvents()

            db.casesDao().insertAll(listOf(caseEntity))

            val emittedItems = expectItem()

            assertThat(emittedItems.size).isEqualTo(0)

            cancel()
        }
    }

    @Test
    fun `GIVEN saved area exist WHEN searchAllSavedAreaCases called THEN area cases are returned`() = runBlocking {

        val caseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        val insertedCases = listOf(
            caseEntity,
            caseEntity.copy(areaCode = "002", areaName = "England")
        )

        db.casesDao().savedAreaCases().test {
            expectNoEvents()

            db.casesDao().insertAll(insertedCases)

            assertThat(expectItem().size).isEqualTo(0)

            db.savedAreaDao().insert(SavedAreaEntity(areaCode = caseEntity.areaCode))

            val emittedItems = expectItem()

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems[0]).isEqualTo(insertedCases[0])

            cancel()
        }
    }

    @Test
    fun `GIVEN all case areas are saved WHEN searchAllSavedAreaCases called THEN all cases are returned`() = runBlocking {

        val caseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        val toInsert = listOf(
            caseEntity,
            caseEntity.copy(areaCode = "002", areaName = "England")
        )

        val insertedCasesAsSavedAreaEntities =
            toInsert.map { SavedAreaEntity(areaCode = it.areaCode) }

        db.casesDao().savedAreaCases().test {

            expectNoEvents()

            db.casesDao().insertAll(toInsert)

            assertThat(expectItem().size).isEqualTo(0)

            db.savedAreaDao().insert(insertedCasesAsSavedAreaEntities.first())

            val emittedItems = expectItem()

            assertThat(emittedItems.size).isEqualTo(1)
            assertThat(emittedItems[0]).isEqualTo(toInsert.first())

            cancel()
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
