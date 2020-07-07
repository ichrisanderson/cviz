package com.chrisa.covid19.features.search.domain

import com.chrisa.covid19.features.search.data.SearchDataSource
import com.chrisa.covid19.features.search.domain.models.AreaModel
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val searchDataSource: SearchDataSource
) {
    fun execute(query: String): List<AreaModel> {
        return searchDataSource.searchAreas(query)
            .map { AreaModel(it.code, it.name) }
    }
}

