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

package com.chrisa.covid19.features.home.domain

import com.chrisa.covid19.features.home.data.HomeDataSource
import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.AreaCase
import com.chrisa.covid19.features.home.domain.models.AreaCaseList
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class LoadSavedAreaCasesUseCaseTest {

    private val homeDataSource = mockk<HomeDataSource>()
    private val sut = LoadSavedAreaCasesUseCase(homeDataSource)

    @Test
    fun `WHEN execute called THEN area case list is created and emitted`() =
        runBlockingTest {

            val ukFirstCase = SavedAreaCaseDto(
                areaCode = "001",
                areaName = "UK",
                date = LocalDate.ofEpochDay(0),
                dailyLabConfirmedCases = 12
            )

            val ukSecondCase = ukFirstCase.copy(date = LocalDate.ofEpochDay(1))
            val englandFirstCase = ukFirstCase.copy(areaCode = "002", areaName = "England")

            val allCases = listOf(ukFirstCase, englandFirstCase, ukSecondCase)
            val allCasesFlow = flow {
                emit(allCases)
            }

            every { homeDataSource.savedAreaCases() } returns allCasesFlow

            val emittedItems = mutableListOf<List<AreaCaseList>>()

            sut.execute().collect { emittedItems.add(it) }

            assertThat(emittedItems.size).isEqualTo(1)

            val enittedAreaCaseList = emittedItems.first()

            assertThat(enittedAreaCaseList[0]).isEqualTo(
                AreaCaseList(
                    areaName = englandFirstCase.areaName,
                    areaCode = englandFirstCase.areaCode,
                    cases = listOf(
                        AreaCase(
                            date = englandFirstCase.date,
                            dailyLabConfirmedCases = englandFirstCase.dailyLabConfirmedCases
                        )
                    )
                )
            )
            assertThat(enittedAreaCaseList[1]).isEqualTo(
                AreaCaseList(
                    areaName = ukFirstCase.areaName,
                    areaCode = ukFirstCase.areaCode,
                    cases = listOf(
                        AreaCase(
                            date = ukFirstCase.date,
                            dailyLabConfirmedCases = ukFirstCase.dailyLabConfirmedCases
                        ), AreaCase(
                            date = ukSecondCase.date,
                            dailyLabConfirmedCases = ukSecondCase.dailyLabConfirmedCases
                        )
                    )
                )
            )
        }
}
