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
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chrisa.cviz.R
import com.chrisa.cviz.core.ui.widgets.recyclerview.chart.chartTabCard
import com.chrisa.cviz.core.util.DateFormatter
import com.chrisa.cviz.features.area.presentation.widgets.areaCaseSummaryCard
import com.chrisa.cviz.features.area.presentation.widgets.areaDeathSummaryCard
import com.chrisa.cviz.features.area.presentation.widgets.areaHospitalSummaryCard
import com.chrisa.cviz.features.area.presentation.widgets.areaSectionHeader
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
import kotlinx.android.synthetic.main.area_error.areaError
import kotlinx.android.synthetic.main.area_error.errorAction
import kotlinx.android.synthetic.main.area_fragment.areaProgress
import kotlinx.android.synthetic.main.area_fragment.areaRecyclerView
import kotlinx.android.synthetic.main.area_fragment.areaToolbar
import kotlinx.android.synthetic.main.area_fragment.swipeRefreshLayout
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AreaFragment : Fragment(R.layout.area_fragment) {

    private val viewModel: AreaViewModel by viewModels()
    private val args by navArgs<AreaFragmentArgs>()
    private val disposables = CompositeDisposable()
    private val formatter = DateTimeFormatter
        .ofPattern("MMM d")
        .withLocale(Locale.UK)
        .withZone(ZoneId.of("GMT"))

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
        areaToolbar.navigationIcon =
            ContextCompat.getDrawable(areaToolbar.context, R.drawable.ic_arrow_back)
        areaToolbar.title = args.areaName
        areaToolbar.setNavigationOnClickListener { navigateUp() }
        disposables.addAll(subscribeMenuClicks())
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun initRecyclerView() {
        areaRecyclerView.addItemDecoration(
            AreaItemDecoration(
                areaRecyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin_large
                ),
                areaRecyclerView.context.resources.getDimensionPixelSize(
                    R.dimen.card_margin
                )
            )
        )
    }

    private fun subscribeMenuClicks(): @NonNull Disposable {
        return areaToolbar.itemClicks().subscribe {
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
            val lastCaseDate = dateLabel(areaDataModel.areaMetadata.lastCaseDate)
            val lastDeathDate = dateLabel(areaDataModel.areaMetadata.lastDeathDate)
            val lastHospitalAdmissionDate =
                dateLabel(areaDataModel.areaMetadata.lastHospitalAdmissionDate)
            val lastUpdated = areaRecyclerView.context.getString(
                R.string.updated_label,
                DateFormatter.getLocalRelativeTimeSpanString(areaDataModel.areaMetadata.lastUpdatedDate)
            )
            areaRecyclerView.withModels {
                areaSectionHeader {
                    id("caseSummaryTitle")
                    title(getString(R.string.cases_title))
                    subtitle1(
                        areaToolbar.context.getString(
                            R.string.latest_data_label,
                            lastCaseDate
                        )
                    )
                    subtitle2(lastUpdated)
                }
                areaCaseSummaryCard {
                    id("caseSummary")
                    summary(areaDataModel.caseSummary)
                }
                chartTabCard {
                    id("caseChartData")
                    chartData(areaDataModel.caseChartData)
                }
                if (areaDataModel.showDeaths) {
                    areaSectionHeader {
                        id("deathSummaryTitle")
                        title(getString(R.string.deaths_title))
                        subtitle1(
                            areaToolbar.context.getString(
                                R.string.latest_data_label,
                                lastDeathDate
                            )
                        )
                        subtitle2(lastUpdated)
                    }
                    areaDeathSummaryCard {
                        id("deathSummary")
                        summary(areaDataModel.deathSummary)
                    }
                    chartTabCard {
                        id("deathsChartData")
                        chartData(areaDataModel.deathsChartData)
                    }
                }
                if (areaDataModel.showHospitalAdmissions) {
                    areaSectionHeader {
                        id("hospitalAdmissionsSummaryTitle")
                        title(getString(R.string.hospital_admissions_title))
                        subtitle1(
                            areaToolbar.context.getString(
                                R.string.latest_data_label,
                                lastHospitalAdmissionDate
                            )
                        )
                        subtitle2(lastUpdated)
                    }
                    areaHospitalSummaryCard {
                        id("hospitalAdmissionsSummary")
                        summary(areaDataModel.hospitalAdmissionsSummary)
                    }
                    chartTabCard {
                        id("hospitalAdmissionsChartData")
                        chartData(areaDataModel.hospitalAdmissionsChartData)
                    }
                }
            }
            areaError.isVisible = false
        })
    }

    private fun dateLabel(date: LocalDate?) =
        date?.format(formatter) ?: ""

    private fun observeIsSaved() {
        viewModel.isSaved.observe(viewLifecycleOwner, Observer { isSaved ->
            isSaved?.let { saved ->
                val menu = areaToolbar.menu
                menu.findItem(R.id.insertSavedArea).isVisible = !saved
                menu.findItem(R.id.deleteSavedArea).isVisible = saved
            }
        })
    }

    private fun observeIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let { loading ->
                areaProgress.isVisible = loading
            }
        })
    }

    private fun observeIsRefreshing() {
        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
            swipeRefreshLayout.isRefreshing = it
        })
    }

    private fun observeSyncAreaError() {
        viewModel.syncAreaError.observe(viewLifecycleOwner, EventObserver { isFatal ->
            if (isFatal) {
                errorAction.setOnClickListener { viewModel.retry() }
                areaError.isVisible = true
                Snackbar.make(
                    areaRecyclerView,
                    getString(R.string.sync_error_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(
                    areaRecyclerView,
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
