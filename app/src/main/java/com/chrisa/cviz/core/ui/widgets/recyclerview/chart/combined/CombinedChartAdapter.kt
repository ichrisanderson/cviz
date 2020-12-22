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

class CombinedChartAdapter : ListAdapter<CombinedChartData, CombinedChartAdapter.ViewHolder>(
    CombinedChartDataDiffCallback()
) {

    fun getItemTitle(position: Int): String = getItem(position).title

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val root = inflater.inflate(R.layout.core_combined_chart_holder, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chartData = getItem(position)
        holder.chart.setData(chartData.barChartData, chartData.lineChartData)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chart: CombinedChart = itemView.findViewById(R.id.chart_container)
    }
}
