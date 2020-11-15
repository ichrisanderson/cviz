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

package com.chrisa.cviz.features.home.presentation.summarylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.chrisa.cviz.R
import com.chrisa.cviz.core.util.KeyboardUtils
import com.chrisa.cviz.databinding.SummaryListFragmentBinding
import com.chrisa.cviz.features.home.domain.models.SortOption
import com.chrisa.cviz.features.home.presentation.HomeItemDecoration
import com.chrisa.cviz.summaryCard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class SummaryListFragment : Fragment(R.layout.summary_list_fragment) {

    private lateinit var binding: SummaryListFragmentBinding
    private val viewModel: SummaryListViewModel by viewModels()
    private var controllerState: Bundle? = null
    private var attachedController: EpoxyController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SummaryListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecyclerView()
        initSwipeRefreshLayout()
        bindIsLoading()
        bindIsRefreshing()
        bindSummaries()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        attachedController?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        detachFromController()
        super.onDestroyView()
    }

    private fun attachToController(controller: EpoxyController) {
        attachedController = controller
        if (controllerState != null) {
            controller.onRestoreInstanceState(controllerState)
            controllerState = null
        }
    }

    private fun detachFromController() {
        controllerState = Bundle()
            .apply { attachedController?.onSaveInstanceState(this) }
        attachedController = null
    }

    private fun initToolbar() {
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(binding.toolbar.context, R.drawable.ic_arrow_back)
        binding.toolbar.title = when (viewModel.sortOption) {
            SortOption.InfectionRate -> getString(R.string.top_infection_rates)
            SortOption.NewCases -> getString(R.string.top_cases)
            SortOption.RisingInfectionRate -> getString(R.string.rising_infection_rates)
            SortOption.RisingCases -> getString(R.string.rising_cases)
        }
        binding.toolbar.setNavigationOnClickListener {
            KeyboardUtils.hideSoftKeyboard(it)
            navigateUp()
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.addItemDecoration(
            HomeItemDecoration(
                binding.recyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin_large
                ),
                binding.recyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin_large
                )
            )
        )
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.secondaryColor)
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun bindIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, {
            binding.progress.isVisible = it
        })
    }

    private fun bindIsRefreshing() {
        viewModel.isRefreshing.observe(viewLifecycleOwner, {
            binding.swipeRefreshLayout.isRefreshing = it
        })
    }

    private fun bindSummaries() {
        viewModel.summaries.observe(viewLifecycleOwner, Observer {
            val summaries = it ?: return@Observer
            binding.recyclerView.isVisible = true
            binding.swipeRefreshLayout.isRefreshing = false
            binding.recyclerView.withModels {
                attachToController(this)
                summaries.forEach { data ->
                    when (viewModel.sortOption) {
                        SortOption.InfectionRate, SortOption.RisingInfectionRate -> {
                            summaryCard {
                                id("infection_rate_summary_" + data.areaName)
                                areaPosition(data.position)
                                areaName(data.areaName)
                                currentValue(data.currentInfectionRate.toInt())
                                currentValueCaption(getString(R.string.current_infection_rate))
                                changeInValue(data.changeInInfectionRate.toInt())
                                changeInValueCaption(getString(R.string.change_this_week))
                                clickListener { _ ->
                                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                                }
                            }
                        }
                        SortOption.NewCases, SortOption.RisingCases -> {
                            summaryCard {
                                id("infection_rate_summary_" + data.areaName)
                                areaPosition(data.position)
                                areaName(data.areaName)
                                currentValue(data.currentNewCases)
                                currentValueCaption(getString(R.string.cases_this_week))
                                changeInValue(data.changeInCases)
                                changeInValueCaption(getString(R.string.change_this_week))
                                clickListener { _ ->
                                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    private fun navigateToArea(areaCode: String, areaName: String, areaType: String) {
        val action =
            SummaryListFragmentDirections.summaryListToArea(
                areaCode = areaCode,
                areaName = areaName,
                areaType = areaType
            )
        findNavController().navigate(action)
    }
}
