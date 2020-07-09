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

package com.chrisa.covid19.features.search.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.search.domain.SearchUseCase
import com.chrisa.covid19.features.search.domain.models.AreaModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val searchUseCase = mockk<SearchUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val sut = SearchViewModel(
        searchUseCase,
        TestCoroutineDispatchersImpl(testDispatcher)
    )

    @Test
    fun `GIVEN search call succeeds with empty results WHEN searchAreas called THEN empty state is emitted`() =
        testDispatcher.runBlockingTest {

            val query = "London"

            coEvery { searchUseCase.execute(query) } returns listOf()

            val statesObserver = sut.state.test()

            sut.searchAreas(query)

            assertThat(statesObserver.values[0]).isEqualTo(SearchState.Loading)
            assertThat(statesObserver.values[1]).isEqualTo(SearchState.Empty)
        }

    @Test
    fun `GIVEN search call succeeds with non empty results WHEN searchAreas called THEN success state is emitted`() =
        testDispatcher.runBlockingTest {

            val query = "London"

            val area =
                AreaModel(
                    code = "001",
                    name = "London"
                )

            val expectedResults = listOf(area)

            coEvery { searchUseCase.execute(query) } returns expectedResults

            val statesObserver = sut.state.test()

            sut.searchAreas(query)

            assertThat(statesObserver.values[0]).isEqualTo(SearchState.Loading)
            assertThat(statesObserver.values[1]).isEqualTo(SearchState.Success(expectedResults))
        }
}
