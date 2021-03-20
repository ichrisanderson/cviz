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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class SavedAreaDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        db.areaDao().insertAll(
            listOf(
                AreaEntity(
                    areaCode = "1234",
                    areaName = "1234",
                    areaType = AreaType.UTLA
                ),
                AreaEntity(
                    areaCode = "2345",
                    areaName = "2345",
                    areaType = AreaType.UTLA
                )
            )
        )
    }

    @Test
    fun `GIVEN saved area does not exist WHEN searchSavedAreas called THEN null entity is returned`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(
                areaCode = "1234"
            )

            db.savedAreaDao().isSaved(areaEntity.areaCode).test {
                expectNoEvents()
                assertThat(expectItem()).isEqualTo(false)
                cancel()
            }
        }

    @Test
    fun `GIVEN saved area does not exist WHEN insert called THEN new entity is inserted`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(
                areaCode = "1234"
            )

            db.savedAreaDao().isSaved(areaEntity.areaCode).test {
                expectNoEvents()

                assertThat(expectItem()).isEqualTo(false)

                db.savedAreaDao().insert(areaEntity)
                assertThat(expectItem()).isEqualTo(true)

                cancel()
            }
        }

    @Test(expected = TimeoutCancellationException::class)
    fun `GIVEN saved area does exist WHEN insert called THEN last insert is ignored`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(areaCode = "1234")

            db.savedAreaDao().isSaved(areaEntity.areaCode).test {
                expectNoEvents()

                assertThat(expectItem()).isEqualTo(false)

                db.savedAreaDao().insert(areaEntity)

                assertThat(expectItem()).isEqualTo(true)

                db.savedAreaDao().insert(areaEntity)

                expectItem() // item should timeout as 2nd insert is ignored

                cancel()
            }
        }

    @Test
    fun `GIVEN area is saved and removed WHEN searchSavedAreas is observing THEN changes are emitted`() =
        runBlocking {

            val areaEntity = SavedAreaEntity(areaCode = "1234")

            db.savedAreaDao().isSaved(areaEntity.areaCode).test {
                expectNoEvents()

                assertThat(expectItem()).isEqualTo(false)

                db.savedAreaDao().insert(areaEntity)

                assertThat(expectItem()).isEqualTo(true)

                db.savedAreaDao().delete(areaEntity)

                assertThat(expectItem()).isEqualTo(false)

                cancel()
            }
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
