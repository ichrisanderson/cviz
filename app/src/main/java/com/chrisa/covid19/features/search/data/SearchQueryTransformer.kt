package com.chrisa.covid19.features.search.data

import javax.inject.Inject

class SearchQueryTransformer @Inject constructor() {
    fun transformQuery(query: String): String = "$query%"
}
