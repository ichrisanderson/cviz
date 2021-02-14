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

package com.chrisa.cviz.features.search.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.PostcodeLookupDataSynchroniser
import com.chrisa.cviz.features.search.data.dtos.AreaDTO
import javax.inject.Inject

class SearchDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val postcodeLookupDataSynchroniser: PostcodeLookupDataSynchroniser,
    private val searchQueryTransformer: SearchQueryTransformer
) {
    fun searchAreas(query: String): List<AreaDTO> {
        return appDatabase.areaDao()
            .search(searchQueryTransformer.transformQuery(query))
            .map { AreaDTO(it.areaCode, it.areaName, it.areaType.value) }
    }

    suspend fun searchPostcode(postcode: String): AreaDTO? {
        val lookup = areaLookupData(postcode)
        return lookup?.msoaName?.let { AreaDTO(lookup.msoaCode, it, AreaType.MSOA.value) }
    }

    private suspend fun areaLookupData(postcode: String) =
        try {
            postcodeLookupDataSynchroniser.performSync(postcode)
            appDatabase.areaLookupDao().byTrimmedPostcode(postcode)
        } catch (e: Throwable) {
            null
        }
}
