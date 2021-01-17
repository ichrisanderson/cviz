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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class AreaDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no areas WHEN count called THEN count is 0`() {
        val count = db.areaDao().count()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `GIVEN areas exist WHEN count called THEN count is not zero`() {
        db.areaDao().insertAll(
            listOf(
                AreaEntity(
                    areaCode = "1234",
                    areaName = Constants.UK_AREA_NAME,
                    areaType = AreaType.OVERVIEW
                )
            )
        )

        val count = db.areaDao().count()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN areas does not exist WHEN search called THEN an empty list is returned`() {
        db.areaDao().insertAll(
            listOf(
                AreaEntity(
                    areaCode = "1234",
                    areaName = Constants.UK_AREA_NAME,
                    areaType = AreaType.OVERVIEW
                )
            )
        )

        val items = db.areaDao().search("London")
        assertThat(items).isEqualTo(listOf<AreaEntity>())
    }

    @Test
    fun `GIVEN areas does exist WHEN search called THEN a list is returned`() {

        val allAreas = listOf(
            AreaEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaName = Constants.UK_AREA_NAME,
                areaType = AreaType.OVERVIEW
            )
        )

        db.areaDao().insertAll(allAreas)

        val items = db.areaDao().search(allAreas.first().areaName)
        assertThat(items).isEqualTo(allAreas)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
