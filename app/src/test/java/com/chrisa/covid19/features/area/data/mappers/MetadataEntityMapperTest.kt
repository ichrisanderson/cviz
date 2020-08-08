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

package com.chrisa.covid19.features.area.data.mappers

import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.features.area.data.dtos.MetadataDto
import com.chrisa.covid19.features.area.data.mappers.MetadataEntityMapper.toMetadataDto
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Test

class MetadataEntityMapperTest {

    @Test
    fun `WHEN toMetadataDto called THEN dto is created with correct details`() {
        val entity = MetadataEntity(
            id = "12234",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        assertThat(entity.toMetadataDto()).isEqualTo(
            MetadataDto(
                lastUpdatedAt = entity.lastUpdatedAt
            )
        )
    }
}
