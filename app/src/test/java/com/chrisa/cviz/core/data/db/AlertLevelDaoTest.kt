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
import java.io.IOException
import java.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class AlertLevelDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no data exists WHEN insert called THEN data is inserted`() {
        db.alertLevelDao().insert(alertLevel)

        val alertLevelEntity = db.alertLevelDao().byAreaCode(areaCode)

        assertThat(alertLevelEntity).isEqualTo(alertLevel)
    }

    @Test
    fun `GIVEN data exists WHEN insert called with same metadata is THEN metadata is updated`() {
        db.alertLevelDao().insert(alertLevel)

        val newAlertLevel = alertLevel.copy(
            date = alertLevel.date.plusDays(1)
        )

        db.alertLevelDao().insert(newAlertLevel)

        val metadata = db.alertLevelDao().byAreaCode(areaCode)

        assertThat(metadata).isEqualTo(newAlertLevel)
    }

    @Test
    fun `GIVEN no data WHEN byAreaCode called THEN data is null`() {
        val data = db.alertLevelDao().byAreaCode(areaCode)
        assertThat(data).isNull()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    companion object {
        private val date = LocalDate.of(2020, 2, 3)
        private val areaCode = "1234"
        private val alertLevel = AlertLevelEntity(
            areaCode = areaCode,
            areaName = "",
            areaType = AreaType.LTLA,
            date = date,
            alertLevel = 1,
            alertLevelUrl = "http://www.acme.com",
            alertLevelName = "Level 1",
            alertLevelValue = 1
        )
    }
}
