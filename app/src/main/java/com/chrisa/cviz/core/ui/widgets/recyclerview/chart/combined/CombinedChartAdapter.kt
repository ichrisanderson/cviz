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

package com.chrisa.cviz.core.ui.widgets.recyclerview.chart.combined

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chrisa.cviz.R
import com.chrisa.cviz.core.ui.widgets.charts.BarChart
import com.chrisa.cviz.core.ui.widgets.charts.BarChartData
import com.chrisa.cviz.core.ui.widgets.charts.BarChartTab
import com.chrisa.cviz.core.ui.widgets.charts.ChartTab
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChart
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTab
import com.chrisa.cviz.core.ui.widgets.charts.DataSheet
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetItem
import com.chrisa.cviz.core.ui.widgets.charts.DataSheetTab
import com.chrisa.cviz.core.ui.widgets.charts.LineChartData

class CombinedChartAdapter : ListAdapter<ChartTab, CombinedChartAdapter.ViewHolder>(
    CombinedChartDataDiffCallback()
) {
    enum class ItemViewType {
        CombinedChart,
        BarChart,
        Data;

        companion object {
            fun from(value: Int): ItemViewType =
                when (value) {
                    BarChart.ordinal -> BarChart
                    CombinedChart.ordinal -> CombinedChart
                    Data.ordinal -> Data
                    else -> throw IllegalArgumentException("Unsupported item view type")
                }
        }
    }

    fun getItemTitle(position: Int): String =
        getItem(position).title

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is BarChartTab -> ItemViewType.BarChart.ordinal
            is CombinedChartTab -> ItemViewType.CombinedChart.ordinal
            is DataSheetTab -> ItemViewType.Data.ordinal
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (ItemViewType.from(viewType)) {
            ItemViewType.BarChart -> barChartLayout(inflater, parent)
            ItemViewType.CombinedChart -> combinedChartLayout(inflater, parent)
            ItemViewType.Data -> dataSheetLayout(inflater, parent)
        }
    }

    private fun combinedChartLayout(inflater: LayoutInflater, parent: ViewGroup): ViewHolder =
        CombinedChartViewHolder(
            inflater.inflate(
                R.layout.core_combined_chart_holder,
                parent,
                false
            )
        )

    private fun barChartLayout(inflater: LayoutInflater, parent: ViewGroup): ViewHolder =
        BarChartViewHolder(
            inflater.inflate(
                R.layout.core_bar_chart_holder,
                parent,
                false
            )
        )

    private fun dataSheetLayout(inflater: LayoutInflater, parent: ViewGroup): ViewHolder =
        DataSheetViewHolder(inflater.inflate(R.layout.core_chart_data_sheet_holder, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: ChartTab)
    }

    class CombinedChartViewHolder(itemView: View) : ViewHolder(itemView) {
        val chart: CombinedChart = itemView.findViewById(R.id.chart_container)

        override fun bind(item: ChartTab) =
            when (item) {
                is CombinedChartTab ->
                    bindChart(item.barChartData, item.lineChartData)
                else ->
                    throw IllegalArgumentException("Unsupported item type")
            }

        private fun bindChart(barChartData: BarChartData, lineChartData: LineChartData) =
            chart.setData(barChartData, lineChartData)
    }

    class BarChartViewHolder(itemView: View) : ViewHolder(itemView) {
        val chart: BarChart = itemView.findViewById(R.id.chart_container)

        override fun bind(item: ChartTab) =
            when (item) {
                is BarChartTab ->
                    bindChart(item.barChartData)
                else ->
                    throw IllegalArgumentException("Unsupported item type")
            }

        private fun bindChart(barChartData: BarChartData) =
            chart.setData(barChartData)
    }

    class DataSheetViewHolder(itemView: View) : ViewHolder(itemView) {
        val dataSheet: DataSheet = itemView.findViewById(R.id.sheet_container)

        override fun bind(item: ChartTab) {
            when (item) {
                is DataSheetTab ->
                    bindSheet(item.data)
                else ->
                    throw IllegalArgumentException("Unsupported item type")
            }
        }

        private fun bindSheet(data: List<DataSheetItem>) =
            dataSheet.setData(data)
    }
}
