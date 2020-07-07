package com.chrisa.covid19.features.area.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import com.chrisa.covid19.core.util.test
import com.chrisa.covid19.features.area.domain.AreaUseCase
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.presentation.mappers.AreaUiModelMapper
import com.chrisa.covid19.features.area.presentation.models.AreaUiModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import java.util.Date

class AreaViewModelTest {

    @Rule
    @JvmField
    val liveDataJunitRule = InstantTaskExecutorRule()

    private val areaUseCase = mockk<AreaUseCase>()
    private val areaUiModelMapper = mockk<AreaUiModelMapper>()
    private val testDispatcher = TestCoroutineDispatcher()

    @Test
    fun `GIVEN bootstap succeeds WHEN viewmodel initialized THEN success state emitted`() =
        testDispatcher.runBlockingTest {
            pauseDispatcher {

                val areaCode = "AC-001"
                val savedStateHandle = SavedStateHandle(mapOf("areaCode" to areaCode))

                val caseModels = listOf(
                    CaseModel(
                        date = Date(0),
                        dailyLabConfirmedCases = 123
                    )
                )

                val areaDetailModel = AreaDetailModel(
                    lastUpdatedAt = Date(0),
                    allCases = caseModels,
                    latestCases = caseModels.takeLast(7)
                )

                val areaUiModel = AreaUiModel(
                    lastUpdatedAt = Date(0),
                    allCasesChartData = BarChartData(
                        label = "All cases",
                        values = emptyList()
                    ),
                    latestCasesChartData = BarChartData(
                        label = "Latest cases",
                        values = emptyList()
                    )
                )

                every { areaUseCase.execute(areaCode) } returns areaDetailModel
                every { areaUiModelMapper.mapAreaDetailModel(areaDetailModel) } returns areaUiModel

                val sut = AreaViewModel(
                    areaUseCase,
                    TestCoroutineDispatchersImpl(testDispatcher),
                    areaUiModelMapper,
                    savedStateHandle
                )

                val statesObserver = sut.state.test()

                runCurrent()

                assertThat(statesObserver.values[0]).isEqualTo(AreaState.Loading)
                assertThat(statesObserver.values[1]).isEqualTo(AreaState.Success(areaUiModel))
            }
        }
}

