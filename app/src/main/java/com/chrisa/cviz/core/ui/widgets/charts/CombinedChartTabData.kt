package com.chrisa.cviz.core.ui.widgets.charts

sealed class CombinedChartTabData {
    abstract val title: String
}

data class CombinedChartData(
    override val title: String,
    val barChartData: BarChartData,
    val lineChartData: LineChartData
) : CombinedChartTabData()


data class DailyData(
    override val title: String,
    val rawDataItem: List<DailyDataItem>
) : CombinedChartTabData()

data class DailyDataItem(
    val value: Float,
    val label: String
)
