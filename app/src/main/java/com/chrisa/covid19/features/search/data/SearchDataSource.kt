package com.chrisa.covid19.features.search.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.features.search.data.dtos.AreaDTO
import javax.inject.Inject

class SearchDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val searchQueryTransformer: SearchQueryTransformer
) {
    fun searchAreas(query: String): List<AreaDTO> {
        return appDatabase.casesDao()
            .searchAllAreas(searchQueryTransformer.transformQuery(query))
            .map { AreaDTO(it.areaCode, it.areaName) }
    }
}

