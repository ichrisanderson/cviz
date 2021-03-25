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
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChart
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartTabData
import com.chrisa.cviz.core.ui.widgets.charts.DailyData

class CombinedChartAdapter : ListAdapter<CombinedChartTabData, CombinedChartAdapter.ViewHolder>(
    CombinedChartDataDiffCallback()
) {
    enum class ItemViewType {
        Chart,
        Data;

        companion object {
            fun from(value: Int): ItemViewType =
                when (value) {
                    Chart.ordinal -> Chart
                    Data.ordinal -> Data
                    else -> throw IllegalArgumentException("Unsupported item view type")
                }
        }
    }

    fun getItemTitle(position: Int): String =
        getItem(position).title

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is CombinedChartData -> ItemViewType.Chart.ordinal
            is DailyData -> TODO()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (ItemViewType.from(viewType)) {
            ItemViewType.Chart -> chartLayout(inflater, parent)
            ItemViewType.Data -> TODO()
        }
    }

    private fun chartLayout(inflater: LayoutInflater, parent: ViewGroup) =
        ChartViewHolder(inflater.inflate(R.layout.core_combined_chart_holder, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        when (val item = getItem(position)) {
            is CombinedChartData -> chartLayout(holder, item)
            is DailyData -> TODO()
        }

    private fun chartLayout(holder: ViewHolder, combinedChartData: CombinedChartData) {
        if (holder is ChartViewHolder) {
            holder.chart.setData(combinedChartData.barChartData, combinedChartData.lineChartData)
        }
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class ChartViewHolder(itemView: View) : ViewHolder(itemView) {
        val chart: CombinedChart = itemView.findViewById(R.id.chart_container)
    }
}
