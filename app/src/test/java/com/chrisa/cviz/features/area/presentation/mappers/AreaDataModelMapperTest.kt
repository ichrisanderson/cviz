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

package com.chrisa.cviz.features.area.presentation.mappers

import android.content.Context
import com.chrisa.cviz.R
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverage
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import com.chrisa.cviz.features.area.presentation.models.AreaMetadata
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

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
        every { context.getString(R.string.all_hospital_admissions_chart_label) } returns allHospitalAdmissionsLabel
        every { context.getString(R.string.latest_hospital_admissions_chart_label) } returns latestHospitalAdmissionsLabel
        every { context.getString(R.string.rolling_average_chart_label) } returns rollingAverageLabel

        every { dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(any()) } returns dailyData
        every { weeklySummaryBuilder.buildWeeklySummary(any()) } returns weeklySummary

        every {
            chartBuilder.allChartData(
                allCasesLabel,
                latestCasesLabel,
                rollingAverageLabel,
                any()
            )
        } returns listOf(combinedChartData)

        every {
            chartBuilder.allChartData(
                allDeathsLabel,
                latestDeathsLabel,
                rollingAverageLabel,
                any()
            )
        } returns emptyList()

        every {
            chartBuilder.allChartData(
                allHospitalAdmissionsLabel,
                latestHospitalAdmissionsLabel,
                rollingAverageLabel,
                any()
            )
        } returns emptyList()
    }

    @Test
    fun `WHEN mapAreaDetailModel called without death data THEN deaths are hidden`() {
        val mappedModel = sut.mapAreaDetailModel(areaDetail)

        assertThat(mappedModel).isEqualTo(
            AreaDataModel(
                areaMetadata = areaMetadata,
                caseChartData = listOf(combinedChartData),
                caseSummary = weeklySummary,
                showDeaths = false,
                deathsChartData = emptyList(),
                deathSummary = weeklySummary,
                showHospitalAdmissions = false,
                hospitalAdmissionsSummary = weeklySummary,
                hospitalAdmissionsChartData = emptyList()
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

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithDeaths)

        assertThat(mappedModel).isEqualTo(
            AreaDataModel(
                areaMetadata = areaMetadataWithDeaths,
                caseChartData = listOf(combinedChartData),
                caseSummary = weeklySummary,
                showDeaths = true,
                deathsChartData = listOf(combinedChartData),
                deathSummary = weeklySummary,
                showHospitalAdmissions = false,
                hospitalAdmissionsSummary = weeklySummary,
                hospitalAdmissionsChartData = emptyList()
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called without hospital admission data THEN hospital admissions hidden`() {
        val mappedModel = sut.mapAreaDetailModel(areaDetail)

        assertThat(mappedModel).isEqualTo(
            AreaDataModel(
                areaMetadata = areaMetadata,
                caseChartData = listOf(combinedChartData),
                caseSummary = weeklySummary,
                showDeaths = false,
                deathsChartData = emptyList(),
                deathSummary = weeklySummary,
                showHospitalAdmissions = false,
                hospitalAdmissionsSummary = weeklySummary,
                hospitalAdmissionsChartData = emptyList()
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with hospital admission data THEN hospital admissions shown`() {
        every {
            chartBuilder.allChartData(
                allHospitalAdmissionsLabel,
                latestHospitalAdmissionsLabel,
                rollingAverageLabel,
                any()
            )
        } returns listOf(combinedChartData)

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithHospitalAdmissions)

        assertThat(mappedModel).isEqualTo(
            AreaDataModel(
                areaMetadata = areaMetadataWithHospitalAdmissions,
                caseChartData = listOf(combinedChartData),
                caseSummary = weeklySummary,
                showDeaths = false,
                deathsChartData = emptyList(),
                deathSummary = weeklySummary,
                showHospitalAdmissions = true,
                hospitalAdmissionsSummary = weeklySummary,
                hospitalAdmissionsChartData = listOf(combinedChartData)
            )
        )
    }

    companion object {
        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)
        private val weeklySummary = SynchronisationTestData.weeklySummary

        private const val allCasesLabel = "All cases"
        private const val latestCasesLabel = "Latest cases"
        private const val allDeathsLabel = "All deaths"
        private const val latestDeathsLabel = "Latest deaths"
        private const val allHospitalAdmissionsLabel = "All hospital admissions"
        private const val latestHospitalAdmissionsLabel = "Latest hospital admissions"
        private const val rollingAverageLabel = "Rolling average"
        private const val barChartLabel = "bar chart"
        private const val lineChartLabel = "line chart"

        private val dailyDataWithRollingAverage = DailyDataWithRollingAverage(
            newValue = 1,
            cumulativeValue = 101,
            rollingAverage = 1.0,
            rate = 100.0,
            date = LocalDate.of(2020, 1, 1)
        )

        private val dailyData = listOf(dailyDataWithRollingAverage)

        private val lastData = SynchronisationTestData.dailyData().last()
        private val areaMetadata = AreaMetadata(
            lastUpdatedDate = syncDateTime,
            lastCaseDate = lastData.date,
            lastHospitalAdmissionDate = null,
            lastDeathDate = null
        )

        private val areaMetadataWithDeaths = areaMetadata.copy(
            lastDeathDate = lastData.date
        )

        private val areaMetadataWithHospitalAdmissions = areaMetadata.copy(
            lastHospitalAdmissionDate = lastData.date
        )

        private val areaDetail = AreaDetailModel(
            areaType = AreaType.OVERVIEW.value,
            lastSyncedAt = syncDateTime,
            cases = SynchronisationTestData.dailyData(),
            deaths = emptyList(),
            hospitalAdmissions = emptyList()
        )

        private val areaDetailWithDeaths = areaDetail.copy(
            deaths = SynchronisationTestData.dailyData()
        )

        private val areaDetailWithHospitalAdmissions = areaDetail.copy(
            hospitalAdmissions = SynchronisationTestData.dailyData()
        )

        private val combinedChartData =
            CombinedChartData(
                title = barChartLabel,
                barChartData = BarChartData(
                    label = barChartLabel,
                    values = listOf(
                        BarChartItem(
                            value = 10.0f,
                            label = "BarChartItem"
                        )
                    )
                ),
                lineChartData = LineChartData(
                    label = lineChartLabel,
                    values = listOf(
                        LineChartItem(
                            value = 10.0f,
                            label = "LineChartItem"
                        )
                    )
                )
            )
    }
}
