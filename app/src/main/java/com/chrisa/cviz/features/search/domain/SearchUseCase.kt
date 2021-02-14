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
import com.chrisa.cviz.features.search.domain.models.AreaModel
import java.util.Locale
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val searchDataSource: SearchDataSource
) {
    suspend fun execute(query: String): List<AreaModel> {
        if (query.isBlank()) return emptyList()
        val formattedQuery = query.filter { !it.isWhitespace() }
        if (formattedQuery.isNotEmpty() && formattedQuery.matches(postcodeRegex)) {
            val lookup = searchDataSource.searchPostcode(formattedQuery.toUpperCase(Locale.UK))
            if (lookup != null) {
                return listOf(AreaModel(lookup.code, lookup.name, lookup.type))
            }
        }
        return searchDataSource.searchAreas(query)
            .map { AreaModel(it.code, it.name, it.type) }
    }

    companion object {
        private const val postcodeRegexPattern =
            "^[a-z]{1,2}\\d[a-z\\d]?\\s*\\d[a-z]{2}\$"
        private val postcodeRegex = Regex(postcodeRegexPattern, RegexOption.IGNORE_CASE)
    }
}
