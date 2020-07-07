package com.chrisa.covid19.features.search.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.search.domain.models.AreaModel
import com.chrisa.covid19.features.search.domain.SearchUseCase
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
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
