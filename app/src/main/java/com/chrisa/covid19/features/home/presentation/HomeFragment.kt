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

package com.chrisa.covid19.features.home.presentation

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.chrisa.covid19.R
import com.chrisa.covid19.features.home.domain.models.AreaCaseListModel
import com.chrisa.covid19.features.home.presentation.widgets.savedAreaCard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchBar()
        initRecyclerView()
        bindIsLoading()
        bindIsEmpty()
        bindAreaCases()
    }

    private fun initSearchBar() {
        fakeSearchBar.setOnClickListener { navigateToHome() }
    }

    private fun initRecyclerView() {
        homeRecyclerView.addItemDecoration(
            AreaItemDecoration(
                homeRecyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                )
            )
        )
    }

    private fun bindIsEmpty() {
        viewModel.isEmpty.observe(viewLifecycleOwner, Observer {
            val isEmpty = it ?: return@Observer
            homeEmptyView.isVisible = isEmpty
        })
    }

    private fun bindIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            val isLoading = it ?: return@Observer
            homeProgress.isVisible = isLoading
        })
    }

    private fun bindAreaCases() {
        viewModel.areaCases.observe(viewLifecycleOwner, Observer {
            val cases = it ?: return@Observer
            homeRecyclerView.isVisible = true
            homeRecyclerView.withModels {
                cases.forEach { areCase ->
                    savedAreaCard {
                        id(areCase.areaCode)
                        areCase(areCase)
                        clickListener { _ ->
                            navigateToArea(areCase)
                        }
                    }
                }
            }
        })
    }

    private fun navigateToHome() {
        findNavController().navigate(HomeFragmentDirections.homeToSearch())
    }

    private fun navigateToArea(area: AreaCaseListModel) {
        val action =
            HomeFragmentDirections.homeToArea(
                areaCode = area.areaCode,
                areaName = area.areaName
            )
        findNavController().navigate(action)
    }
}
