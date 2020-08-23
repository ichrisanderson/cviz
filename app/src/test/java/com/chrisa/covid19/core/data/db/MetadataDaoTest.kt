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
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class MetadataDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no metadata exists WHEN insertMetadata called THEN metadata is inserted`() {

        val lastUpdated = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        val lastSynced = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneOffset.UTC)

        val newMetadata = MetadataEntity(
            id = METADATA_ID,
            lastUpdatedAt = lastUpdated,
            lastSyncTime = lastSynced
        )

        db.metadataDao().insert(newMetadata)

        val metadata = db.metadataDao().metadata(METADATA_ID)

        assertThat(metadata).isEqualTo(newMetadata)
    }

    @Test
    fun `GIVEN metadata exists WHEN insertMetadata called with same metadata is THEN metadata is updated`() {

        val lastUpdated = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        val lastSynced = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneOffset.UTC)

        val oldMetadata = MetadataEntity(
            id = METADATA_ID,
            lastUpdatedAt = lastUpdated,
            lastSyncTime = lastSynced
        )
        db.metadataDao().insert(oldMetadata)

        val newMetadata = MetadataEntity(
            id = METADATA_ID,
            lastUpdatedAt = lastUpdated,
            lastSyncTime = lastSynced
        )

        db.metadataDao().insert(newMetadata)

        val metadata = db.metadataDao().metadata(METADATA_ID)

        assertThat(metadata).isEqualTo(newMetadata)
    }

    @Test
    fun `GIVEN no metadata WHEN metadata called THEN metadata is null`() {
        val metadata = db.metadataDao().metadata(METADATA_ID)
        assertThat(metadata).isNull()
    }

    @Test
    fun `GIVEN metadata data exists WHEN deleteAllNotInIds called THEN metadata with id is not deleted`() {

        val lastUpdated = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        val lastSynced = LocalDateTime.ofInstant(Instant.ofEpochMilli(1), ZoneOffset.UTC)

        val newMetadata = MetadataEntity(
            id = METADATA_ID,
            lastUpdatedAt = lastUpdated,
            lastSyncTime = lastSynced
        )

        val idToRetain = METADATA_ID + "_retained"

        db.metadataDao().insert(newMetadata)
        db.metadataDao().insert(newMetadata.copy(id = idToRetain))

        assertThat(db.metadataDao().metadata(METADATA_ID)).isNotNull()
        assertThat(db.metadataDao().metadata(idToRetain)).isNotNull()

        db.metadataDao().deleteAllNotInIds(listOf(idToRetain))

        assertThat(db.metadataDao().metadata(METADATA_ID)).isNull()
        assertThat(db.metadataDao().metadata(idToRetain)).isNotNull()
    }

    @Test
    fun `GIVEN saved area does not exist WHEN insert called THEN new entity is inserted`() =
        runBlocking {

            val metadataEntity = MetadataEntity(
                id = "1234",
                lastSyncTime = LocalDateTime.now(),
                lastUpdatedAt = LocalDateTime.now()
            )

            db.metadataDao().metadataAsFlow(metadataEntity.id).test {
                expectNoEvents()

                assertThat(expectItem()).isEqualTo(null)

                db.metadataDao().insert(metadataEntity)
                assertThat(expectItem()).isEqualTo(metadataEntity)

                cancel()
            }
        }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    companion object {
        private const val METADATA_ID = "1234"
    }
}
