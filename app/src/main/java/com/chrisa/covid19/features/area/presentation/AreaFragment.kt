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

package com.chrisa.covid19.features.area.presentation

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chrisa.covid19.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_area.*

@AndroidEntryPoint
class AreaFragment : Fragment(R.layout.fragment_area) {

    private val viewModel: AreaViewModel by viewModels()
    private val args by navArgs<AreaFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        observeViewModel()
    }

    private fun initToolbar() {
        areaToolbar.navigationIcon =
            ContextCompat.getDrawable(areaToolbar.context, R.drawable.ic_arrow_back)
        areaToolbar.title = args.areaName
        areaToolbar.setNavigationOnClickListener { navigateUp() }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                AreaState.Loading -> {
                    // TODO: Loading State
                }
                is AreaState.Success -> {

                    val areaUiModel = state.areaUiModel

                    totalCasesSubtitle.text = getString(R.string.last_updated_date, DateUtils.getRelativeTimeSpanString(areaUiModel.lastUpdatedAt.time))
                    latestCasesChart.setData(areaUiModel.latestCasesChartData)
                    allCasesChart.setData(areaUiModel.allCasesChartData)
                }
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}
