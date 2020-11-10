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
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val searchDataSource: SearchDataSource
) {
    fun execute(query: String): List<AreaModel> {
        if (query.isBlank()) return emptyList()
        return searchDataSource.searchAreas(query)
            .map { AreaModel(it.code, it.name, it.type) }
    }
}
