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

package com.chrisa.cviz.features.home.presentation.savedareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.chrisa.cviz.R
import com.chrisa.cviz.areaDetailCard
import com.chrisa.cviz.databinding.SavedAreasFragmentBinding
import com.chrisa.cviz.emptySavedAreasCard
import com.chrisa.cviz.features.home.presentation.HomeFragmentDirections
import com.chrisa.cviz.features.home.presentation.HomeItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class SavedAreasFragment : Fragment(R.layout.saved_areas_fragment) {

    private lateinit var binding: SavedAreasFragmentBinding
    private val viewModel: SavedAreasViewModel by viewModels()
    private var controllerState: Bundle? = null
    private var attachedController: EpoxyController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SavedAreasFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun bindIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            val isLoading = it ?: return@Observer
            binding.progress.isVisible = isLoading
        })
    }

    private fun bindIsRefreshing() {
        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            binding.swipeRefreshLayout.isRefreshing = it
        })
    }

    private fun bindSummaries() {
        viewModel.savedAreas.observe(viewLifecycleOwner, Observer {
            val savedAreas = it ?: return@Observer
            binding.swipeRefreshLayout.isVisible = true
            binding.recyclerView.isVisible = true
            binding.recyclerView.withModels {
                attachToController(this)
                if (savedAreas.isEmpty()) {
                    emptySavedAreasCard {
                        id("emptySavedAreas")
                    }
                } else {
                    savedAreas.forEach { summary ->
                        areaDetailCard {
                            id(summary.areaCode)
                            areaName(summary.areaName)
                            currentNewCases(summary.currentNewCases)
                            changeInCasesThisWeek(summary.changeInCases)
                            currentInfectionRate(summary.changeInInfectionRate.toInt())
                            changeInInfectionRateThisWeek(summary.changeInInfectionRate.toInt())
                            clickListener { _ ->
                                navigateToArea(
                                    summary.areaCode,
                                    summary.areaName,
                                    summary.areaType
                                )
                            }
                        }
                    }
                }
            }
        })
    }

    private fun navigateToArea(areaCode: String, areaName: String, areaType: String) {
        val action =
            HomeFragmentDirections.homeToArea(
                areaCode = areaCode,
                areaName = areaName,
                areaType = areaType
            )
        findNavController().navigate(action)
    }
}
