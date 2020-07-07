package com.chrisa.covid19.features.area.presentation.mappers

import android.content.Context
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import com.chrisa.covid19.core.ui.widgets.charts.BarChartItem
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.chrisa.covid19.features.area.presentation.models.AreaUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class AreaUiModelMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun mapAreaDetailModel(areaDetailModel: AreaDetailModel): AreaUiModel {
        return AreaUiModel(
            lastUpdatedAt = areaDetailModel.lastUpdatedAt,
            latestCasesChartData = BarChartData(
                label = context.getString(R.string.latest_cases_chart_label),
                values = areaDetailModel.latestCases.map(this::mapCaseModel)
            ),
            allCasesChartData = BarChartData(
                label = context.getString(R.string.all_cases_chart_label),
                values = areaDetailModel.allCases.map(this::mapCaseModel)
            )
        )
    }

    private fun mapCaseModel(caseModel: CaseModel): BarChartItem {
        return BarChartItem(
            caseModel.dailyLabConfirmedCases.toFloat(),
            labelFormatter.format(caseModel.date)
        )
    }

    companion object {
        private val labelFormatter = SimpleDateFormat("dd-MM", Locale.UK)
    }
}
