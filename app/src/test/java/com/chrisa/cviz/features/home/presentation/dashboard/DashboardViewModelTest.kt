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

package com.chrisa.cviz.features.home.presentation.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.cviz.core.util.test
import com.chrisa.cviz.features.home.domain.LoadDashboardDataUseCase
import com.chrisa.cviz.features.home.domain.RefreshDataUseCase
import com.chrisa.cviz.features.home.domain.models.DashboardDataModel
import com.chrisa.cviz.features.home.domain.models.LatestUkDataModel
import com.chrisa.cviz.features.home.domain.models.SummaryModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
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
class DashboardViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val loadHomeDataUseCase = mockk<LoadDashboardDataUseCase>()
    private val refreshDataUseCase = mockk<RefreshDataUseCase>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val coroutineDispatchers = TestCoroutineDispatchersImpl(testDispatcher)

    @Test
    fun `GIVEN load home data cases succeeds WHEN viewmodel initialized THEN home screen data is emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val latestUkData = LatestUkDataModel(
                    areaCode = Constants.UK_AREA_CODE,
                    areaName = "UK",
                    areaType = AreaType.OVERVIEW.value,
                    cumulativeCases = 22,
                    newCases = 33,
                    lastUpdated = LocalDateTime.of(2020, 1, 1, 1, 1)
                )

                val summaryModel = SummaryModel(
                    position = 1,
                    areaCode = "1234",
                    areaName = "Lambeth",
                    areaType = "ltla",
                    changeInCases = 22,
                    currentNewCases = 33,
                    changeInInfectionRate = 100.0,
                    currentInfectionRate = 10.0
                )

                val homeScreenDataModel = DashboardDataModel(
                    latestUkData = listOf(latestUkData),
                    topInfectionRates = listOf(summaryModel),
                    risingInfectionRates = listOf(summaryModel),
                    risingNewCases = listOf(summaryModel),
                    topNewCases = listOf(summaryModel)
                )

                every { loadHomeDataUseCase.execute() } returns listOf(homeScreenDataModel).asFlow()

                val sut = DashboardViewModel(
                    loadHomeDataUseCase,
                    refreshDataUseCase,
                    coroutineDispatchers
                )

                val homeScreenDataObserver = sut.dashboardData.test()
                val isLoadingObserver = sut.isLoading.test()

                runCurrent()

                assertThat(homeScreenDataObserver.values[0]).isEqualTo(homeScreenDataModel)
                assertThat(isLoadingObserver.values[0]).isEqualTo(true)
                assertThat(isLoadingObserver.values[1]).isEqualTo(false)
            }
        }

    @Test
    fun `WHEN viewmodel refreshed THEN refresh state is emitted`() =
        testDispatcher.runBlockingTest {
                every { loadHomeDataUseCase.execute() } returns emptyList<DashboardDataModel>().asFlow()
                coEvery { refreshDataUseCase.execute() } just Runs
                val sut = DashboardViewModel(
                    loadHomeDataUseCase,
                    refreshDataUseCase,
                    coroutineDispatchers
                )
                val isRefreshingObserver = sut.isRefreshing.test()

                sut.refresh()

                assertThat(isRefreshingObserver.values[0]).isEqualTo(true)
                assertThat(isRefreshingObserver.values[1]).isEqualTo(false)
        }
}
