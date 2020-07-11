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

package com.chrisa.covid19.features.search.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaTupleEntity
import com.chrisa.covid19.features.search.data.dtos.AreaDTO
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class SearchDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val queryTransformer = SearchQueryTransformer()

    private val sut = SearchDataSource(appDatabase, queryTransformer)

    @Test
    fun `WHEN execute called THEN casesDao searches for area`() {

        val area = AreaTupleEntity(
            areaCode = "1234",
            areaName = "London"
        )

        val areaNameAsQuery = queryTransformer.transformQuery(area.areaName)
        val expectedResults = listOf(area)

        every { appDatabase.casesDao().searchAllAreas(areaNameAsQuery) } returns expectedResults

        val results = sut.searchAreas(area.areaName)

        assertThat(results).isEqualTo(expectedResults.map {
            AreaDTO(
                it.areaCode,
                it.areaName
            )
        })
    }
}
