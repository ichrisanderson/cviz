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
import com.chrisa.covid19.core.data.db.CaseEntity
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class HomeDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val sut = HomeDataSource(appDatabase)

    @Test
    fun `WHEN savedAreaCases called THEN savedAreaCase Dtos are returned`() = runBlockingTest {

        val caseEntity = CaseEntity(
            areaCode = "1234",
            areaName = "London",
            date = LocalDate.ofEpochDay(0),
            dailyLabConfirmedCases = 222,
            dailyTotalLabConfirmedCasesRate = 122.0,
            totalLabConfirmedCases = 122
        )

        val allCases = listOf(
            caseEntity,
            caseEntity.copy(areaCode = "1111", areaName = "England")
        )
        val allCasesFlow = flow { emit(allCases) }

        val allSavedAreaDtos = allCases.map {
            SavedAreaCaseDto(
                areaCode = it.areaCode,
                areaName = it.areaName,
                dailyLabConfirmedCases = it.dailyLabConfirmedCases,
                totalLabConfirmedCases = it.totalLabConfirmedCases,
                dailyTotalLabConfirmedCasesRate = it.dailyTotalLabConfirmedCasesRate,
                date = it.date
            )
        }

        every {
            appDatabase.casesDao().savedAreaCases()
        } returns allCasesFlow

        val emittedItems = mutableListOf<List<SavedAreaCaseDto>>()

        sut.savedAreaCases().collect { emittedItems.add(it) }

        assertThat(emittedItems.size).isEqualTo(1)
        assertThat(emittedItems.first()).isEqualTo(allSavedAreaDtos)
    }
}
