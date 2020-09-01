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

package com.chrisa.covid19.features.home.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.home.domain.LoadHomeDataUseCase
import com.chrisa.covid19.features.home.domain.models.HomeScreenDataModel
import com.chrisa.covid19.features.home.domain.models.InfectionRateModel
import com.chrisa.covid19.features.home.domain.models.LatestUkDataModel
import com.chrisa.covid19.features.home.domain.models.NewCaseModel
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@InternalCoroutinesApi
@FlowPreview
@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val loadHomeDataUseCase = mockk<LoadHomeDataUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN load home data cases succeeds WHEN viewmodel initialized THEN home screen data is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val savedArea = SavedAreaModel(
                    areaCode = "UK001",
                    areaName = "England",
                    areaType = "utla",
                    dailyTotalLabConfirmedCasesRate = 111.0,
                    totalLabConfirmedCases = 22,
                    changeInDailyTotalLabConfirmedCasesRate = 11.0,
                    changeInTotalLabConfirmedCases = 11,
                    totalLabConfirmedCasesLastWeek = 11
                )

                val latestUkData = LatestUkDataModel(
                    areaName = "England",
                    totalLabConfirmedCases = 22,
                    dailyLabConfirmedCases = 33,
                    lastUpdated = LocalDateTime.of(2020, 1, 1, 1, 1)
                )

                val newCaseModel = NewCaseModel(
                    areaCode = "1234",
                    areaName = "Lambeth",
                    areaType = "ltla",
                    changeInCases = 22,
                    currentNewCases = 33
                )

                val infectionRateModel = InfectionRateModel(
                    areaName = "UK",
                    areaCode = Constants.UK_AREA_CODE,
                    areaType = AreaType.OVERVIEW.value,
                    changeInInfectionRate = 100.0,
                    currentInfectionRate = 10.0
                )

                val homeScreenDataModel = HomeScreenDataModel(
                    savedAreas = listOf(savedArea),
                    latestUkData = listOf(latestUkData),
                    topInfectionRates = listOf(infectionRateModel),
                    risingInfectionRates = listOf(infectionRateModel),
                    risingNewCases = listOf(newCaseModel),
                    topNewCases = listOf(newCaseModel)
                )

                every { loadHomeDataUseCase.execute() } returns listOf(homeScreenDataModel).asFlow()

                val sut = HomeViewModel(
                    loadHomeDataUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher)
                )

                val homeScreenDataObserver = sut.homeScreenData.test()
                val isLoadingObserver = sut.isLoading.test()

                runCurrent()

                assertThat(homeScreenDataObserver.values[0]).isEqualTo(homeScreenDataModel)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
            }
        }
}
