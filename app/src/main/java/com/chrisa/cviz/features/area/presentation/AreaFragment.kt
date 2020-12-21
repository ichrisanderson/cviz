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

package com.chrisa.cviz.features.area.presentation

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
import androidx.navigation.fragment.navArgs
import com.chrisa.cviz.R
import com.chrisa.cviz.areaCaseSummaryCard
import com.chrisa.cviz.areaDeathSummaryCard
import com.chrisa.cviz.areaHospitalSummaryCard
import com.chrisa.cviz.areaSectionHeader
import com.chrisa.cviz.core.ui.widgets.recyclerview.chart.chartTabCard
import com.chrisa.cviz.core.util.DateFormatter
import com.chrisa.cviz.databinding.AreaFragmentBinding
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.appcompat.itemClicks
import dagger.hilt.android.AndroidEntryPoint
import io.plaidapp.core.util.event.EventObserver
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AreaFragment : Fragment(R.layout.area_fragment) {

    private lateinit var binding: AreaFragmentBinding
    private val viewModel: AreaViewModel by viewModels()
    private val args by navArgs<AreaFragmentArgs>()
    private val disposables = CompositeDisposable()
    private val formatter = DateTimeFormatter
        .ofPattern("MMM d")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AreaFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecyclerView()
        initSwipeRefreshLayout()
        observeCases()
        observeIsSaved()
        observeIsLoading()
        observeIsRefreshing()
        observeSyncAreaError()
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun initToolbar() {
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(binding.toolbar.context, R.drawable.ic_arrow_back)
        binding.toolbar.title = args.areaName
        binding.toolbar.setNavigationOnClickListener { navigateUp() }
        disposables.addAll(subscribeMenuClicks())
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.secondaryColor)
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun initRecyclerView() {
        binding.recyclerView.addItemDecoration(
            AreaItemDecoration(
                binding.recyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin_large
                ),
                binding.recyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                )
            )
        )
    }

    private fun subscribeMenuClicks(): @NonNull Disposable {
        return binding.toolbar.itemClicks().subscribe {
            when (it.itemId) {
                R.id.insertSavedArea -> {
                    viewModel.insertSavedArea()
                }
                R.id.deleteSavedArea -> {
                    viewModel.deleteSavedArea()
                }
                else -> {
                    // Ignore unknown menu options
                }
            }
        }
    }

    private fun observeCases() {
        viewModel.areaDataModel.observe(viewLifecycleOwner, Observer {
            val areaDataModel = it ?: return@Observer
            val lastCaseDate = dateLabel(areaDataModel.lastCaseDate)
            val lastDeathDate = dateLabel(areaDataModel.lastDeathPublishedDate)
            val lastHospitalAdmissionDate =
                dateLabel(areaDataModel.lastHospitalAdmissionDate)
            val lastUpdated = binding.recyclerView.context.getString(
                R.string.updated_label,
                DateFormatter.getLocalRelativeTimeSpanString(areaDataModel.lastUpdatedDate)
            )
            binding.recyclerView.withModels {
                areaSectionHeader {
                    id("caseSummaryTitle")
                    title(getString(R.string.cases_title))
                    subtitle1(
                        binding.toolbar.context.getString(
                            R.string.latest_data_label,
                            lastCaseDate
                        )
                    )
                    subtitle2(lastUpdated)
                }
                areaCaseSummaryCard {
                    id("caseSummary")
                    totalCases(areaDataModel.caseSummary.currentTotal)
                    dailyCases(areaDataModel.caseSummary.dailyTotal)
                    currentNewCases(areaDataModel.caseSummary.weeklyTotal)
                    changeInNewCasesThisWeek(areaDataModel.caseSummary.changeInTotal)
                    currentInfectionRate(areaDataModel.caseSummary.weeklyRate.toInt())
                    changeInInfectionRateThisWeek(areaDataModel.caseSummary.changeInRate.toInt())
                }
                chartTabCard {
                    id("caseChartData")
                    chartData(areaDataModel.caseChartData)
                }
                if (areaDataModel.showDeathsByPublishedDate) {
                    areaSectionHeader {
                        id("deathSummaryTitle")
                        title(getString(R.string.deaths_title))
                        subtitle1(
                            binding.toolbar.context.getString(
                                R.string.latest_data_label,
                                lastDeathDate
                            )
                        )
                        subtitle2(lastUpdated)
                    }
                    areaDeathSummaryCard {
                        id("deathSummary")
                        totalDeaths(areaDataModel.deathsByPublishedDateSummary.currentTotal)
                        dailyDeaths(areaDataModel.deathsByPublishedDateSummary.dailyTotal)
                        currentNewDeaths(areaDataModel.deathsByPublishedDateSummary.weeklyTotal)
                        changeInNewDeathsThisWeek(areaDataModel.deathsByPublishedDateSummary.changeInTotal)
                    }
                    chartTabCard {
                        id("deathsChartData")
                        chartData(areaDataModel.deathsByPublishedDateChartData)
                    }
                }
                if (areaDataModel.showHospitalAdmissions) {
                    areaSectionHeader {
                        id("hospitalAdmissionsSummaryTitle")
                        title(getString(R.string.hospital_admissions_title, areaDataModel.hospitalAdmissionsRegionName))
                        subtitle1(
                            binding.toolbar.context.getString(
                                R.string.latest_data_label,
                                lastHospitalAdmissionDate
                            )
                        )
                        subtitle2(lastUpdated)
                    }
                    areaHospitalSummaryCard {
                        id("hospitalAdmissionsSummary")
                        totalHospitalAdmissions(areaDataModel.hospitalAdmissionsSummary.currentTotal)
                        dailyHospitalAdmissions(areaDataModel.hospitalAdmissionsSummary.dailyTotal)
                        currentNewHospitalAdmissions(areaDataModel.hospitalAdmissionsSummary.weeklyTotal)
                        changeInNewHospitalAdmissionsThisWeek(areaDataModel.hospitalAdmissionsSummary.changeInTotal)
                    }
                    chartTabCard {
                        id("hospitalAdmissionsChartData")
                        chartData(areaDataModel.hospitalAdmissionsChartData)
                    }
                }
            }
            binding.error.container.isVisible = false
        })
    }

    private fun dateLabel(date: LocalDate?) =
        date?.format(formatter) ?: ""

    private fun observeIsSaved() {
        viewModel.isSaved.observe(viewLifecycleOwner, Observer { isSaved ->
            isSaved?.let { saved ->
                val menu = binding.toolbar.menu
                menu.findItem(R.id.insertSavedArea).isVisible = !saved
                menu.findItem(R.id.deleteSavedArea).isVisible = saved
            }
        })
    }

    private fun observeIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let { loading ->
                binding.progress.isVisible = loading
            }
        })
    }

    private fun observeIsRefreshing() {
        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            binding.swipeRefreshLayout.isRefreshing = it
        })
    }

    private fun observeSyncAreaError() {
        viewModel.syncAreaError.observe(viewLifecycleOwner, EventObserver { isFatal ->
            if (isFatal) {
                binding.error.errorAction.setOnClickListener { viewModel.retry() }
                binding.error.container.isVisible = true
                Snackbar.make(
                    binding.recyclerView,
                    getString(R.string.sync_error_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(
                    binding.recyclerView,
                    getString(R.string.sync_error_message),
                    Snackbar.LENGTH_SHORT
                )
                    .setAction(R.string.retry) {
                        viewModel.refresh()
                    }
                    .show()
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}
