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

package com.chrisa.covid19.features.area.presentation.mappers

import android.content.Context
import com.chrisa.covid19.R
import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.synchronisation.DailyDataWithRollingAverage
import com.chrisa.covid19.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.covid19.core.data.synchronisation.SynchronisationTestData
import com.chrisa.covid19.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.BarChartItem
import com.chrisa.covid19.core.ui.widgets.charts.CombinedChartData
import com.chrisa.covid19.core.ui.widgets.charts.LineChartData
import com.chrisa.covid19.core.ui.widgets.charts.LineChartItem
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.presentation.models.AreaDataModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class AreaDataModelMapperTest {

    private val context = mockk<Context>()
    private val dailyDataWithRollingAverageBuilder = mockk<DailyDataWithRollingAverageBuilder>()
    private val chartBuilder = mockk<ChartBuilder>()
    private val weeklySummaryBuilder = mockk<WeeklySummaryBuilder>()
    private val sut = AreaDataModelMapper(
        context,
        dailyDataWithRollingAverageBuilder,
        weeklySummaryBuilder,
        chartBuilder
    )

    @Before
    fun setup() {
        every { context.getString(R.string.all_cases_chart_label) } returns allCasesLabel
        every { context.getString(R.string.latest_cases_chart_label) } returns latestCasesLabel
        every { context.getString(R.string.all_deaths_chart_label) } returns allDeathsLabel
        every { context.getString(R.string.latest_deaths_chart_label) } returns latestDeathsLabel
        every { context.getString(R.string.rolling_average_chart_label) } returns rollingAverageLabel

        every { dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(any()) } returns
            listOf(dailyDataWithRollingAverage)

        every { weeklySummaryBuilder.buildWeeklySummary(any()) } returns weeklySummary

        every {
            chartBuilder.allChartData(
                allCasesLabel,
                latestCasesLabel,
                rollingAverageLabel,
                listOf(dailyDataWithRollingAverage)
            )
        } returns listOf(combinedChartData)
    }

    @Test
    fun `WHEN mapAreaDetailModel called without death data THEN deaths are hidden`() {
        every {
            chartBuilder.allChartData(
                allDeathsLabel,
                latestDeathsLabel,
                rollingAverageLabel,
                listOf(dailyDataWithRollingAverage)
            )
        } returns emptyList()

        val mappedModel = sut.mapAreaDetailModel(areaDetail)

        assertThat(mappedModel).isEqualTo(
            AreaDataModel(
                caseChartData = listOf(combinedChartData),
                caseSummary = weeklySummary,
                showDeaths = false,
                deathsChartData = emptyList(),
                deathSummary = weeklySummary
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with death data THEN deaths are shown`() {
        every {
            chartBuilder.allChartData(
                allDeathsLabel,
                latestDeathsLabel,
                rollingAverageLabel,
                any()
            )
        } returns
            listOf(combinedChartData)

        val mappedModel = sut.mapAreaDetailModel(areaDetail)

        assertThat(mappedModel).isEqualTo(
            AreaDataModel(
                caseChartData = listOf(combinedChartData),
                caseSummary = weeklySummary,
                showDeaths = true,
                deathsChartData = listOf(combinedChartData),
                deathSummary = weeklySummary
            )
        )
    }

    companion object {
        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)
        private val weeklySummary = SynchronisationTestData.bigWeeklySummary

        private const val allCasesLabel = "All cases"
        private const val latestCasesLabel = "Latest cases"
        private const val allDeathsLabel = "All deaths"
        private const val latestDeathsLabel = "Latest deaths"
        private const val rollingAverageLabel = "Rolling average"
        private const val barChartLabel = "bar chart"
        private const val lineChartLabel = "line chart"

        private val areaDetail = AreaDetailModel(
            areaType = AreaType.OVERVIEW.value,
            lastSyncedAt = syncDateTime,
            cases = SynchronisationTestData.dailyData(),
            deaths = SynchronisationTestData.dailyData(),
            hospitalAdmissions = SynchronisationTestData.dailyData(),
        )

        private val dailyDataWithRollingAverage = DailyDataWithRollingAverage(
            newValue = 1,
            cumulativeValue = 101,
            rollingAverage = 1.0,
            rate = 100.0,
            date = LocalDate.of(2020, 1, 1)
        )

        private val combinedChartData =
            CombinedChartData(
                title = barChartLabel,
                barChartData = BarChartData(
                    label = barChartLabel,
                    values = listOf(
                        BarChartItem(
                            value = 10.0f,
                            label = "Foo"
                        )
                    )
                ),
                lineChartData = LineChartData(
                    label = lineChartLabel,
                    values = listOf(
                        LineChartItem(
                            value = 10.0f,
                            label = "Foo"
                        )
                    )
                )
            )
    }
}
