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

package com.chrisa.covid19.features.home.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun savedAreaCases(): Flow<List<SavedAreaCaseDto>> {
        return appDatabase.casesDao()
            .savedAreaCases()
            .map { casesList ->
                casesList.map {
                    SavedAreaCaseDto(
                        areaCode = it.areaCode,
                        areaName = it.areaName,
                        date = it.date,
                        dailyLabConfirmedCases = it.dailyLabConfirmedCases
                    )
                }
            }
    }
}
