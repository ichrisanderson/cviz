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

package com.chrisa.cviz.core.ui.widgets.charts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chrisa.cviz.R
import com.chrisa.cviz.databinding.CoreWidgetBarChartBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class BarChart(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    private var binding: CoreWidgetBarChartBinding =
        CoreWidgetBarChartBinding.inflate(LayoutInflater.from(context), this)

    init {
        initChart()
        initXAxis()
        initYAxis()
    }

    private fun initChart() {
        binding.chart.setDrawGridBackground(false)
        binding.chart.axisRight.isEnabled = false
        binding.chart.description.isEnabled = false
        binding.chart.setDrawBarShadow(false)
        binding.chart.setDrawValueAboveBar(true)
        binding.chart.setDrawGridBackground(false)
        binding.chart.setPinchZoom(false)
        binding.chart.isDoubleTapToZoomEnabled = false
        binding.chart.extraBottomOffset = 16f
    }

    private fun initXAxis() {
        val xAxis: XAxis = binding.chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelRotationAngle = -30.0f
    }

    private fun initYAxis() {
        val leftAxis: YAxis = binding.chart.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 16f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.granularity = 1f
    }

    fun setData(barChartData: BarChartData) {
        binding.chart.data = barDataSet(barChartData)
        binding.chart.invalidate()
    }

    private fun barDataSet(data: BarChartData): BarData {
        val barDataSet = BarDataSet(
            data.values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.value) },
            data.label
        )
        barDataSet.setDrawValues(false)
        barDataSet.isHighlightEnabled = false
        barDataSet.color = ContextCompat.getColor(context, R.color.combinedChartBarColor)

        binding.chart.legend.textColor = defaultTextColor()
        binding.chart.xAxis.textColor = defaultTextColor()
        binding.chart.axisLeft.textColor = defaultTextColor()
        binding.chart.xAxis.valueFormatter = StringValueAxisFormatter(data.values.map { it.label })

        return BarData(barDataSet)
    }

    private fun defaultTextColor() = ContextCompat.getColor(context, R.color.primaryTextColor)
}
