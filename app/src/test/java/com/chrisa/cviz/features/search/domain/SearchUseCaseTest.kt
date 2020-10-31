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

package com.chrisa.cviz.features.search.domain

import com.chrisa.cviz.features.search.data.SearchDataSource
import com.chrisa.cviz.features.search.data.dtos.AreaDTO
import com.chrisa.cviz.features.search.domain.models.AreaModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class SearchUseCaseTest {

    private val searchDataSource = mockk<SearchDataSource>()

    private val sut = SearchUseCase(searchDataSource)

    @Test
    fun `WHEN execute called THEN casesDao searchs for area`() {

        val area = AreaDTO(
            code = "1234",
            name = "London",
            type = "utla"
        )

        val expectedResults = listOf(area)

        every { searchDataSource.searchAreas(area.name) } returns expectedResults

        val results = sut.execute(area.name)

        assertThat(results).isEqualTo(expectedResults.map {
            AreaModel(
                it.code,
                it.name,
                it.type
            )
        })
    }
}
