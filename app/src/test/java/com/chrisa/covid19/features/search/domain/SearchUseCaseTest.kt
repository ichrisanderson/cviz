package com.chrisa.covid19.features.search.domain

import com.chrisa.covid19.features.search.data.dtos.AreaDTO
import com.chrisa.covid19.features.search.data.SearchDataSource
import com.chrisa.covid19.features.search.domain.models.AreaModel
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
            name = "London"
        )

        val expectedResults = listOf(area)

        every { searchDataSource.searchAreas(area.name) } returns expectedResults

        val results = sut.execute(area.name)

        assertThat(results).isEqualTo(expectedResults.map {
            AreaModel(
                it.code,
                it.name
            )
        })
    }
}
