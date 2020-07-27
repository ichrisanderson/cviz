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
import com.chrisa.covid19.core.data.db.MetadataEntity.Companion.CASE_METADATA_ID
import com.chrisa.covid19.core.data.db.MetadataEntity.Companion.DEATH_METADATA_ID
import com.chrisa.covid19.core.util.test
import com.google.common.truth.Truth.assertThat
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

        val newMetadata = MetadataEntity(
            id = CASE_METADATA_ID,
            disclaimer = "New metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        db.metadataDao().insertAll(listOf(newMetadata))

        val metadata = db.metadataDao().metadata(CASE_METADATA_ID)

        assertThat(metadata.size).isEqualTo(1)
        assertThat(metadata.first()).isEqualTo(newMetadata)
    }

    @Test
    fun `GIVEN metadata exists WHEN insertMetadata called with same metadata is THEN metadata is updated`() {

        val oldMetadata = MetadataEntity(
            id = CASE_METADATA_ID,
            disclaimer = "Old Metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )
        db.metadataDao().insertAll(listOf(oldMetadata))

        val newMetadata = MetadataEntity(
            id = CASE_METADATA_ID,
            disclaimer = "New metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        db.metadataDao().insertAll(listOf(newMetadata))

        val metadata = db.metadataDao().metadata(CASE_METADATA_ID)

        assertThat(metadata.size).isEqualTo(1)
        assertThat(metadata.first()).isEqualTo(newMetadata)
    }

    @Test
    fun `GIVEN no case metadata WHEN casesMetadata called THEN metadata is null`() {
        val casesMetadata = db.metadataDao().metadata(CASE_METADATA_ID)
        assertThat(casesMetadata).isEmpty()
    }

    @Test
    fun `GIVEN case metadata exists WHEN casesMetadata called THEN metadata is not null`() {
        val metadata = MetadataEntity(
            id = CASE_METADATA_ID,
            disclaimer = "test Metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )
        db.metadataDao().insertAll(listOf(metadata))

        val casesMetadata = db.metadataDao().metadata(CASE_METADATA_ID)
        assertThat(casesMetadata).isEqualTo(listOf(metadata))
    }

    @Test
    fun `GIVEN no death metadata WHEN deathsMetadata called THEN death metadata is null`() {
        val deathsMetadata = db.metadataDao().metadata(DEATH_METADATA_ID)
        assertThat(deathsMetadata).isEmpty()
    }

    @Test
    fun `GIVEN death metadata exists WHEN deathsMetadata called THEN death metadata is not null`() {
        val metadata = MetadataEntity(
            id = DEATH_METADATA_ID,
            disclaimer = "Test Metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )
        db.metadataDao().insertAll(listOf(metadata))

        val deathsMetadata = db.metadataDao().metadata(DEATH_METADATA_ID)
        assertThat(deathsMetadata).isEqualTo(listOf(metadata))
    }

    @Test
    fun `GIVEN metadata exists WHEN metadataAsFlow called THEN entity is emitted`() = runBlocking {
        val metadata = MetadataEntity(
            id = CASE_METADATA_ID,
            disclaimer = "test Metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        db.metadataDao().metadataAsFlow(CASE_METADATA_ID).test {
            expectNoEvents()
            db.metadataDao().insertAll(listOf(metadata))

            val casesMetadata = expectItem()
            assertThat(casesMetadata).isEqualTo(metadata)

            cancel()
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
