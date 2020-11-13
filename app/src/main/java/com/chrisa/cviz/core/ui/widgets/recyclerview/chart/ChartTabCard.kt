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

package com.chrisa.cviz.core.ui.widgets.recyclerview.chart

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.cviz.R
import com.chrisa.cviz.core.ui.widgets.charts.CombinedChartData
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.core_widget_chart_tab_card)
class ChartTabCard(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

    private val adapter = ChartAdapter()
    private lateinit var pager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupViewPager()
        setupTabLayout()
    }

    private fun setupViewPager() {
        pager = findViewById(R.id.pager)
        pager.adapter = adapter
        pager.isUserInputEnabled = false
        pager.offscreenPageLimit = 2
    }

    private fun setupTabLayout() {
        tabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = adapter.getItemTitle(position)
        }.attach()
    }

    @ModelProp
    fun chartData(chartData: List<CombinedChartData>) {
        adapter.submitList(chartData)
    }
}
