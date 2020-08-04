package com.chrisa.covid19.core.ui.widgets.charts

import com.github.mikephil.charting.formatter.ValueFormatter

class StringValueAxisFormatter(private val labels: List<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return labels[value.toInt()]
    }
}
