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
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
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
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(emptyList())
        } returns
            emptyList()
        every { weeklySummaryBuilder.buildWeeklySummary(emptyList()) } returns
            WeeklySummary.EMPTY

        every {
            chartBuilder.allBarChartData(
                allDeathsLabel,
                latestDeathsLabel,
                emptyList()
            )
        } returns emptyList()

        every {
            chartBuilder.allCombinedChartData(
                allCasesLabel,
                latestCasesLabel,
                rollingAverageLabel,
                emptyList()
            )
        } returns emptyList()

        every {
            chartBuilder.allCombinedChartData(
                allDeathsLabel,
                latestDeathsLabel,
                rollingAverageLabel,
                emptyList()
            )
        } returns emptyList()

        every {
            chartBuilder.allCombinedChartData(
                allHospitalAdmissionsLabel,
                latestHospitalAdmissionsLabel,
                rollingAverageLabel,
                emptyList()
            )
        } returns emptyList()
    }

    @Test
    fun `WHEN mapAreaDetailModel called with case data THEN cases shown`() {
        val areaName = "London"
        val cases = SynchronisationTestData.dailyData(1, 100)
        val casesWithRollingAverage = SynchronisationTestData.dailyDataWithRollingAverage(1, 100)
        val chartData = combinedChartData("cases")
        val areaDetailWithCases = areaDetail.copy(
            casesAreaName = areaName,
            cases = cases
        )
        val casesWeeklySummary = SynchronisationTestData.weeklySummary(currentTotal = 1000)
        every { weeklySummaryBuilder.buildWeeklySummary(cases) } returns
            casesWeeklySummary
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(cases)
        } returns
            casesWithRollingAverage
        every {
            chartBuilder.allCombinedChartData(
                allCasesLabel,
                latestCasesLabel,
                rollingAverageLabel,
                casesWithRollingAverage
            )
        } returns
            listOf(chartData)

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases)

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                lastUpdatedDate = syncDateTime,
                caseAreaName = areaName,
                lastCaseDate = cases.last().date,
                caseSummary = casesWeeklySummary,
                caseChartData = listOf(chartData)
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with published death data THEN published deaths shown`() {
        val areaName = "London"
        val deaths = SynchronisationTestData.dailyData(1, 50)
        val deathsWithRollingAverage = SynchronisationTestData.dailyDataWithRollingAverage(1, 50)
        val chartData = combinedChartData("deaths")
        val areaDetailWithCases = areaDetail.copy(
            deathsByPublishedDate = deaths,
            deathsByPublishedDateAreaName = areaName
        )
        val deathsWeeklySummary = SynchronisationTestData.weeklySummary(currentTotal = 500)
        every { weeklySummaryBuilder.buildWeeklySummary(deaths) } returns
            deathsWeeklySummary
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(deaths)
        } returns
            deathsWithRollingAverage
        every {
            chartBuilder.allCombinedChartData(
                allDeathsLabel,
                latestDeathsLabel,
                rollingAverageLabel,
                deathsWithRollingAverage
            )
        } returns
            listOf(chartData)

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases)

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                lastUpdatedDate = syncDateTime,
                lastDeathPublishedDate = deaths.last().date,
                showDeathsByPublishedDate = true,
                deathsByPublishedDateAreaName = areaName,
                deathsByPublishedDateSummary = deathsWeeklySummary,
                deathsByPublishedDateChartData = listOf(chartData)
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with ons death data THEN ons deaths shown`() {
        val areaName = "London"
        val deaths = SynchronisationTestData.dailyData(1, 75)
        val chartData = barChartData("onsDeaths")
        val areaDetailWithCases = areaDetail.copy(
            onsDeathsByRegistrationDate = deaths,
            onsDeathAreaName = areaName
        )
        every {
            chartBuilder.allBarChartData(
                allDeathsLabel,
                latestDeathsLabel,
                deaths
            )
        } returns listOf(chartData)

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases)

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                lastUpdatedDate = syncDateTime,
                showOnsDeaths = true,
                lastOnsDeathRegisteredDate = deaths.last().date,
                onsDeathsAreaName = areaName,
                onsDeathsByRegistrationDateChartData = listOf(chartData)
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with admission data THEN hospital data shown`() {
        val areaName = "London"
        val admissions = SynchronisationTestData.dailyData(1, 23)
        val admissionsWithRollingAverage =
            SynchronisationTestData.dailyDataWithRollingAverage(1, 23)
        val chartData = combinedChartData("admissions")
        val areaDetailWithCases = areaDetail.copy(
            hospitalAdmissions = admissions,
            hospitalAdmissionsRegionName = areaName
        )
        val admissionsWeeklySummary = SynchronisationTestData.weeklySummary(currentTotal = 233)
        every { weeklySummaryBuilder.buildWeeklySummary(admissions) } returns
            admissionsWeeklySummary
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(admissions)
        } returns
            admissionsWithRollingAverage
        every {
            chartBuilder.allCombinedChartData(
                allHospitalAdmissionsLabel,
                latestHospitalAdmissionsLabel,
                rollingAverageLabel,
                admissionsWithRollingAverage
            )
        } returns
            listOf(chartData)

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases)

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                lastUpdatedDate = syncDateTime,
                showHospitalAdmissions = true,
                lastHospitalAdmissionDate = admissions.last().date,
                hospitalAdmissionsRegionName = areaName,
                hospitalAdmissionsSummary = admissionsWeeklySummary,
                hospitalAdmissionsChartData = listOf(chartData)
            )
        )
    }

    companion object {
        private val syncDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)

        private const val allCasesLabel = "All cases"
        private const val latestCasesLabel = "Latest cases"
        private const val allDeathsLabel = "All deaths"
        private const val latestDeathsLabel = "Latest deaths"
        private const val allHospitalAdmissionsLabel = "All hospital admissions"
        private const val latestHospitalAdmissionsLabel = "Latest hospital admissions"
        private const val rollingAverageLabel = "Rolling average"
        private const val barChartLabel = "bar chart"
        private const val lineChartLabel = "line chart"

        private val areaDetail = AreaDetailModel(
            lastUpdatedAt = syncDateTime,
            lastSyncedAt = syncDateTime,
            cases = emptyList(),
            casesAreaName = "",
            deathsByPublishedDateAreaName = "",
            deathsByPublishedDate = emptyList(),
            onsDeathAreaName = "",
            onsDeathsByRegistrationDate = emptyList(),
            hospitalAdmissionsRegionName = "",
            hospitalAdmissions = emptyList()
        )

        private val defaultModel = AreaDataModel(
            lastUpdatedDate = null,
            lastCaseDate = null,
            caseAreaName = "",
            caseSummary = WeeklySummary.EMPTY,
            caseChartData = emptyList(),
            showDeathsByPublishedDate = false,
            lastDeathPublishedDate = null,
            deathsByPublishedDateAreaName = "",
            deathsByPublishedDateSummary = WeeklySummary.EMPTY,
            deathsByPublishedDateChartData = emptyList(),
            showOnsDeaths = false,
            lastOnsDeathRegisteredDate = null,
            onsDeathsAreaName = "",
            onsDeathsByRegistrationDateChartData = emptyList(),
            showHospitalAdmissions = false,
            lastHospitalAdmissionDate = null,
            hospitalAdmissionsRegionName = "",
            hospitalAdmissionsSummary = WeeklySummary.EMPTY,
            hospitalAdmissionsChartData = emptyList()
        )

        private fun combinedChartData(labelPrefix: String) =
            CombinedChartData(
                title = barChartLabel,
                barChartData = BarChartData(
                    label = barChartLabel,
                    values = listOf(
                        BarChartItem(
                            value = 10.0f,
                            label = "${labelPrefix}_BarChartItem"
                        )
                    )
                ),
                lineChartData = LineChartData(
                    label = lineChartLabel,
                    values = listOf(
                        LineChartItem(
                            value = 10.0f,
                            label = "${labelPrefix}_LineChartItem"
                        )
                    )
                )
            )

        private fun barChartData(labelPrefix: String) =
            BarChartData(
                label = barChartLabel,
                values = listOf(
                    BarChartItem(
                        value = 10.0f,
                        label = "${labelPrefix}_BarChartItem"
                    )
                )
            )
    }
}
