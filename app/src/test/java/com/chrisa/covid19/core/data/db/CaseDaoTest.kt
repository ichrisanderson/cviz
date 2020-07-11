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
import java.io.IOException
import java.util.Date
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
    fun `GIVEN no cases WHEN casesCount called THEN count is zero`() {
        val count = db.casesDao().casesCount()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `GIVEN cases exist WHEN casesCount called THEN count is not zero`() {
        db.casesDao().insertAll(
            listOf(
                CaseEntity(
                    areaCode = "1234",
                    areaName = "UK",
                    dailyTotalLabConfirmedCasesRate = 11.0,
                    dailyLabConfirmedCases = 1,
                    date = Date(0),
                    totalLabConfirmedCases = 1
                )
            )
        )

        val count = db.casesDao().casesCount()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN case does not exist WHEN insertCases called THEN case is added`() {

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().insertAll(listOf(newCaseEntity))

        val cases = db.casesDao().searchAllCases(newCaseEntity.areaCode)

        assertThat(cases.size).isEqualTo(1)
        assertThat(cases.first()).isEqualTo(newCaseEntity)
    }

    @Test
    fun `GIVEN case does exist WHEN insertCases called with same date and area code THEN case is updated`() {

        val oldCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().insertAll(listOf(oldCaseEntity))

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().insertAll(listOf(newCaseEntity))

        val cases = db.casesDao().searchAllCases(oldCaseEntity.areaCode)

        assertThat(cases.size).isEqualTo(1)
        assertThat(cases[0]).isEqualTo(newCaseEntity)
    }

    @Test
    fun `GIVEN case does exist WHEN insertCases called with same area code and different date THEN case is added`() {

        val oldCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().insertAll(listOf(oldCaseEntity))

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(1),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().insertAll(listOf(newCaseEntity))

        val cases = db.casesDao().searchAllCases(oldCaseEntity.areaCode)

        assertThat(cases.size).isEqualTo(2)
        assertThat(cases[0]).isEqualTo(oldCaseEntity)
        assertThat(cases[1]).isEqualTo(newCaseEntity)
    }

    @Test
    fun `GIVEN case does not exist WHEN insertCases called THEN case area created`() {

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().insertAll(listOf(newCaseEntity))

        val cases = db.casesDao().searchAllAreas(newCaseEntity.areaName)

        assertThat(cases.size).isEqualTo(1)
        assertThat(cases.first()).isEqualTo(
            AreaTupleEntity(
                newCaseEntity.areaCode,
                newCaseEntity.areaName
            )
        )
    }

    @Test
    fun `GIVEN case does exist WHEN insertCases called with same area code and area name THEN single area case is created`() {

        val oldCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().insertAll(listOf(oldCaseEntity))

        val newCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(1),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().insertAll(listOf(newCaseEntity))

        val cases = db.casesDao().searchAllAreas(oldCaseEntity.areaName)

        assertThat(cases.size).isEqualTo(1)
        assertThat(cases[0]).isEqualTo(AreaTupleEntity(newCaseEntity.areaCode, newCaseEntity.areaName))
    }

    @Test
    fun `GIVEN case does exist WHEN insertCases called with different area code and area name THEN new area case is created`() {

        val oldCaseEntity = CaseEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 9,
            totalLabConfirmedCases = 9,
            dailyTotalLabConfirmedCasesRate = 9.0
        )

        db.casesDao().insertAll(listOf(oldCaseEntity))

        val newCaseEntity = CaseEntity(
            areaCode = "002",
            areaName = "UK",
            date = Date(1),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0
        )

        db.casesDao().insertAll(listOf(newCaseEntity))

        val cases = db.casesDao().searchAllAreas(oldCaseEntity.areaName)

        assertThat(cases.size).isEqualTo(2)
        assertThat(cases[0]).isEqualTo(AreaTupleEntity(oldCaseEntity.areaCode, oldCaseEntity.areaName))
        assertThat(cases[1]).isEqualTo(AreaTupleEntity(newCaseEntity.areaCode, newCaseEntity.areaName))
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
