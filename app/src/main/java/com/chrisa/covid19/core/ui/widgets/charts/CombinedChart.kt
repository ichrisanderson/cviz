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

package com.chrisa.covid19.core.ui.widgets.charts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chrisa.covid19.R
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.widget_combined_chart.view.*

class CombinedChart(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_combined_chart, this, true)
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
        leftAxis.granularity = 1f
    }

    fun setData(
        barChartData: BarChartData,
        lineChartData: LineChartData
    ) {
        val combinedData = CombinedData()

        combinedData.setData(barDataSet(barChartData))
        combinedData.setData(lineDataSet(lineChartData))

        chart.data = combinedData

        chart.invalidate()
    }

    private fun barDataSet(data: BarChartData): BarData {
        val barDataSet = BarDataSet(
            data.values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.value) },
            data.label
        )
        barDataSet.setDrawValues(false)
        barDataSet.isHighlightEnabled = false

        chart.xAxis.valueFormatter = StringValueAxisFormatter(data.values.map { it.label })

        return BarData(barDataSet)
    }

    private fun lineDataSet(data: LineChartData): LineData {
        val dataSet = LineDataSet(
            data.values.mapIndexed { i, v -> Entry(i.toFloat(), v.value) },
            data.label
        )
        dataSet.setDrawValues(false)
        dataSet.isHighlightEnabled = false

        dataSet.color = ContextCompat.getColor(context, android.R.color.black)
        dataSet.setDrawCircleHole(false)
        dataSet.setDrawCircles(false)

        return LineData(dataSet)
    }
}
