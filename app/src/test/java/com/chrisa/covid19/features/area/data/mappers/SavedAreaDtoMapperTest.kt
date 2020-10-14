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

import com.chrisa.covid19.core.data.db.SavedAreaEntity
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.chrisa.covid19.features.area.data.mappers.SavedAreaDtoMapper.toSavedAreaEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SavedAreaDtoMapperTest {
    @Test
    fun `WHEN toSavedAreaEntity called THEN entity is created with correct details`() {
        val dto = SavedAreaDto(areaCode = "1234")

        assertThat(dto.toSavedAreaEntity()).isEqualTo(SavedAreaEntity(areaCode = dto.areaCode))
    }
}
