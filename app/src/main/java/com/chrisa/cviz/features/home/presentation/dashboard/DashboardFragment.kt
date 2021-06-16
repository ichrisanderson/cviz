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

package com.chrisa.cviz.features.home.presentation.dashboard

import android.content.Intent
import android.net.Uri
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
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.carousel
import com.chrisa.cviz.LatestUkDataCardBindingModel_
import com.chrisa.cviz.R
import com.chrisa.cviz.SummaryCardBindingModel_
import com.chrisa.cviz.core.ui.widgets.recyclerview.caseMapCard
import com.chrisa.cviz.databinding.DashboardFragmentBinding
import com.chrisa.cviz.features.home.domain.models.LatestUkDataModel
import com.chrisa.cviz.features.home.domain.models.SortOption
import com.chrisa.cviz.features.home.domain.models.SummaryModel
import com.chrisa.cviz.features.home.presentation.HomeFragmentDirections
import com.chrisa.cviz.sectionHeader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.dashboard_fragment) {

    private lateinit var binding: DashboardFragmentBinding
    private val viewModel: DashboardViewModel by viewModels()
    private var controllerState: Bundle? = null
    private var attachedController: EpoxyController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initSwipeRefreshLayout()
        bindIsLoading()
        bindIsRefreshing()
        bindAreaCases()
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
            DashboardItemDecoration(
                binding.recyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                ),
                binding.recyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                )
            )
        )
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.secondaryColor)
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun bindIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding.progress.isVisible = it
        })
    }

    private fun bindIsRefreshing() {
        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            binding.swipeRefreshLayout.isRefreshing = it
        })
    }

    private fun bindAreaCases() {
        viewModel.dashboardData.observe(viewLifecycleOwner, Observer {
            val homeScreenData = it ?: return@Observer
            binding.swipeRefreshLayout.isVisible = true
            binding.swipeRefreshLayout.isRefreshing = false
            binding.recyclerView.withModels {

                attachToController(this)

                sectionHeader {
                    id("dailyRecordHeader")
                    title(getString(R.string.uk_overview))
                    isMoreButtonVisible(false)
                }
                carousel {
                    id("dailyRecordCarousel")
                    models(dailyRecordModels("dailyRecord_", homeScreenData.latestUkData))
                }
                homeScreenData.nationMap?.let { nationMap ->
                    caseMapCard {
                        id("caseMap")
                        mapDate(nationMap.lastUpdated)
                        mapUri(nationMap.imageUri)
                        clickListener { _ -> navigateToInteractiveMap(nationMap.redirectUri) }
                    }
                }
                sectionHeader {
                    id("topNewCasesHeader")
                    title(getString(R.string.top_cases))
                    isMoreButtonVisible(true)
                    clickListener { _ -> navigateToSummaryList(SortOption.NewCases) }
                }
                carousel {
                    id("topNewCasesCarousel")
                    models(mapCases("topCase_", homeScreenData.topNewCases))
                }
                sectionHeader {
                    id("topInfectionRatesHeader")
                    title(getString(R.string.top_infection_rates))
                    isMoreButtonVisible(true)
                    clickListener { _ -> navigateToSummaryList(SortOption.InfectionRate) }
                }
                carousel {
                    id("topInfectionRatesCarousel")
                    models(
                        mapInfectionRateModels(
                            "topInfectionRate_",
                            homeScreenData.topInfectionRates
                        )
                    )
                }
                sectionHeader {
                    id("risingCasesHeader")
                    title(getString(R.string.rising_cases))
                    isMoreButtonVisible(true)
                    clickListener { _ -> navigateToSummaryList(SortOption.RisingCases) }
                }
                carousel {
                    id("risingCasesCarousel")
                    models(mapCases("risingCase_", homeScreenData.risingNewCases))
                }
                sectionHeader {
                    id("risingInfectionRatesHeader")
                    title(getString(R.string.rising_infection_rates))
                    isMoreButtonVisible(true)
                    clickListener { _ -> navigateToSummaryList(SortOption.RisingInfectionRate) }
                }
                carousel {
                    id("risingInfectionRatesCarousel")
                    models(
                        mapInfectionRateModels(
                            "risingInfectionRate_",
                            homeScreenData.risingInfectionRates
                        )
                    )
                }
            }
        })
    }

    private fun mapInfectionRateModels(
        idPrefix: String,
        topInfectionRates: List<SummaryModel>
    ): List<EpoxyModel<*>> =
        topInfectionRates.map { data ->
            SummaryCardBindingModel_()
                .id(idPrefix + data.areaName)
                .areaPosition(data.position)
                .areaName(data.areaName)
                .currentValue(data.currentInfectionRate.toInt())
                .currentValueCaption(getString(R.string.current_infection_rate))
                .changeInValue(data.changeInInfectionRate.toInt())
                .changeInValueCaption(getString(R.string.change_this_week))
                .clickListener { _ ->
                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                }
        }

    private fun mapCases(idPrefix: String, cases: List<SummaryModel>): List<EpoxyModel<*>> =
        cases.map { data ->
            SummaryCardBindingModel_()
                .id(idPrefix + data.areaName)
                .areaPosition(data.position)
                .areaName(data.areaName)
                .currentValue(data.currentNewCases)
                .currentValueCaption(getString(R.string.cases_this_week))
                .changeInValue(data.changeInCases)
                .changeInValueCaption(getString(R.string.change_this_week))
                .clickListener { _ ->
                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                }
        }

    private fun dailyRecordModels(
        idPrefix: String,
        latestUkData: List<LatestUkDataModel>
    ): List<EpoxyModel<*>> =
        latestUkData.map { data ->
            LatestUkDataCardBindingModel_()
                .id(idPrefix + data.areaName + data.lastUpdated)
                .areaName(data.areaName)
                .dailyCases(data.newCases)
                .totalCases(data.cumulativeCases)
                .lastUpdated(data.lastUpdated)
                .clickListener { _ ->
                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                }
        }

    private fun navigateToSummaryList(sortOption: SortOption) {
        findNavController()
            .navigate(HomeFragmentDirections.homeToSummaryList(sortOption))
    }

    private fun navigateToArea(areaCode: String, areaName: String, areaType: String) {
        val action =
            HomeFragmentDirections.homeToArea(
                areaCode = areaCode,
                areaName = areaName,
                areaType = areaType
            )
        findNavController()
            .navigate(action)
    }

    private fun navigateToInteractiveMap(redirectUri: String) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(redirectUri)
            )
            startActivity(intent)
        } catch (e: Throwable) {
        }
    }
}
