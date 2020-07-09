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
