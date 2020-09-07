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
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.carousel
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.widgets.recyclerview.sectionHeader
import com.chrisa.covid19.features.home.domain.models.InfectionRateModel
import com.chrisa.covid19.features.home.domain.models.LatestUkDataModel
import com.chrisa.covid19.features.home.domain.models.NewCaseModel
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import com.chrisa.covid19.features.home.presentation.widgets.EmptySavedAreasCardModel_
import com.chrisa.covid19.features.home.presentation.widgets.LatestUkDataCardModel_
import com.chrisa.covid19.features.home.presentation.widgets.SavedAreaCardModel_
import com.chrisa.covid19.features.home.presentation.widgets.TopInfectionRateCardModel_
import com.chrisa.covid19.features.home.presentation.widgets.TopNewCaseCardModel_
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.fakeSearchBar
import kotlinx.android.synthetic.main.fragment_home.homeProgress
import kotlinx.android.synthetic.main.fragment_home.homeRecyclerView
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchBar()
        initRecyclerView()
        bindIsLoading()
        bindAreaCases()
    }

    private fun initSearchBar() {
        fakeSearchBar.setOnClickListener { navigateToHome() }
    }

    private fun initRecyclerView() {
        homeRecyclerView.addItemDecoration(
            HomeItemDecoration(
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
                sectionHeader {
                    id("dailyRecordHeader")
                    title(getString(R.string.uk_overview))
                }
                carousel {
                    id("dailyRecordCarousel")
                    models(dailyRecordModels("dailyRecord_", homeScreenData.latestUkData))
                }
                sectionHeader {
                    id("risingCasesHeader")
                    title(getString(R.string.rising_cases))
                }
                carousel {
                    id("risingCasesCarousel")
                    models(mapCases("risingCase_", homeScreenData.risingNewCases))
                }
                sectionHeader {
                    id("risingInfectionRatesHeader")
                    title(getString(R.string.rising_infection_rates))
                }
                carousel {
                    id("risingInfectionRatesCarousel")
                    models(mapInfectionRateModels("risingInfectionRate_", homeScreenData.risingInfectionRates))
                }
                sectionHeader {
                    id("topNewCasesHeader")
                    title(getString(R.string.top_new_cases))
                }
                carousel {
                    id("topNewCasesCarousel")
                    models(mapCases("topCase_", homeScreenData.topNewCases))
                }
                sectionHeader {
                    id("topInfectionRatesHeader")
                    title(getString(R.string.top_infection_rates))
                }
                carousel {
                    id("topInfectionRatesCarousel")
                    models(mapInfectionRateModels("topInfectionRate_", homeScreenData.topInfectionRates))
                }
                sectionHeader {
                    id("savedAreaHeader")
                    title(getString(R.string.saved_locations_title))
                }
                carousel {
                    id("savedAreaCarousel")
                    models(savedAreaModels("savedArea_", homeScreenData.savedAreas))
                }
            }
        })
    }

    private fun mapInfectionRateModels(idPrefix: String, topInfectionRates: List<InfectionRateModel>): List<EpoxyModel<*>> =
        topInfectionRates.map { data ->
            TopInfectionRateCardModel_()
                .id(idPrefix + data.areaName)
                .infectionRateModel(data)
                .clickListener { _ ->
                    navigateToArea(data.areaCode, data.areaName, data.areaType)
                }
        }

    private fun mapCases(idPrefix: String, cases: List<NewCaseModel>): List<EpoxyModel<*>> =
        cases.map { data ->
            TopNewCaseCardModel_()
                .id(idPrefix + data.areaName)
                .newCaseModel(data)
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
        }

    private fun savedAreaModels(
        idPrefix: String,
        savedAreas: List<SavedAreaModel>
    ): List<EpoxyModel<*>> =
        savedAreas.map { savedAreaModel ->
            SavedAreaCardModel_()
                .id(idPrefix + savedAreaModel.areaCode)
                .savedAreaModel(savedAreaModel)
                .clickListener { _ ->
                    navigateToArea(savedAreaModel.areaCode, savedAreaModel.areaName, savedAreaModel.areaType)
                }
        }.ifEmpty { listOf(EmptySavedAreasCardModel_().id("emptySavedAreas")) }

    private fun navigateToHome() {
        findNavController().navigate(HomeFragmentDirections.homeToSearch())
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
