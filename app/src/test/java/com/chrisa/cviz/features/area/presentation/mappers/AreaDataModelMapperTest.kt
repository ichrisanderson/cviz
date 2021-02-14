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
import com.chrisa.cviz.core.data.synchronisation.DailyDataWithRollingAverageBuilder
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.data.synchronisation.WeeklySummaryBuilder
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartItem
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData
import com.chrisa.cviz.core.ui.widgets.charts.LineChartItem
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.chrisa.cviz.features.area.domain.models.AlertLevelModel as DomainAlertLevelModel
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.domain.models.AreaTransmissionRateModel as DomainAreaTransmissionRateModel
import com.chrisa.cviz.features.area.domain.models.SoaData
import com.chrisa.cviz.features.area.domain.models.SoaDataModel as DomainSoaDataModel
import com.chrisa.cviz.features.area.domain.models.TransmissionRateModel
import com.chrisa.cviz.features.area.presentation.models.AlertLevelModel
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import com.chrisa.cviz.features.area.presentation.models.AreaTransmissionRateModel
import com.chrisa.cviz.features.area.presentation.models.HospitalAdmissionsAreaModel
import com.chrisa.cviz.features.area.presentation.models.SoaDataModel
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
    private val admissionsFilter = mockk<AdmissionsFilter>()
    private val soaChartBuilder = mockk<SoaChartBuilder>()
    private val sut = AreaDataModelMapper(
        context,
        dailyDataWithRollingAverageBuilder,
        weeklySummaryBuilder,
        chartBuilder,
        admissionsFilter,
        soaChartBuilder
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

        every { admissionsFilter.filterHospitalData(any(), any()) } returns
            emptyList()
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

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
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

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                lastUpdatedDate = lastUpdatedDateTime,
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

        val mappedModel = sut.mapAreaDetailModel(areaDetailWithCases, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
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
        val combinedAdmissions = SynchronisationTestData.dailyData(100, 123)
        val hospitalAdmissions = listOf(
            AreaDailyDataDto("T3", admissions),
            AreaDailyDataDto("T2", admissions),
            AreaDailyDataDto("T1", admissions)
        )
        val areaDetail = areaDetail.copy(
            hospitalAdmissions = hospitalAdmissions,
            hospitalAdmissionsAreaName = areaName
        )
        val admissionsWeeklySummary = SynchronisationTestData.weeklySummary(currentTotal = 233)
        every { admissionsFilter.filterHospitalData(hospitalAdmissions, emptySet()) } returns
            combinedAdmissions
        every { weeklySummaryBuilder.buildWeeklySummary(combinedAdmissions) } returns
            admissionsWeeklySummary
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(combinedAdmissions)
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

        val mappedModel = sut.mapAreaDetailModel(areaDetail, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                showHospitalAdmissions = true,
                lastHospitalAdmissionDate = combinedAdmissions.last().date,
                canFilterHospitalAdmissionsAreas = true,
                hospitalAdmissionsRegionName = areaName,
                hospitalAdmissions = hospitalAdmissions,
                hospitalAdmissionsSummary = admissionsWeeklySummary,
                hospitalAdmissionsChartData = listOf(chartData),
                hospitalAdmissionsAreas = hospitalAdmissions
                    .sortedBy { it.name }.map {
                        HospitalAdmissionsAreaModel(
                            it.name,
                            true
                        )
                    }
            )
        )
    }

    @Test
    fun `WHEN updateHospitalAdmissionFilters THEN hospital data shown`() {
        val admissionsWithRollingAverage =
            SynchronisationTestData.dailyDataWithRollingAverage(1, 23)
        val chartData = combinedChartData("admissions")
        val combinedAdmissions = SynchronisationTestData.dailyData(100, 123)
        val admissions = SynchronisationTestData.dailyData(1, 23)
        val hospitalAdmissions = listOf(
            AreaDailyDataDto("T3", admissions),
            AreaDailyDataDto("T2", admissions),
            AreaDailyDataDto("T1", admissions)
        )
        val areaDataModel = defaultModel.copy(
            hospitalAdmissions = hospitalAdmissions
        )
        val admissionsWeeklySummary = SynchronisationTestData.weeklySummary(currentTotal = 233)
        every { admissionsFilter.filterHospitalData(hospitalAdmissions, emptySet()) } returns
            combinedAdmissions
        every { weeklySummaryBuilder.buildWeeklySummary(combinedAdmissions) } returns
            admissionsWeeklySummary
        every {
            dailyDataWithRollingAverageBuilder.buildDailyDataWithRollingAverage(combinedAdmissions)
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

        val mappedModel = sut.updateHospitalAdmissionFilters(areaDataModel, emptySet())

        assertThat(mappedModel.hospitalAdmissionsSummary).isEqualTo(admissionsWeeklySummary)
        assertThat(mappedModel.hospitalAdmissionsChartData).isEqualTo(listOf(chartData))
        assertThat(mappedModel.hospitalAdmissionsAreas).isEqualTo(
            hospitalAdmissions.sortedBy { it.name }
                .map {
                    HospitalAdmissionsAreaModel(
                        it.name,
                        true
                    )
                }
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with transmission data THEN hospital data shown`() {
        val rateDate = lastUpdatedDateTime.minusDays(7).toLocalDate()
        val areaName = "London"
        val areaTransmissionRate = DomainAreaTransmissionRateModel(
            areaName,
            lastUpdatedDateTime,
            TransmissionRateModel(
                date = rateDate,
                transmissionRateMin = 1.0,
                transmissionRateMax = 1.3,
                transmissionRateGrowthRateMin = 0.3,
                transmissionRateGrowthRateMax = 0.7
            )
        )
        val areaDetail = areaDetail.copy(
            transmissionRate = areaTransmissionRate
        )

        val mappedModel = sut.mapAreaDetailModel(areaDetail, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                areaTransmissionRate = AreaTransmissionRateModel(
                    areaName = areaName,
                    lastUpdatedDate = lastUpdatedDateTime,
                    lastRateDate = rateDate,
                    minRate = 1.0,
                    maxRate = 1.3,
                    minGrowthRate = 0.3,
                    maxGrowthRate = 0.7
                )
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with alert level data THEN alert level is present`() {
        val alertLevel = DomainAlertLevelModel(alertLevelUrl = "https://www.acme.com")
        val areaDetail = areaDetail.copy(alertLevel = alertLevel)

        val mappedModel = sut.mapAreaDetailModel(areaDetail, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                alertLevel = AlertLevelModel(
                    alertLevelUrl = alertLevel.alertLevelUrl
                )
            )
        )
    }

    @Test
    fun `WHEN mapAreaDetailModel called with soa data THEN soa data is present`() {
        val week1 = SoaData(
            date = LocalDate.of(2020, 1, 1),
            rollingSum = 11,
            rollingRate = 12.0
        )
        val week2 = SoaData(
            date = LocalDate.of(2020, 1, 1),
            rollingSum = 11,
            rollingRate = 12.0
        )
        val data = listOf(week2, week1)
        val soaDataModel = DomainSoaDataModel(
            areaCode = "1234",
            areaName = "London",
            areaType = AreaType.REGION,
            data = data
        )
        val casesChart = combinedChartData("soa cases")
        val chartData = listOf(casesChart)
        every { soaChartBuilder.caseChartData(data) } returns chartData
        val areaDetail = areaDetail.copy(soaData = soaDataModel)

        val mappedModel = sut.mapAreaDetailModel(areaDetail, emptySet())

        assertThat(mappedModel).isEqualTo(
            defaultModel.copy(
                soaData = SoaDataModel(
                    areaName = soaDataModel.areaName,
                    lastDate = week2.date,
                    weeklyRate = week2.rollingRate.toInt(),
                    weeklyCases = week2.rollingSum,
                    changeInCases = week2.rollingSum - week1.rollingSum,
                    changeInRate = week2.rollingRate.toInt() - week1.rollingRate.toInt(),
                    chartData = chartData
                )
            )
        )
    }

    companion object {
        private val lastUpdatedDateTime = LocalDateTime.of(2020, 1, 1, 11, 0)

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
            lastUpdatedAt = lastUpdatedDateTime,
            casesAreaName = "",
            cases = emptyList(),
            deathsByPublishedDateAreaName = "",
            deathsByPublishedDate = emptyList(),
            onsDeathAreaName = "",
            onsDeathsByRegistrationDate = emptyList(),
            hospitalAdmissionsAreaName = "",
            hospitalAdmissions = emptyList(),
            transmissionRate = null,
            alertLevel = null
        )

        private val defaultModel = AreaDataModel(
            lastUpdatedDate = lastUpdatedDateTime,
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
            hospitalAdmissions = emptyList(),
            hospitalAdmissionsChartData = emptyList(),
            canFilterHospitalAdmissionsAreas = false,
            hospitalAdmissionsAreas = emptyList(),
            areaTransmissionRate = null,
            alertLevel = null,
            soaData = null
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
