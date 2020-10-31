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

package com.chrisa.cviz.features.search.presentation

import com.chrisa.cviz.features.search.domain.models.AreaModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaDiffCallbackTest {

    private val sut = AreaDiffCallback()

    @Test
    fun `GIVEN area codes are the same WHEN items are compared THEN areItemsTheSame returns true`() {

        val item1 = AreaModel(
            code = "001",
            name = "London",
            type = "utla"
        )
        val item2 = AreaModel(
            code = "001",
            name = "Birmingham",
            type = "utla"
        )

        assertThat(sut.areItemsTheSame(item1, item2)).isEqualTo(true)
    }

    @Test
    fun `GIVEN area codes are not the same WHEN items are compared THEN areItemsTheSame returns false`() {

        val item1 = AreaModel(
            "001",
            name = "London",
            type = "utla"
        )
        val item2 = AreaModel(
            "002",
            name = "Birmingham",
            type = "utla"
        )

        assertThat(sut.areItemsTheSame(item1, item2)).isEqualTo(false)
    }

    @Test
    fun `GIVEN contents are the same WHEN items are compared THEN areContentsTheSame returns true`() {

        val item1 = AreaModel(
            "001",
            name = "London",
            type = "utla"
        )
        val item2 = AreaModel(
            "001",
            name = "London",
            type = "utla"
        )

        assertThat(sut.areContentsTheSame(item1, item2)).isEqualTo(true)
    }

    @Test
    fun `GIVEN contents are the same WHEN items are compared THEN areContentsTheSame returns false`() {

        val item1 = AreaModel(
            "001",
            name = "London",
            type = "utla"
        )
        val item2 = AreaModel(
            "001",
            "Birmingham",
            type = "utla"
        )

        assertThat(sut.areContentsTheSame(item1, item2)).isEqualTo(false)
    }
}
