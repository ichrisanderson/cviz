package com.chrisa.covid19.core.ui.widgets.charts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.chrisa.covid19.R
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.view_total_cases_chart.view.*

class BarChart(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_total_cases_chart, this, true)
        initChart()
        initXAxis()
        initYAxis()
    }

    private fun initChart() {
        chart.setDrawGridBackground(false)
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.extraBottomOffset = 16f
    }

    private fun initXAxis() {
        val xAxis: XAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelRotationAngle = -30.0f
    }

    private fun initYAxis() {
        val leftAxis: YAxis = chart.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 16f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    }

    fun setData(
        data: BarChartData
    ) {
        val barDataSet = BarDataSet(
            data.values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.value) },
            data.label
        )
        barDataSet.setDrawValues(false)
        barDataSet.isHighlightEnabled = false

        chart.xAxis.valueFormatter = StringValueAxisFormatter(data.values.map { it.label })
        chart.data = BarData(barDataSet)
        chart.invalidate()
    }
}

private class StringValueAxisFormatter(private val labels: List<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return labels[value.toInt()]
    }
}
