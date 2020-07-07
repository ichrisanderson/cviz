package com.chrisa.covid19.features.area.presentation.models

import com.chrisa.covid19.core.ui.widgets.charts.BarChartData
import java.util.Date

data class AreaUiModel(
    val lastUpdatedAt: Date,
    val latestCasesChartData: BarChartData,
    val allCasesChartData: BarChartData
)
