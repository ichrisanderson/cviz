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
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class SavedAreaDaoTest {

    private lateinit var db: AppDatabase
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN saved area does not exist WHEN searchSavedAreas called THEN null entity is returned`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(
                areaCode = "1234"
            )

            val values = mutableListOf<SavedAreaEntity?>()
            val latch = CountDownLatch(1)

            val searchJob = async(Dispatchers.IO) {
                db.savedAreaDao().searchSavedAreas(areaEntity.areaCode)
                    .collect {
                        values.add(it)
                        latch.countDown()
                    }
            }

            latch.await()
            searchJob.cancelAndJoin()
            assertThat(values.first()).isNull()
        }

    @Test
    fun `GIVEN saved area does not exist WHEN insert called THEN new entity is inserted`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(
                areaCode = "1234"
            )

            db.savedAreaDao().insert(areaEntity)

            val values = mutableListOf<SavedAreaEntity?>()
            val latch = CountDownLatch(1)

            val searchJob = async(Dispatchers.IO) {
                db.savedAreaDao().searchSavedAreas(areaEntity.areaCode)
                    .collect {
                        values.add(it)
                        latch.countDown()
                    }
            }

            latch.await()
            searchJob.cancelAndJoin()

            assertThat(values.first()).isEqualTo(areaEntity)
        }

    @Test
    fun `GIVEN saved area does exist WHEN insert called THEN last insert is ignored`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(
                areaCode = "1234"
            )

            val expectedEmissions = 2
            val values = mutableListOf<SavedAreaEntity?>()
            val latches = (1..expectedEmissions).map { CountDownLatch(1) }

            val searchJob = async(Dispatchers.IO) {
                db.savedAreaDao().searchSavedAreas(areaEntity.areaCode)
                    .collectIndexed { index, value ->
                        if (index < latches.size) {
                            values.add(value)
                            latches[index].countDown()
                        } else {
                            fail("Unexpected result.")
                        }
                    }
            }

            latches[0].await()
            assertThat(values[0]).isEqualTo(null)

            db.savedAreaDao().insert(areaEntity)

            latches[1].await()
            assertThat(values[1]).isEqualTo(areaEntity)

            db.savedAreaDao().insert(areaEntity)

            searchJob.cancelAndJoin()
        }

    @Test
    fun `GIVEN area is saved and removed WHEN searchSavedAreas is observing THEN changes are emitted`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(areaCode = "1234")

            val expectedEmissions = 3
            val values = mutableListOf<SavedAreaEntity?>()
            val latches = (1..expectedEmissions).map { CountDownLatch(1) }

            val searchJob = async(Dispatchers.IO) {
                db.savedAreaDao().searchSavedAreas(areaEntity.areaCode)
                    .collectIndexed { index, value ->
                        if (index < latches.size) {
                            values.add(value)
                            latches[index].countDown()
                        } else {
                            fail("Unexpected result.")
                        }
                    }
            }

            latches[0].await()
            assertThat(values[0]).isEqualTo(null)

            db.savedAreaDao().insert(areaEntity)

            latches[1].await()
            assertThat(values[1]).isEqualTo(areaEntity)

            db.savedAreaDao().delete(areaEntity)

            latches[2].await()
            assertThat(values[2]).isEqualTo(null)

            searchJob.cancelAndJoin()
        }

    @Test
    fun `GIVEN saved area does exist WHEN delete called THEN area is deleted`() {

        val areaEntity = SavedAreaEntity(
            areaCode = "2345"
        )
        db.savedAreaDao().insert(areaEntity)

        val deletedRows = db.savedAreaDao().delete(areaEntity)
        assertThat(deletedRows).isEqualTo(1)
    }

    @Test
    fun `GIVEN saved area does not exist WHEN delete called THEN delete is ignored`() {

        val areaEntity = SavedAreaEntity(
            areaCode = "2345"
        )

        val deletedRows = db.savedAreaDao().delete(areaEntity)
        assertThat(deletedRows).isEqualTo(0)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
