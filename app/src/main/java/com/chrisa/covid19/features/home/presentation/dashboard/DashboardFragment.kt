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

package com.chrisa.covid19.features.home.presentation.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.carousel
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.widgets.recyclerview.sectionHeader
import com.chrisa.covid19.features.home.domain.models.LatestUkDataModel
import com.chrisa.covid19.features.home.domain.models.SortOption
import com.chrisa.covid19.features.home.domain.models.SummaryModel
import com.chrisa.covid19.features.home.presentation.HomeFragmentDirections
import com.chrisa.covid19.features.home.presentation.widgets.LatestUkDataCardModel_
import com.chrisa.covid19.features.home.presentation.widgets.SummaryCardModel_
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.homeProgress
import kotlinx.android.synthetic.main.fragment_dashboard.homeRecyclerView
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel: DashboardViewModel by viewModels()
    private var controllerState: Bundle? = null
    private var attachedController: EpoxyController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        bindIsLoading()
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
        homeRecyclerView.addItemDecoration(
            DashboardItemDecoration(
                homeRecyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                ),
                homeRecyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                )
            )
        )
    }

    private fun bindIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            val isLoading = it ?: return@Observer
            homeProgress.isVisible = isLoading
        })
    }

    private fun bindAreaCases() {
        viewModel.homeScreenData.observe(viewLifecycleOwner, Observer {
            val homeScreenData = it ?: return@Observer
            homeRecyclerView.isVisible = true
            homeRecyclerView.withModels {

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
            }
        })
    }

    private fun mapInfectionRateModels(
        idPrefix: String,
        topInfectionRates: List<SummaryModel>
    ): List<EpoxyModel<*>> =
        topInfectionRates.map { data ->
            SummaryCardModel_()
                .id(idPrefix + data.areaName)
                .summary(data)
                .showAreaPosition(true)
                .showInfectionRates(true)
                .clickListener { _ ->
                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                }
        }

    private fun mapCases(idPrefix: String, cases: List<SummaryModel>): List<EpoxyModel<*>> =
        cases.map { data ->
            SummaryCardModel_()
                .id(idPrefix + data.areaName)
                .summary(data)
                .showAreaPosition(true)
                .showCases(true)
                .clickListener { _ ->
                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                }
        }

    private fun dailyRecordModels(
        idPrefix: String,
        latestUkData: List<LatestUkDataModel>
    ): List<EpoxyModel<*>> =
        latestUkData.map { data ->
            LatestUkDataCardModel_()
                .id(idPrefix + data.areaName + data.lastUpdated)
                .latestUkData(data)
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
}
