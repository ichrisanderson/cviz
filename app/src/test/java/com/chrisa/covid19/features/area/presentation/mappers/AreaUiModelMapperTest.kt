package com.chrisa.covid19.features.area.presentation.mappers

import android.content.Context
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.BarChartItem
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AreaUiModelMapperTest {

    private val context = mockk<Context>()
    private val sut = AreaUiModelMapper(context)
    private val labelFormatter = SimpleDateFormat("dd-MM", Locale.UK)

    @Test
    fun `WHEN mapAreaDetailModel called THEN ui model is mapped correctly`() {

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

        val allBarChartData = caseModels.map {
            BarChartItem(
                value = it.dailyLabConfirmedCases.toFloat(),
                label = labelFormatter.format(it.date)
            )
        }

        val allCasesLabel = "All cases"
        val latestCasesLabel = "Latest cases"

        every { context.getString(R.string.latest_cases_chart_label) } returns latestCasesLabel
        every { context.getString(R.string.all_cases_chart_label) } returns allCasesLabel

        val mappedModel = sut.mapAreaDetailModel(areaDetailModel)

        assertThat(mappedModel.lastUpdatedAt).isEqualTo(areaDetailModel.lastUpdatedAt)
        assertThat(mappedModel.latestCasesChartData).isEqualTo(
            BarChartData(
                label = latestCasesLabel,
                values = allBarChartData
            )
        )
        assertThat(mappedModel.allCasesChartData).isEqualTo(
            BarChartData(
                label = allCasesLabel,
                values = allBarChartData
            )
        )

    }
}
