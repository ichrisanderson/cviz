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

package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.MetadataModel
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Test

class MetadataModelMapperTest {

    private val sut = MetadataModelMapper()

    @Test
    fun `WHEN mapToMetadataEntity called THEN metadataEntity is returned`() {

        val id = "Foo"
        val metadataModel = MetadataModel(
            disclaimer = "New metadata",
            lastUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneOffset.UTC)
        )

        val entity = sut.mapToMetadataEntity(id, metadataModel)

        assertThat(entity).isEqualTo(
            MetadataEntity(
                id = id,
                disclaimer = metadataModel.disclaimer,
                lastUpdatedAt = metadataModel.lastUpdatedAt
            )
        )
    }
}
