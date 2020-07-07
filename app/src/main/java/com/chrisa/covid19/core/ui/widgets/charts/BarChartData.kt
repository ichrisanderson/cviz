package com.chrisa.covid19.core.ui.widgets.charts

data class BarChartData(
    val label: String,
    val values: List<BarChartItem>
)

data class BarChartItem(
    val value: Float,
    val label: String
)
